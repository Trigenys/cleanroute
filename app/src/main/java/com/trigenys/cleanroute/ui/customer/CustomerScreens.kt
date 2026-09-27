package com.trigenys.cleanroute.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.CollectionCadence
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerProfile
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.PaymentMethods
import com.trigenys.cleanroute.domain.PaymentState
import com.trigenys.cleanroute.domain.RouteDayId
import com.trigenys.cleanroute.domain.ServicePlan
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.Zone
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun CustomerDirectoryScreen(
    entries: List<CustomerDirectoryEntry>,
    query: String,
    loading: Boolean,
    innerPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onAddCustomer: () -> Unit,
    onCustomerSelected: (CustomerDirectoryEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Clients", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        if (entries.size == 1) "1 client" else "${entries.size} clients",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onAddCustomer) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter")
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                label = { Text("Nom, téléphone ou zone") }
            )
        }

        if (loading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (entries.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (query.isBlank()) "Aucun client pour le moment"
                        else "Aucun client trouvé",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (query.isBlank()) {
                            "Ajoutez votre premier client. L’import Excel arrivera ensuite."
                        } else {
                            "Essayez un autre nom, numéro ou quartier."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (query.isBlank()) {
                        Button(onClick = onAddCustomer) {
                            Text("Ajouter un client")
                        }
                    }
                }
            }
        } else {
            items(
                items = entries,
                key = { it.customer.id.value }
            ) { entry ->
                CustomerRow(
                    entry = entry,
                    onClick = { onCustomerSelected(entry) }
                )
            }
        }
    }
}

@Composable
private fun CustomerRow(
    entry: CustomerDirectoryEntry,
    onClick: () -> Unit
) {
    CleanRouteCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.customer.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    entry.zoneName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.customer.phone?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusChip(
                text = if (entry.customer.status == CustomerStatus.ACTIVE) "Actif" else "Suspendu",
                tone = if (entry.customer.status == CustomerStatus.ACTIVE) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.WARNING
                }
            )
        }
    }
}

@Composable
fun CustomerDetailScreen(
    profile: CustomerProfile,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRecordPayment: () -> Unit = {},
    onReversePayment: (Payment) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text(
                    "Fiche client",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Modifier")
                }
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.customer.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            profile.customer.phone ?: "Aucun numéro",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            buildString {
                                append(profile.zone.name)
                                profile.customer.addressLabel?.let { append(" · ").append(it) }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(
                        text = if (profile.customer.status == CustomerStatus.ACTIVE) "Actif" else "Suspendu",
                        tone = if (profile.customer.status == CustomerStatus.ACTIVE) {
                            StatusTone.SUCCESS
                        } else {
                            StatusTone.WARNING
                        }
                    )
                }

                Text(
                    profile.nextCollectionDate?.let { "Prochain passage : $it" }
                        ?: "Prochain passage : non planifié",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaceholderAction(
                        label = "Appeler",
                        icon = Icons.Outlined.Phone,
                        modifier = Modifier.weight(1f)
                    )
                    PlaceholderAction(
                        label = "WhatsApp",
                        icon = Icons.Outlined.ChatBubbleOutline,
                        modifier = Modifier.weight(1f)
                    )
                    PlaceholderAction(
                        label = "Localiser",
                        icon = Icons.Outlined.LocationOn,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Ces actions seront activées dans l’étape communications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Text("Abonnement", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatXaf(profile.servicePlan.monthlyFeeXaf) + " / mois",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Payé ce mois : ${formatXaf(profile.paidThisPeriodXaf)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (profile.outstandingThisPeriodXaf == 0L) {
                        "À payer : rien"
                    } else {
                        "À payer : ${formatXaf(profile.outstandingThisPeriodXaf)}"
                    },
                    color = if (profile.outstandingThisPeriodXaf == 0L) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                )

                if (profile.outstandingThisPeriodXaf > 0L) {
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enregistrer un paiement")
                    }
                }
            }
        }

        item {
            Text("Historique récent", style = MaterialTheme.typography.titleMedium)
        }

        if (profile.recentVisits.isEmpty() && profile.recentPayments.isEmpty()) {
            item {
                Text(
                    "Aucune activité enregistrée.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(profile.recentVisits, key = { "visit-${it.id.value}" }) { visit ->
                ActivityRow(
                    title = when (visit.status) {
                        CollectionVisitStatus.COLLECTED -> "Collecte effectuée"
                        CollectionVisitStatus.ABSENT -> "Client absent"
                        CollectionVisitStatus.NO_WASTE -> "Pas de déchets"
                        CollectionVisitStatus.SCHEDULED -> "Collecte planifiée"
                    },
                    detail = visit.scheduledDate.toString()
                )
            }
            items(profile.recentPayments, key = { "payment-${it.id.value}" }) { payment ->
                PaymentActivityRow(
                    payment = payment,
                    onReverse = { onReversePayment(payment) }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun ActivityRow(title: String, detail: String) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentActivityRow(
    payment: Payment,
    onReverse: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Paiement · ${formatXaf(payment.amountXaf)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${payment.servicePeriod} · ${PaymentMethods.labelFor(payment.method)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (payment.state == PaymentState.RECORDED) "Enregistré" else "Annulé",
                tone = if (payment.state == PaymentState.RECORDED) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.NEUTRAL
                }
            )
        }

        if (payment.state == PaymentState.REVERSED) {
            Text(
                "Annulé le ${payment.reversedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(onClick = onReverse) {
                Text("Annuler ce paiement")
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    initialProfile: CustomerProfile?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (CustomerDraft) -> Unit
) {
    var name by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.name.orEmpty())
    }
    var phone by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.phone.orEmpty())
    }
    var zone by remember(initialProfile) {
        mutableStateOf(initialProfile?.zone?.name.orEmpty())
    }
    var address by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.addressLabel.orEmpty())
    }
    var monthlyFee by remember(initialProfile) {
        mutableStateOf(
            initialProfile?.servicePlan?.monthlyFeeXaf?.takeIf { it > 0 }?.toString().orEmpty()
        )
    }
    var active by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.status != CustomerStatus.SUSPENDED)
    }

    val feeValue = monthlyFee.toLongOrNull()
    val valid = name.isNotBlank() && zone.isNotBlank() && feeValue != null && feeValue >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialProfile == null) "Nouveau client" else "Modifier le client")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text("Zone / quartier *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Repère") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monthlyFee,
                    onValueChange = { monthlyFee = it.filter(Char::isDigit) },
                    label = { Text("Abonnement mensuel (F) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialProfile != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (active) "Client actif" else "Client suspendu",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = active,
                            onCheckedChange = { active = it }
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    "* Champs obligatoires",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CustomerDraft(
                            customerId = initialProfile?.customer?.id,
                            name = name,
                            phone = phone.trim().takeIf(String::isNotEmpty),
                            zoneName = zone,
                            addressLabel = address.trim().takeIf(String::isNotEmpty),
                            monthlyFeeXaf = requireNotNull(feeValue),
                            status = if (active) CustomerStatus.ACTIVE else CustomerStatus.SUSPENDED
                        )
                    )
                },
                enabled = valid
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

