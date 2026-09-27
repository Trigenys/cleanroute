package com.trigenys.cleanroute.data.repository

import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.local.OutboxOperationFactory
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity
import com.trigenys.cleanroute.data.local.toDomain
import com.trigenys.cleanroute.data.local.toEntity
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.CollectionCadence
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitRepository
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerProfile
import com.trigenys.cleanroute.domain.CustomerRepository
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentLedger
import com.trigenys.cleanroute.domain.PaymentMethodOption
import com.trigenys.cleanroute.domain.PaymentMethods
import com.trigenys.cleanroute.domain.PaymentRepository
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.ZoneId
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.UUID

class RoomCustomerRepository(
    private val database: CleanRouteDatabase
) : CustomerRepository {
    override suspend fun get(id: CustomerId): Customer? =
        database.customerDao().get(id.value)?.toDomain()

    override suspend fun search(query: String): List<CustomerDirectoryEntry> =
        database.customerDao()
            .search(query.trim())
            .map { row ->
                CustomerDirectoryEntry(
                    customer = row.customer.toDomain(),
                    zoneName = row.zoneName
                )
            }

    override suspend fun getProfile(
        id: CustomerId,
        servicePeriod: YearMonth,
        today: LocalDate
    ): CustomerProfile? {
        val customer = database.customerDao().get(id.value)?.toDomain() ?: return null
        val zone = database.catalogDao().getZone(customer.zoneId.value)?.toDomain() ?: return null
        val servicePlan = database.catalogDao()
            .getServicePlan(customer.servicePlanId.value)
            ?.toDomain()
            ?: return null
        val payments = database.paymentDao()
            .getForPeriod(customer.id.value, servicePeriod.toString())
            .map { it.toDomain() }

        return CustomerProfile(
            customer = customer,
            zone = zone,
            servicePlan = servicePlan,
            nextCollectionDate = database.collectionVisitDao()
                .getNextScheduled(customer.id.value, today.toString())
                ?.toDomain()
                ?.scheduledDate,
            paidThisPeriodXaf = PaymentLedger.paidAmountXaf(
                customerId = customer.id,
                servicePeriod = servicePeriod,
                payments = payments
            ),
            outstandingThisPeriodXaf = PaymentLedger.outstandingAmountXaf(
                customerId = customer.id,
                servicePeriod = servicePeriod,
                expectedAmountXaf = servicePlan.monthlyFeeXaf,
                payments = payments
            ),
            recentVisits = database.collectionVisitDao()
                .getRecentCompleted(customer.id.value, 3)
                .map { it.toDomain() },
            recentPayments = database.paymentDao()
                .getRecent(customer.id.value, 3)
                .map { it.toDomain() }
        )
    }

    override suspend fun saveDraft(
        draft: CustomerDraft,
        now: Instant
    ): CustomerId {
        val existing = draft.customerId?.let { get(it) }
        val existingPlan = existing?.let {
            database.catalogDao().getServicePlan(it.servicePlanId.value)
        }
        val customerId = draft.customerId ?: CustomerId(UUID.randomUUID().toString())
        val zoneId = ZoneId(stableId("zone", draft.zoneName))

        val preservePlan = existingPlan != null &&
            existingPlan.monthlyFeeXaf == draft.monthlyFeeXaf
        val servicePlanId = if (preservePlan) {
            requireNotNull(existing).servicePlanId
        } else {
            ServicePlanId("plan-${customerId.value}")
        }

        val customer = Customer(
            id = customerId,
            externalId = existing?.externalId,
            name = draft.name.trim(),
            phone = draft.phone?.trim()?.takeIf(String::isNotEmpty),
            zoneId = zoneId,
            addressLabel = draft.addressLabel?.trim()?.takeIf(String::isNotEmpty),
            servicePlanId = servicePlanId,
            status = draft.status,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )

        database.customerDao().saveCustomerWithCatalog(
            zone = ZoneEntity(
                id = zoneId.value,
                name = draft.zoneName.trim()
            ),
            servicePlan = ServicePlanEntity(
                id = servicePlanId.value,
                label = existingPlan?.label ?: "Abonnement",
                cadence = existingPlan?.cadence ?: CollectionCadence.CUSTOM.name,
                monthlyFeeXaf = draft.monthlyFeeXaf
            ),
            customer = customer.toEntity(),
            operation = OutboxOperationFactory.customer(customer)
        )
        return customerId
    }

    override suspend fun upsert(customer: Customer) {
        database.customerDao().upsertWithOutbox(
            customer = customer.toEntity(),
            operation = OutboxOperationFactory.customer(customer)
        )
    }

    private fun stableId(prefix: String, rawValue: String): String {
        val normalized = rawValue.trim().lowercase(Locale.ROOT)
        val uuid = UUID.nameUUIDFromBytes(normalized.toByteArray(StandardCharsets.UTF_8))
        return "$prefix-$uuid"
    }
}

