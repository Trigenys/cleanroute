package com.trigenys.cleanroute.ui.collection

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.DailyCollectionRoute
import com.trigenys.cleanroute.domain.DailyCollectionStop
import com.trigenys.cleanroute.domain.RouteDayId
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.domain.ZoneWorkload
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.Instant
import java.time.LocalDate

@Composable
fun ZoneWorkloadScreen(
    workloads: List<ZoneWorkload>,
    loading: Boolean,
    errorMessage: String?,
    innerPadding: PaddingValues,
    onZoneSelected: (ZoneWorkload) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Collecte", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Choisissez une zone pour la tournée d’aujourd’hui.",
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
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!loading && workloads.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Aucune zone prête",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Ajoutez ou importez d’abord des clients actifs.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = workloads,
                key = { it.zoneId.value }
            ) { workload ->
                ZoneWorkloadCard(
                    workload = workload,
                    onClick = { onZoneSelected(workload) }
                )
            }
        }

        item {
            Text(
                "Pilote : démarrer une zone inclut ses clients actifs. Le filtrage automatique par jours de passage sera branché après validation du fichier Excel réel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ZoneWorkloadCard(
    workload: ZoneWorkload,
    onClick: () -> Unit
) {
    val progress = if (workload.totalStops == 0) {
        0f
    } else {
        workload.completedStops.toFloat() / workload.totalStops
    }

    CleanRouteCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workload.zoneName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "${workload.completedStops} faits · ${workload.remainingStops} restants",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (workload.remainingStops == 0) "Terminée" else "${workload.totalStops} arrêts",
                tone = if (workload.remainingStops == 0) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.NEUTRAL
                }
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onClick,
            enabled = workload.totalStops > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (workload.completedStops == 0) {
                    "Démarrer la zone"
                } else {
                    "Continuer la zone"
                }
            )
        }
    }
}

@Composable
fun DailyRouteScreen(
    route: DailyCollectionRoute,
    innerPadding: PaddingValues,
    expandedVisitId: String?,
    busy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onToggleActions: (CollectionVisitId) -> Unit,
    onOutcome: (CollectionVisitId, CollectionVisitStatus) -> Unit,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val progress = if (route.stops.isEmpty()) {
        0f
    } else {
        route.completedStops.toFloat() / route.stops.size
    }

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
                        route.zoneName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "${route.completedStops} / ${route.stops.size} terminés",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = if (route.remainingStops == 0) "Terminée" else "${route.remainingStops} restants",
                    tone = if (route.remainingStops == 0) {
                        StatusTone.SUCCESS
                    } else {
                        StatusTone.NEUTRAL
                    }
                )
            }
        }

        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (busy) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        errorMessage?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (route.stops.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Aucun client actif dans cette zone.")
                }
            }
        } else {
            items(
                items = route.stops,
                key = { it.visit.id.value }
            ) { stop ->
                CollectionStopCard(
                    stop = stop,
                    expanded = expandedVisitId == stop.visit.id.value,
                    busy = busy,
                    onToggleActions = { onToggleActions(stop.visit.id) },
                    onOutcome = { outcome -> onOutcome(stop.visit.id, outcome) },
                    onCall = stop.phone?.let { phone -> { onCall(phone) } },
                    onWhatsApp = stop.phone?.let { phone -> { onWhatsApp(phone) } }
                )
            }
        }
    }
}