private fun formatXaf(amount: Long): String =
    NumberFormat.getIntegerInstance(Locale.FRENCH).format(amount) + " F"

private fun sampleCustomer(
    id: String = "customer-1",
    name: String = "Mme Mballa",
    status: CustomerStatus = CustomerStatus.ACTIVE
): Customer = Customer(
    id = CustomerId(id),
    name = name,
    phone = "+237 690 00 00 01",
    zoneId = ZoneId("zone-1"),
    addressLabel = "Immeuble vert",
    servicePlanId = ServicePlanId("plan-1"),
    status = status,
    createdAt = Instant.parse("2026-09-01T08:00:00Z"),
    updatedAt = Instant.parse("2026-09-26T08:00:00Z")
)

internal fun sampleCustomerProfile(
    status: CustomerStatus = CustomerStatus.ACTIVE
): CustomerProfile {
    val customer = sampleCustomer(status = status)
    return CustomerProfile(
        customer = customer,
        zone = Zone(ZoneId("zone-1"), "Bonamoussadi"),
        servicePlan = ServicePlan(
            id = ServicePlanId("plan-1"),
            label = "Abonnement",
            cadence = CollectionCadence.CUSTOM,
            monthlyFeeXaf = 5_000
        ),
        nextCollectionDate = LocalDate.of(2026, 9, 28),
        paidThisPeriodXaf = 5_000,
        outstandingThisPeriodXaf = 0,
        recentVisits = listOf(
            CollectionVisit(
                id = CollectionVisitId("visit-1"),
                routeDayId = RouteDayId("route-1"),
                customerId = customer.id,
                scheduledDate = LocalDate.of(2026, 9, 24),
                status = CollectionVisitStatus.COLLECTED,
                statusChangedAt = Instant.parse("2026-09-24T09:42:00Z"),
                revision = 1
            )
        ),
        recentPayments = listOf(
            Payment(
                id = PaymentId("payment-1"),
                customerId = customer.id,
                servicePeriod = YearMonth.of(2026, 9),
                amountXaf = 5_000,
                method = PaymentMethod("cash"),
                recordedAt = Instant.parse("2026-09-02T14:10:00Z")
            )
        )
    )
}

@Preview(name = "Clients - empty", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun EmptyDirectoryPreview() {
    CleanRouteTheme {
        CustomerDirectoryScreen(
            entries = emptyList(),
            query = "",
            loading = false,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onAddCustomer = {},
            onCustomerSelected = {}
        )
    }
}

@Preview(name = "Clients - populated", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PopulatedDirectoryPreview() {
    CleanRouteTheme {
        CustomerDirectoryScreen(
            entries = listOf(
                CustomerDirectoryEntry(sampleCustomer(), "Bonamoussadi"),
                CustomerDirectoryEntry(
                    sampleCustomer("customer-2", "M. Ewane", CustomerStatus.SUSPENDED),
                    "Makepe"
                )
            ),
            query = "",
            loading = false,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onAddCustomer = {},
            onCustomerSelected = {}
        )
    }
}

@Preview(name = "Client - suspended", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SuspendedDetailPreview() {
    CleanRouteTheme {
        CustomerDetailScreen(
            profile = sampleCustomerProfile(CustomerStatus.SUSPENDED),
            innerPadding = PaddingValues(),
            onBack = {},
            onEdit = {}
        )
    }
}