class RoomCollectionVisitRepository(
    private val database: CleanRouteDatabase
) : CollectionVisitRepository {
    override suspend fun get(id: CollectionVisitId): CollectionVisit? =
        database.collectionVisitDao().get(id.value)?.toDomain()

    override suspend fun upsert(visit: CollectionVisit) {
        database.collectionVisitDao().upsertWithOutbox(
            visit = visit.toEntity(),
            operation = OutboxOperationFactory.collectionVisit(visit)
        )
    }
}

class RoomPaymentRepository(
    private val database: CleanRouteDatabase
) : PaymentRepository {
    override val methods: List<PaymentMethodOption> = PaymentMethods.DEFAULT

    override suspend fun get(id: PaymentId): Payment? =
        database.paymentDao().get(id.value)?.toDomain()

    override suspend fun record(
        draft: PaymentDraft,
        at: Instant
    ): Payment {
        val paymentId = PaymentId(
            UUID.nameUUIDFromBytes(
                "payment:${draft.submissionId}".toByteArray(StandardCharsets.UTF_8)
            ).toString()
        )

        val existing = get(paymentId)
        if (existing != null) {
            require(
                existing.customerId == draft.customerId &&
                    existing.servicePeriod == draft.servicePeriod &&
                    existing.amountXaf == draft.amountXaf &&
                    existing.method == draft.method
            ) {
                "Cet identifiant de paiement a déjà été utilisé pour une autre opération."
            }
            return existing
        }

        val payment = Payment(
            id = paymentId,
            customerId = draft.customerId,
            servicePeriod = draft.servicePeriod,
            amountXaf = draft.amountXaf,
            method = draft.method,
            recordedAt = at
        )
        upsert(payment)
        return payment
    }

    override suspend fun reverse(
        id: PaymentId,
        at: Instant
    ): Payment {
        val current = get(id) ?: error("Paiement introuvable.")
        val reversed = current.reverse(at)
        if (reversed != current) {
            upsert(reversed)
        }
        return reversed
    }

    override suspend fun arrears(
        servicePeriod: YearMonth,
        query: String
    ): List<ArrearsEntry> =
        database.paymentDao()
            .arrears(
                servicePeriod = servicePeriod.toString(),
                query = query.trim()
            )
            .map { row ->
                ArrearsEntry(
                    customerId = CustomerId(row.customerId),
                    customerName = row.customerName,
                    phone = row.phone,
                    zoneName = row.zoneName,
                    monthlyFeeXaf = row.monthlyFeeXaf,
                    paidXaf = row.paidXaf,
                    outstandingXaf = row.outstandingXaf
                )
            }

    override suspend fun upsert(payment: Payment) {
        database.paymentDao().upsertWithOutbox(
            payment = payment.toEntity(),
            operation = OutboxOperationFactory.payment(payment)
        )
    }
}
