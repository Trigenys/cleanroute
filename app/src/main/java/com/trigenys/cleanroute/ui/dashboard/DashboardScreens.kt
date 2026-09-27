package com.trigenys.cleanroute.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.DashboardClientRecord
import com.trigenys.cleanroute.domain.DashboardCollectionRecord
import com.trigenys.cleanroute.domain.DashboardMetric
import com.trigenys.cleanroute.domain.DashboardReceiptRecord
import com.trigenys.cleanroute.domain.DashboardZoneRecord
import com.trigenys.cleanroute.domain.OwnerDashboardSnapshot
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.PaymentMethods
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.CleanRoutePrimaryButton
import com.trigenys.cleanroute.ui.components.KpiTile
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun OwnerDashboardScreen(
    snapshot: OwnerDashboardSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    innerPadding: PaddingValues,
    onMetricSelected: (DashboardMetric) -> Unit,
    onOpenCollection: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenPayments: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Bonjour 👋", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Votre activité aujourd’hui",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (loading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        errorMessage?.let { error ->
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        val current = snapshot
        if (!loading && current != null && current.activeClients.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Commencez par vos clients",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Ajoutez un client ou importez votre fichier Excel pour alimenter les tournées et les paiements.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenClients,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ajouter un client")
                    }
                }
            }
        }

        if (current != null) {
            item {
                CollectionKpiCard(
                    snapshot = current,
                    onClick = { onMetricSelected(DashboardMetric.COLLECTION) },
                    onOpenCollection = onOpenCollection
                )
            }

            item {
                Text(
                    "Vue d’ensemble",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiTile(
                        label = "Clients actifs",
                        value = current.activeClients.size.toString(),
                        supportingText = "Voir la liste",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMetricSelected(DashboardMetric.ACTIVE_CLIENTS) }
                    )
                    KpiTile(
                        label = "Encaissements",
                        value = formatXaf(current.receiptsXaf),
                        supportingText = "Aujourd’hui",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMetricSelected(DashboardMetric.RECEIPTS) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiTile(
                        label = "Impayés",
                        value = formatXaf(current.arrearsXaf),
                        supportingText = current.servicePeriod.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMetricSelected(DashboardMetric.ARREARS) }
                    )
                    KpiTile(
                        label = "Nouveaux clients",
                        value = current.newClients.size.toString(),
                        supportingText = "Aujourd’hui",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMetricSelected(DashboardMetric.NEW_CLIENTS) }
                    )
                }
            }

            item {
                KpiTile(
                    label = "Zones aujourd’hui",
                    value = current.zones.size.toString(),
                    supportingText = if (current.zones.isEmpty()) {
                        "Aucune tournée démarrée"
                    } else {
                        "Voir les zones"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMetricSelected(DashboardMetric.ZONES) }
                )
            }

            if (current.arrears.isNotEmpty()) {
                item {
                    Button(
                        onClick = onOpenPayments,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voir les impayés")
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionKpiCard(
    snapshot: OwnerDashboardSnapshot,
    onClick: () -> Unit,
    onOpenCollection: () -> Unit
) {
    val total = snapshot.collectionTotal
    val completed = snapshot.collectionCompleted
    val progress = if (total == 0) 0f else completed.toFloat() / total

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
                Text(
                    "Collecte du jour",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    if (total == 0) {
                        "Aucune tournée démarrée"
                    } else {
                        "$completed / $total passages terminés"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (total > 0 && completed == total) "Terminée" else "Hors connexion",
                tone = if (total > 0 && completed == total) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.NEUTRAL
                }
            )
        }

        if (total > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        CleanRoutePrimaryButton(
            text = if (total == 0) "Commencer une tournée" else "Continuer la collecte",
            onClick = onOpenCollection,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DashboardDetailScreen(
    snapshot: OwnerDashboardSnapshot,
    metric: DashboardMetric,
    innerPadding: PaddingValues,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        metricTitle(metric),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        detailSubtitle(snapshot, metric),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (metric) {
            DashboardMetric.COLLECTION -> {
                if (snapshot.collectionRecords.isEmpty()) {
                    item { EmptyDetail("Aucun passage enregistré aujourd’hui.") }
                } else {
                    items(snapshot.collectionRecords, key = { it.visitId.value }) { record ->
                        CollectionDetailRow(record)
                    }
                }
            }

            DashboardMetric.ACTIVE_CLIENTS -> {
                if (snapshot.activeClients.isEmpty()) {
                    item { EmptyDetail("Aucun client actif.") }
                } else {
                    items(snapshot.activeClients, key = { it.customerId.value }) { record ->
                        ClientDetailRow(record)
                    }
                }
            }

            DashboardMetric.RECEIPTS -> {
                if (snapshot.receipts.isEmpty()) {
                    item { EmptyDetail("Aucun paiement encaissé aujourd’hui.") }
                } else {
                    items(snapshot.receipts, key = { it.paymentId.value }) { record ->
                        ReceiptDetailRow(record)
                    }
                }
            }

            DashboardMetric.ARREARS -> {
                if (snapshot.arrears.isEmpty()) {
                    item { EmptyDetail("Aucun impayé pour ${snapshot.servicePeriod}.") }
                } else {
                    items(snapshot.arrears, key = { it.customerId.value }) { record ->
                        ArrearsDetailRow(record)
                    }
                }
            }

            DashboardMetric.NEW_CLIENTS -> {
                if (snapshot.newClients.isEmpty()) {
                    item { EmptyDetail("Aucun nouveau client aujourd’hui.") }
                } else {
                    items(snapshot.newClients, key = { it.customerId.value }) { record ->
                        ClientDetailRow(record)
                    }
                }
            }

            DashboardMetric.ZONES -> {
                if (snapshot.zones.isEmpty()) {
                    item { EmptyDetail("Aucune zone démarrée aujourd’hui.") }
                } else {
                    items(snapshot.zones, key = { it.zoneId.value }) { record ->
                        ZoneDetailRow(record)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionDetailRow(record: DashboardCollectionRecord) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.customerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    record.zoneName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = collectionStatusLabel(record.status),
                tone = when (record.status) {
                    CollectionVisitStatus.COLLECTED -> StatusTone.SUCCESS
                    CollectionVisitStatus.ABSENT,
                    CollectionVisitStatus.NO_WASTE -> StatusTone.WARNING
                    CollectionVisitStatus.SCHEDULED -> StatusTone.NEUTRAL
                }
            )
        }
    }
}

@Composable
private fun ClientDetailRow(record: DashboardClientRecord) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(record.name, style = MaterialTheme.typography.titleMedium)
        Text(
            record.zoneName,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        record.phone?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReceiptDetailRow(record: DashboardReceiptRecord) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.customerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    PaymentMethods.labelFor(record.method),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatXaf(record.amountXaf),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            record.recordedAt.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArrearsDetailRow(record: ArrearsEntry) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.customerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    record.zoneName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = formatXaf(record.outstandingXaf),
                tone = StatusTone.WARNING
            )
        }
    }
}

@Composable
private fun ZoneDetailRow(record: DashboardZoneRecord) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(record.zoneName, style = MaterialTheme.typography.titleMedium)
        Text(
            "${record.completedStops} faits · ${record.remainingStops} restants",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyDetail(message: String) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun metricTitle(metric: DashboardMetric): String =
    when (metric) {
        DashboardMetric.COLLECTION -> "Collecte du jour"
        DashboardMetric.ACTIVE_CLIENTS -> "Clients actifs"
        DashboardMetric.RECEIPTS -> "Encaissements"
        DashboardMetric.ARREARS -> "Impayés"
        DashboardMetric.NEW_CLIENTS -> "Nouveaux clients"
        DashboardMetric.ZONES -> "Zones aujourd’hui"
    }

private fun detailSubtitle(
    snapshot: OwnerDashboardSnapshot,
    metric: DashboardMetric
): String =
    when (metric) {
        DashboardMetric.COLLECTION ->
            "${snapshot.collectionCompleted} / ${snapshot.collectionTotal} terminés"
        DashboardMetric.ACTIVE_CLIENTS ->
            "${snapshot.activeClients.size} clients"
        DashboardMetric.RECEIPTS ->
            formatXaf(snapshot.receiptsXaf)
        DashboardMetric.ARREARS ->
            "${formatXaf(snapshot.arrearsXaf)} · ${snapshot.servicePeriod}"
        DashboardMetric.NEW_CLIENTS ->
            "${snapshot.newClients.size} aujourd’hui"
        DashboardMetric.ZONES ->
            "${snapshot.zones.size} zones"
    }

private fun collectionStatusLabel(status: CollectionVisitStatus): String =
    when (status) {
        CollectionVisitStatus.SCHEDULED -> "À faire"
        CollectionVisitStatus.COLLECTED -> "Collecté"
        CollectionVisitStatus.ABSENT -> "Absent"
        CollectionVisitStatus.NO_WASTE -> "Pas de déchets"
    }

private fun formatXaf(amount: Long): String =
    NumberFormat.getIntegerInstance(Locale.FRENCH).format(amount) + " F"

internal fun sampleDashboardSnapshot(): OwnerDashboardSnapshot =
    OwnerDashboardSnapshot(
        date = LocalDate.of(2026, 9, 27),
        servicePeriod = YearMonth.of(2026, 9),
        collectionRecords = listOf(
            DashboardCollectionRecord(
                visitId = CollectionVisitId("visit-1"),
                customerId = CustomerId("customer-1"),
                customerName = "Mme Mballa",
                zoneName = "Bonamoussadi",
                status = CollectionVisitStatus.COLLECTED
            ),
            DashboardCollectionRecord(
                visitId = CollectionVisitId("visit-2"),
                customerId = CustomerId("customer-2"),
                customerName = "M. Ewane",
                zoneName = "Bonamoussadi",
                status = CollectionVisitStatus.SCHEDULED
            )
        ),
        activeClients = listOf(
            DashboardClientRecord(
                customerId = CustomerId("customer-1"),
                name = "Mme Mballa",
                phone = "+237 690 00 00 01",
                zoneName = "Bonamoussadi"
            ),
            DashboardClientRecord(
                customerId = CustomerId("customer-2"),
                name = "M. Ewane",
                phone = "+237 650 00 00 02",
                zoneName = "Bonamoussadi"
            )
        ),
        receipts = listOf(
            DashboardReceiptRecord(
                paymentId = PaymentId("payment-1"),
                customerId = CustomerId("customer-1"),
                customerName = "Mme Mballa",
                amountXaf = 5_000,
                method = PaymentMethod("orange_money"),
                recordedAt = Instant.parse("2026-09-27T09:00:00Z")
            )
        ),
        arrears = listOf(
            ArrearsEntry(
                customerId = CustomerId("customer-2"),
                customerName = "M. Ewane",
                phone = "+237 650 00 00 02",
                zoneName = "Bonamoussadi",
                monthlyFeeXaf = 5_000,
                paidXaf = 0,
                outstandingXaf = 5_000
            )
        ),
        newClients = listOf(
            DashboardClientRecord(
                customerId = CustomerId("customer-2"),
                name = "M. Ewane",
                phone = "+237 650 00 00 02",
                zoneName = "Bonamoussadi"
            )
        ),
        zones = listOf(
            DashboardZoneRecord(
                zoneId = ZoneId("zone-1"),
                zoneName = "Bonamoussadi",
                totalStops = 2,
                completedStops = 1
            )
        )
    )

@Preview(name = "Owner dashboard", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OwnerDashboardPreview() {
    CleanRouteTheme {
        OwnerDashboardScreen(
            snapshot = sampleDashboardSnapshot(),
            loading = false,
            errorMessage = null,
            innerPadding = PaddingValues(),
            onMetricSelected = {},
            onOpenCollection = {},
            onOpenClients = {},
            onOpenPayments = {}
        )
    }
}

@Preview(name = "First use", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun FirstUseDashboardPreview() {
    CleanRouteTheme {
        OwnerDashboardScreen(
            snapshot = OwnerDashboardSnapshot(
                date = LocalDate.of(2026, 9, 27),
                servicePeriod = YearMonth.of(2026, 9),
                collectionRecords = emptyList(),
                activeClients = emptyList(),
                receipts = emptyList(),
                arrears = emptyList(),
                newClients = emptyList(),
                zones = emptyList()
            ),
            loading = false,
            errorMessage = null,
            innerPadding = PaddingValues(),
            onMetricSelected = {},
            onOpenCollection = {},
            onOpenClients = {},
            onOpenPayments = {}
        )
    }
}
