package com.trigenys.cleanroute.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AltRoute
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bonjour",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = "Votre activité aujourd’hui",
                    style = MaterialTheme.typography.bodyLarge,
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
                FirstClientCard(onOpenClients = onOpenClients)
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vue d’ensemble",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "TEMPS RÉEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardKpiCard(
                            label = "Clients actifs",
                            value = current.activeClients.size.toString(),
                            icon = Icons.Outlined.Groups,
                            footer = "Voir la liste",
                            footerIcon = Icons.Outlined.ArrowForward,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onMetricSelected(DashboardMetric.ACTIVE_CLIENTS)
                                }
                        )

                        DashboardKpiCard(
                            label = "Encaissements",
                            value = formatXaf(current.receiptsXaf),
                            icon = Icons.Outlined.Payments,
                            footer = "Aujourd’hui",
                            footerIcon = Icons.Outlined.CalendarToday,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onMetricSelected(DashboardMetric.RECEIPTS)
                                }
                        )
                    }
                }
            }

            if (current.arrears.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenPayments),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Impayés à traiter",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = formatXaf(current.arrearsXaf),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            item {
                LocalSyncCard()
            }
        }
    }
}

@Composable
private fun FirstClientCard(
    onOpenClients: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DashboardIconBox(
                icon = Icons.Outlined.PersonAdd,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f),
                contentColor = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Commencez par vos clients",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "Ajoutez un client ou importez votre fichier Excel pour alimenter les tournées et les paiements.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DashboardActionButton(
            text = "Ajouter un client",
            icon = Icons.Filled.Add,
            onClick = onOpenClients,
            modifier = Modifier.fillMaxWidth()
        )
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
    val collectionDone = total > 0 && completed == total
    val nextZone = snapshot.zones.firstOrNull()?.zoneName

    CleanRouteCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Collecte du jour",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusChip(
                text = if (collectionDone) "Terminée" else "Hors connexion",
                tone = if (collectionDone) StatusTone.SUCCESS else StatusTone.NEUTRAL
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DashboardIconBox(
                icon = Icons.Outlined.AltRoute,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (total == 0) {
                        "Aucune tournée démarrée"
                    } else {
                        "$completed / $total passages terminés"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (total == 0) {
                        if (nextZone == null) {
                            "Prêt pour votre prochaine tournée"
                        } else {
                            "Prêt pour le circuit de $nextZone"
                        }
                    } else if (collectionDone) {
                        "La tournée du jour est terminée"
                    } else {
                        "Continuez là où vous vous êtes arrêté"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (total > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }

        DashboardActionButton(
            text = if (total == 0) "Commencer une tournée" else "Continuer la collecte",
            icon = Icons.Filled.PlayArrow,
            onClick = onOpenCollection,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun DashboardKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    footer: String,
    footerIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = footerIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = footer,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (label == "Clients actifs") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DashboardIconBox(
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DashboardActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LocalSyncCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Sync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Synchronisation locale active",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Données enregistrées localement sur cet appareil",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
        }
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