@Composable
private fun CollectionStopCard(
    stop: DailyCollectionStop,
    expanded: Boolean,
    busy: Boolean,
    onToggleActions: () -> Unit,
    onOutcome: (CollectionVisitStatus) -> Unit,
    onCall: (() -> Unit)?,
    onWhatsApp: (() -> Unit)?
) {
    val scheduled = stop.visit.status == CollectionVisitStatus.SCHEDULED

    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stop.customerName,
                    style = MaterialTheme.typography.titleMedium
                )
                stop.addressLabel?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                stop.phone?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!scheduled) {
                StatusChip(
                    text = outcomeLabel(stop.visit.status),
                    tone = when (stop.visit.status) {
                        CollectionVisitStatus.COLLECTED -> StatusTone.SUCCESS
                        CollectionVisitStatus.ABSENT,
                        CollectionVisitStatus.NO_WASTE -> StatusTone.WARNING
                        CollectionVisitStatus.SCHEDULED -> StatusTone.NEUTRAL
                    }
                )
            }
        }

        if (scheduled) {
            Button(
                onClick = { onOutcome(CollectionVisitStatus.COLLECTED) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Collecté")
            }
        } else if (stop.visit.revision > 1) {
            Text(
                "Corrigé ${stop.visit.revision - 1} fois · révision ${stop.visit.revision}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(
            onClick = onToggleActions,
            enabled = !busy
        ) {
            Text(
                if (expanded) "Masquer les actions"
                else if (scheduled) "Autres actions"
                else "Corriger / actions"
            )
        }

        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOutcome(CollectionVisitStatus.ABSENT) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Absent")
                }
                OutlinedButton(
                    onClick = { onOutcome(CollectionVisitStatus.NO_WASTE) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pas de déchets")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCall?.invoke() },
                    enabled = !busy && onCall != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Phone, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Appeler")
                }
                OutlinedButton(
                    onClick = { onWhatsApp?.invoke() },
                    enabled = !busy && onWhatsApp != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }

            if (!scheduled && stop.visit.status != CollectionVisitStatus.COLLECTED) {
                OutlinedButton(
                    onClick = { onOutcome(CollectionVisitStatus.COLLECTED) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Corriger en « Collecté »")
                }
            }
        }
    }
}

private fun outcomeLabel(status: CollectionVisitStatus): String =
    when (status) {
        CollectionVisitStatus.SCHEDULED -> "À faire"
        CollectionVisitStatus.COLLECTED -> "Collecté"
        CollectionVisitStatus.ABSENT -> "Absent"
        CollectionVisitStatus.NO_WASTE -> "Pas de déchets"
    }

private fun previewStop(
    id: String,
    name: String,
    status: CollectionVisitStatus,
    revision: Int = if (status == CollectionVisitStatus.SCHEDULED) 0 else 1
): DailyCollectionStop = DailyCollectionStop(
    visit = CollectionVisit(
        id = CollectionVisitId(id),
        routeDayId = RouteDayId("route-1"),
        customerId = CustomerId("customer-$id"),
        scheduledDate = LocalDate.of(2026, 9, 27),
        status = status,
        statusChangedAt = if (status == CollectionVisitStatus.SCHEDULED) {
            null
        } else {
            Instant.parse("2026-09-27T08:00:00Z")
        },
        revision = revision
    ),
    customerName = name,
    phone = "+237 690 00 00 01",
    addressLabel = "Carrefour, portail vert"
)

internal fun sampleDailyRoute(): DailyCollectionRoute = DailyCollectionRoute(
    routeDayId = RouteDayId("route-1"),
    date = LocalDate.of(2026, 9, 27),
    zoneId = ZoneId("zone-1"),
    zoneName = "Bonamoussadi",
    stops = listOf(
        previewStop("visit-1", "Mme Mballa", CollectionVisitStatus.SCHEDULED),
        previewStop("visit-2", "M. Ewane", CollectionVisitStatus.COLLECTED),
        previewStop("visit-3", "Mme Nguema", CollectionVisitStatus.ABSENT, revision = 2)
    )
)

@Preview(name = "Zones", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ZoneWorkloadPreview() {
    CleanRouteTheme {
        ZoneWorkloadScreen(
            workloads = listOf(
                ZoneWorkload(ZoneId("z1"), "Bonamoussadi", 12, 7),
                ZoneWorkload(ZoneId("z2"), "Makepe", 8, 8)
            ),
            loading = false,
            errorMessage = null,
            innerPadding = PaddingValues(),
            onZoneSelected = {}
        )
    }
}

@Preview(name = "Route - actions", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DailyRouteActionsPreview() {
    val route = sampleDailyRoute()
    CleanRouteTheme {
        DailyRouteScreen(
            route = route,
            innerPadding = PaddingValues(),
            expandedVisitId = "visit-1",
            busy = false,
            errorMessage = null,
            onBack = {},
            onToggleActions = {},
            onOutcome = { _, _ -> },
            onCall = {},
            onWhatsApp = {}
        )
    }
}
