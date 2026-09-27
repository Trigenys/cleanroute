package com.trigenys.cleanroute.data.transfer

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.entity.CustomerEntity
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity
import com.trigenys.cleanroute.domain.CollectionCadence
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.ZoneId
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.UUID

class CustomerSpreadsheetService(
    private val database: CleanRouteDatabase
) {
    suspend fun previewImport(
        file: File,
        displayName: String,
        now: Instant
    ): CustomerImportPlan {
        if (displayName.lowercase(Locale.ROOT).endsWith(".xls")) {
            return fatalPlan(
                sourceName = displayName,
                message = "Le format Excel .xls ancien n’est pas pris en charge. Enregistrez le fichier en .xlsx puis réessayez."
            )
        }

        val existingCustomers = database.customerDao().getAll()
        val existingByExternalId = existingCustomers
            .mapNotNull { customer -> customer.externalId?.let { it to customer } }
            .toMap()
        val plansById = database.catalogDao().getAllServicePlans().associateBy { it.id }
        val zonesById = database.catalogDao().getAllZones().associateBy { it.id }

        var headerMap: HeaderMap? = null
        var unknownHeaders = emptyList<String>()
        var fatalIssues = emptyList<String>()
        val rowIssues = mutableListOf<ImportIssue>()
        val actions = mutableListOf<CustomerImportAction>()
        val seenSourceIds = mutableSetOf<String>()
        var invalidCount = 0
        var dataRowCount = 0

        try {
            readRows(file, displayName) { rowNumber, values ->
                if (headerMap == null) {
                    val mapped = HeaderMapping.map(values)
                    headerMap = mapped
                    unknownHeaders = mapped.unknownHeaders
                    if (mapped.missingRequired.isNotEmpty()) {
                        fatalIssues = listOf(
                            "Colonnes obligatoires manquantes : " +
                                mapped.missingRequired.joinToString(", ") { friendlyHeader(it) }
                        )
                        throw StopImport
                    }
                    return@readRows
                }

                dataRowCount += 1
                if (dataRowCount > MAX_IMPORT_ROWS) {
                    fatalIssues = listOf(
                        "Le fichier dépasse la limite de sécurité de $MAX_IMPORT_ROWS lignes. Découpez-le en plusieurs fichiers."
                    )
                    throw StopImport
                }

                val mapped = requireNotNull(headerMap)
                val parsed = runCatching {
                    parseRow(
                        row = values,
                        header = mapped,
                        rowNumber = rowNumber,
                        now = now,
                        existingByExternalId = existingByExternalId,
                        plansById = plansById,
                        zonesById = zonesById
                    )
                }.getOrElse { error ->
                    invalidCount += 1
                    if (rowIssues.size < MAX_REPORTED_ROW_ISSUES) {
                        rowIssues += ImportIssue(
                            rowNumber = rowNumber,
                            message = error.message ?: "Ligne invalide."
                        )
                    }
                    return@readRows
                }

                if (!seenSourceIds.add(parsed.customer.externalId.orEmpty())) {
                    invalidCount += 1
                    if (rowIssues.size < MAX_REPORTED_ROW_ISSUES) {
                        rowIssues += ImportIssue(
                            rowNumber = rowNumber,
                            message = "Identifiant source dupliqué dans le même fichier."
                        )
                    }
                    return@readRows
                }

                actions += parsed
            }
        } catch (_: StopImport) {
            // Expected short-circuit for fatal header/size validation.
        } catch (error: Exception) {
            return fatalPlan(
                sourceName = displayName,
                message = "Impossible de lire le fichier : ${error.message ?: "format non reconnu"}"
            )
        }

        if (headerMap == null && fatalIssues.isEmpty()) {
            fatalIssues = listOf("Le fichier ne contient aucune ligne d’en-tête lisible.")
        }

        val creates = actions.count { it.type == ImportActionType.CREATE }
        val updates = actions.count { it.type == ImportActionType.UPDATE }
        val unchanged = actions.count { it.type == ImportActionType.UNCHANGED }

        return CustomerImportPlan(
            sourceName = displayName,
            summary = ImportSummary(
                creates = creates,
                updates = updates,
                unchanged = unchanged,
                invalid = invalidCount
            ),
            unknownHeaders = unknownHeaders,
            fatalIssues = fatalIssues,
            rowIssues = rowIssues,
            actions = actions
        )
    }

    suspend fun applyImport(plan: CustomerImportPlan): ImportApplyResult {
        require(plan.fatalIssues.isEmpty()) {
            "Un import avec erreur bloquante ne peut pas être appliqué."
        }

        plan.actions.forEach { action ->
            if (action.type == ImportActionType.UNCHANGED) return@forEach
            database.customerDao().saveCustomerWithCatalog(
                zone = action.zone,
                servicePlan = action.servicePlan,
                customer = action.customer,
                operation = action.outboxOperation
            )
        }

        return ImportApplyResult(
            created = plan.summary.creates,
            updated = plan.summary.updates,
            unchanged = plan.summary.unchanged,
            invalid = plan.summary.invalid
        )
    }

    suspend fun exportXlsx(output: OutputStream) {
        val customers = database.customerDao().getAllForExport()
        val payments = database.paymentDao().getAllForExport()
        val visits = database.collectionVisitDao().getAllForExport()
        val externalByCustomerId = customers.associate {
            it.customer.id to (it.customer.externalId ?: it.customer.id)
        }

        val customerRows = sequence {
            yield(
                textRow(
                    "external_id",
                    "name",
                    "phone",
                    "zone",
                    "address_label",
                    "collection_frequency",
                    "monthly_fee_xaf",
                    "status",
                    "created_at",
                    "updated_at"
                )
            )
            customers.forEach { row ->
                yield(
                    listOf(
                        SpreadsheetCell.Text(exportSourceId(row.customer.externalId, row.customer.id)),
                        SpreadsheetCell.Text(row.customer.name),
                        SpreadsheetCell.Text(row.customer.phone.orEmpty()),
                        SpreadsheetCell.Text(row.zoneName),
                        SpreadsheetCell.Text(row.customer.addressLabel.orEmpty()),
                        SpreadsheetCell.Text(row.cadence),
                        SpreadsheetCell.Number(row.monthlyFeeXaf),
                        SpreadsheetCell.Text(row.customer.status),
                        SpreadsheetCell.Text(Instant.ofEpochMilli(row.customer.createdAtEpochMs).toString()),
                        SpreadsheetCell.Text(Instant.ofEpochMilli(row.customer.updatedAtEpochMs).toString())
                    )
                )
            }
        }

        val paymentRows = sequence {
            yield(
                textRow(
                    "payment_id",
                    "customer_external_id",
                    "service_period",
                    "amount_xaf",
                    "method",
                    "state",
                    "recorded_at",
                    "reversed_at"
                )
            )
            payments.forEach { payment ->
                yield(
                    listOf(
                        SpreadsheetCell.Text(payment.id),
                        SpreadsheetCell.Text(externalByCustomerId[payment.customerId].orEmpty()),
                        SpreadsheetCell.Text(payment.servicePeriod),
                        SpreadsheetCell.Number(payment.amountXaf),
                        SpreadsheetCell.Text(payment.methodCode),
                        SpreadsheetCell.Text(payment.state),
                        SpreadsheetCell.Text(Instant.ofEpochMilli(payment.recordedAtEpochMs).toString()),
                        SpreadsheetCell.Text(
                            payment.reversedAtEpochMs?.let { Instant.ofEpochMilli(it).toString() }.orEmpty()
                        )
                    )
                )
            }
        }

        val collectionRows = sequence {
            yield(
                textRow(
                    "visit_id",
                    "customer_external_id",
                    "route_day_id",
                    "scheduled_date",
                    "status",
                    "status_changed_at",
                    "revision"
                )
            )
            visits.forEach { visit ->
                yield(
                    listOf(
                        SpreadsheetCell.Text(visit.id),
                        SpreadsheetCell.Text(externalByCustomerId[visit.customerId].orEmpty()),
                        SpreadsheetCell.Text(visit.routeDayId),
                        SpreadsheetCell.Text(visit.scheduledDateIso),
                        SpreadsheetCell.Text(visit.status),
                        SpreadsheetCell.Text(
                            visit.statusChangedAtEpochMs?.let { Instant.ofEpochMilli(it).toString() }.orEmpty()
                        ),
                        SpreadsheetCell.Number(visit.revision.toLong())
                    )
                )
            }
        }

        SimpleXlsxWriter.write(
            output = output,
            sheets = listOf(
                SpreadsheetSheet("Clients", customerRows),
                SpreadsheetSheet("Paiements", paymentRows),
                SpreadsheetSheet("Collectes", collectionRows)
            )
        )
    }

    private fun parseRow(
        row: List<String>,
        header: HeaderMap,
        rowNumber: Int,
        now: Instant,
        existingByExternalId: Map<String, CustomerEntity>,
        plansById: Map<String, ServicePlanEntity>,
        zonesById: Map<String, ZoneEntity>
    ): CustomerImportAction {
        val name = header.value(row, "name")
            ?: error("Ligne $rowNumber : nom manquant.")
        val zoneName = header.value(row, "zone")
            ?: error("Ligne $rowNumber : zone/quartier manquant.")
        val monthlyFee = parseMoney(
            header.value(row, "monthly_fee_xaf")
                ?: error("Ligne $rowNumber : montant d’abonnement manquant.")
        )
        val phone = header.value(row, "phone")
        val address = header.value(row, "address_label")
        val cadence = parseCadence(header.value(row, "collection_frequency"))
        val status = parseStatus(header.value(row, "status"))

        val sourceId = sourceId(
            provided = header.value(row, "external_id"),
            name = name,
            phone = phone,
            zone = zoneName,
            address = address
        )
        val existing = existingByExternalId[sourceId]
        val customerId = existing?.id
            ?: deterministicUuid("customer:$sourceId")
        val zoneId = deterministicUuid("zone:${HeaderMapping.normalize(zoneName)}")
        val existingPlan = existing?.let { plansById[it.servicePlanId] }
        val planId = if (existingPlan != null && existingPlan.id.startsWith("plan-import-")) {
            existingPlan.id
        } else {
            "plan-import-$customerId"
        }

        val zone = ZoneEntity(
            id = zoneId,
            name = zoneName.trim()
        )
        val servicePlan = ServicePlanEntity(
            id = planId,
            label = existingPlan?.label ?: "Abonnement importé",
            cadence = cadence.name,
            monthlyFeeXaf = monthlyFee
        )
        val customer = CustomerEntity(
            id = customerId,
            externalId = sourceId,
            name = name.trim(),
            phone = phone?.trim()?.takeIf(String::isNotEmpty),
            zoneId = zoneId,
            addressLabel = address?.trim()?.takeIf(String::isNotEmpty),
            servicePlanId = planId,
            status = status.name,
            createdAtEpochMs = existing?.createdAtEpochMs ?: now.toEpochMilli(),
            updatedAtEpochMs = now.toEpochMilli()
        )

        val unchanged = existing != null &&
            existing.name == customer.name &&
            existing.phone == customer.phone &&
            existing.addressLabel == customer.addressLabel &&
            existing.status == customer.status &&
            zonesById[existing.zoneId]?.name == zone.name &&
            existingPlan?.let {
                it.cadence == servicePlan.cadence &&
                    it.monthlyFeeXaf == servicePlan.monthlyFeeXaf
            } == true

        val effectiveCustomer = if (unchanged) {
            customer.copy(updatedAtEpochMs = existing.updatedAtEpochMs)
        } else {
            customer
        }

        val domainCustomer = Customer(
            id = CustomerId(effectiveCustomer.id),
            externalId = effectiveCustomer.externalId,
            name = effectiveCustomer.name,
            phone = effectiveCustomer.phone,
            zoneId = ZoneId(effectiveCustomer.zoneId),
            addressLabel = effectiveCustomer.addressLabel,
            servicePlanId = ServicePlanId(effectiveCustomer.servicePlanId),
            status = CustomerStatus.valueOf(effectiveCustomer.status),
            createdAt = Instant.ofEpochMilli(effectiveCustomer.createdAtEpochMs),
            updatedAt = Instant.ofEpochMilli(effectiveCustomer.updatedAtEpochMs)
        )

        return CustomerImportAction(
            type = when {
                existing == null -> ImportActionType.CREATE
                unchanged -> ImportActionType.UNCHANGED
                else -> ImportActionType.UPDATE
            },
            zone = zone,
            servicePlan = servicePlan,
            customer = effectiveCustomer,
            outboxOperation = OutboxOperationFactory.customer(domainCustomer)
        )
    }

    private fun readRows(
        file: File,
        displayName: String,
        onRow: (Int, List<String>) -> Unit
    ) {
        val lower = displayName.lowercase(Locale.ROOT)
        when {
            lower.endsWith(".xlsx") -> XlsxRows.readFirstSheet(file, onRow)
            lower.endsWith(".csv") -> FileInputStream(file).use { CsvRows.read(it, onRow) }
            isZipFile(file) -> XlsxRows.readFirstSheet(file, onRow)
            else -> FileInputStream(file).use { CsvRows.read(it, onRow) }
        }
    }

    private fun isZipFile(file: File): Boolean =
        FileInputStream(file).use { input ->
            val first = input.read()
            val second = input.read()
            first == 0x50 && second == 0x4B
        }

    private fun sourceId(
        provided: String?,
        name: String,
        phone: String?,
        zone: String,
        address: String?
    ): String {
        if (!provided.isNullOrBlank()) {
            val normalized = provided.trim()
            if (normalized.startsWith("excel-id:") || normalized.startsWith("excel-fp:")) {
                return normalized
            }
            return "excel-id:$normalized"
        }
        val identity = listOf(
            HeaderMapping.normalize(name),
            phone.orEmpty().filter(Char::isDigit),
            HeaderMapping.normalize(zone),
            HeaderMapping.normalize(address.orEmpty())
        ).joinToString("|")
        return "excel-fp:${sha256(identity).take(24)}"
    }

    private fun parseMoney(value: String): Long {
        if (value.trim().startsWith("-")) {
            error("Le montant ne peut pas être négatif.")
        }
        val digits = value.filter(Char::isDigit)
        if (digits.isEmpty()) error("Montant d’abonnement invalide : $value")
        return digits.toLongOrNull()
            ?: error("Montant d’abonnement trop grand : $value")
    }

    private fun parseCadence(value: String?): CollectionCadence =
        when (HeaderMapping.normalize(value.orEmpty())) {
            "weekly", "hebdomadaire", "semaine", "chaque_semaine" -> CollectionCadence.WEEKLY
            "biweekly", "toutes_les_2_semaines", "toutes_les_deux_semaines" -> CollectionCadence.BIWEEKLY
            "monthly", "mensuel", "mensuelle", "mois" -> CollectionCadence.MONTHLY
            else -> CollectionCadence.CUSTOM
        }

    private fun parseStatus(value: String?): CustomerStatus =
        when (HeaderMapping.normalize(value.orEmpty())) {
            "suspended", "suspendu", "suspendue", "inactive", "inactif", "inactive_client" ->
                CustomerStatus.SUSPENDED
            else -> CustomerStatus.ACTIVE
        }

    private fun deterministicUuid(value: String): String =
        UUID.nameUUIDFromBytes(value.toByteArray(StandardCharsets.UTF_8)).toString()

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun exportSourceId(externalId: String?, fallback: String): String =
        when {
            externalId?.startsWith("excel-id:") == true -> externalId.removePrefix("excel-id:")
            externalId != null -> externalId
            else -> fallback
        }

    private fun textRow(vararg values: String): List<SpreadsheetCell> =
        values.map(SpreadsheetCell::Text)

    private fun friendlyHeader(canonical: String): String =
        when (canonical) {
            "name" -> "nom"
            "zone" -> "zone/quartier"
            "monthly_fee_xaf" -> "montant/abonnement"
            else -> canonical
        }

    private fun fatalPlan(sourceName: String, message: String): CustomerImportPlan =
        CustomerImportPlan(
            sourceName = sourceName,
            summary = ImportSummary(0, 0, 0, 0),
            unknownHeaders = emptyList(),
            fatalIssues = listOf(message),
            rowIssues = emptyList(),
            actions = emptyList()
        )

    private object StopImport : RuntimeException()

    companion object {
        const val MAX_IMPORT_ROWS = 20_000
        private const val MAX_REPORTED_ROW_ISSUES = 100
    }
}
