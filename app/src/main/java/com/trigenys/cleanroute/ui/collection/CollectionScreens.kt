package com.trigenys.cleanroute.ui.collection

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

private const val COLLAPSED_STOP_COUNT = 4

@Composable
fun ZoneWorkloadScreen(
    workloads: List<ZoneWorkload>,
    loading: Boolean,
    errorMessage: String?,
    innerPadding: PaddingValues,
    onZoneSelected: (ZoneWorkload) -> Unit,
    onOpenClients: () -> Unit = {},
    onOpenExcel: () -> Unit = {}
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collecte",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    StatusChip(
                        text = "Tournée du jour",
                        tone = StatusTone.SUCCESS
                    )
                }
                Text(
                    text = "Choisissez une zone pour la tournée d’aujourd’hui.",
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (!loading && workloads.isEmpty()) {
            item {
                EmptyZoneCard(onOpenClients = onOpenClients)
            }
        }

        item {
            PilotFieldPanel()
        }

        if (!loading && workloads.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zones sectorielles",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = "${workloads.size} prévue${if (workloads.size > 1) "s" else ""}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

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
            ExcelTemplateCard(onOpenExcel = onOpenExcel)
        }
    }
}

@Composable
private fun EmptyZoneCard(
    onOpenClients: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Aucune zone prête",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ajoutez ou importez d’abord des clients actifs pour débloquer l’itinéraire.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onOpenClients,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Importer ou ajouter des clients",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PilotFieldPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MODE PILOTE TERRAIN",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "· Phase 1",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Démarrer une zone inclut ses clients actifs. Le filtrage automatique par jours de passage sera branché après validation du fichier Excel réel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExcelTemplateCard(
    onOpenExcel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenExcel),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gabarit d’import Excel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Modèle normalisé (.xlsx)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(onClick = onOpenExcel, shape = CircleShape) {
                Text("Ouvrir")
            }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workload.zoneName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (workload.totalStops == 0) {
                            "0 client affecté"
                        } else {
                            "${workload.remainingStops} à faire · ${workload.completedStops} faits"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Configurer", maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            if (workload.totalStops > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
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
    onCall: (DailyCollectionStop) -> Unit,
    onWhatsApp: (DailyCollectionStop) -> Unit,
    tourStarted: Boolean = true,
    onStartTour: () -> Unit = {},
    onOpenClients: () -> Unit = {},
    onOpenExcel: () -> Unit = {}
) {
    var showAllStops by remember { mutableStateOf(false) }
    val visibleStops = if (showAllStops) route.stops else route.stops.take(COLLAPSED_STOP_COUNT)
    val activeStop = if (tourStarted) {
        route.stops.firstOrNull { it.visit.status == CollectionVisitStatus.SCHEDULED }
    } else {
        null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeStop != null) {
            item {
                ActiveTourHeader(route = route, onBack = onBack)
            }
        } else {
            item {
                ZoneReadyBanner(route = route)
            }

            item {
                ZoneConfigurationHeader(
                    route = route,
                    onBack = onBack
                )
            }
        }

        if (busy) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        errorMessage?.let { error ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (activeStop != null) {
            item {
                ActiveStopCard(
                    stop = activeStop,
                    busy = busy,
                    onOutcome = { outcome -> onOutcome(activeStop.visit.id, outcome) },
                    onCall = activeStop.phone?.let { { onCall(activeStop) } },
                    onWhatsApp = activeStop.phone?.let { { onWhatsApp(activeStop) } }
                )
            }

            val upcoming = route.stops.filter {
                it.visit.status == CollectionVisitStatus.SCHEDULED && it != activeStop
            }
            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        text = "Prochaines étapes (${upcoming.size} restant${if (upcoming.size > 1) "s" else ""})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(
                    items = upcoming,
                    key = { stop -> "upcoming-" + stop.visit.id.value }
                ) { stop ->
                    UpcomingStopRow(
                        position = route.stops.indexOf(stop) + 1,
                        stop = stop
                    )
                }
            }

            val handled = route.stops.filter { it.visit.status != CollectionVisitStatus.SCHEDULED }
            if (handled.isNotEmpty()) {
                item {
                    Text(
                        text = "Déjà traités (${handled.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(
                    items = handled,
                    key = { stop -> "handled-" + stop.visit.id.value }
                ) { stop ->
                    CollectionStopCard(
                        position = route.stops.indexOf(stop) + 1,
                        stop = stop,
                        expanded = expandedVisitId == stop.visit.id.value,
                        busy = busy,
                        tourStarted = tourStarted,
                        onToggleActions = { onToggleActions(stop.visit.id) },
                        onOutcome = { outcome -> onOutcome(stop.visit.id, outcome) },
                        onCall = stop.phone?.let { { onCall(stop) } },
                        onWhatsApp = stop.phone?.let { { onWhatsApp(stop) } }
                    )
                }
            }
        } else {
        item {
            ScheduleSection(route = route)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordre de ramassage",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        text = "${route.remainingStops} prochain${if (route.remainingStops > 1) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (route.stops.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Aucun client actif dans cette zone.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Ajoutez des clients actifs à cette zone avant de démarrer la tournée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            itemsIndexed(
                items = visibleStops,
                key = { _, stop -> stop.visit.id.value }
            ) { index, stop ->
                CollectionStopCard(
                    position = index + 1,
                    stop = stop,
                    expanded = expandedVisitId == stop.visit.id.value,
                    busy = busy,
                    tourStarted = tourStarted,
                    onToggleActions = { onToggleActions(stop.visit.id) },
                    onOutcome = { outcome -> onOutcome(stop.visit.id, outcome) },
                    onCall = stop.phone?.let { { onCall(stop) } },
                    onWhatsApp = stop.phone?.let { { onWhatsApp(stop) } }
                )
            }
            if (!showAllStops && route.stops.size > COLLAPSED_STOP_COUNT) {
                item {
                    TextButton(
                        onClick = { showAllStops = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Voir les ${route.stops.size - COLLAPSED_STOP_COUNT} autres adresses",
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Outlined.ExpandMore, contentDescription = null)
                    }
                }
            }
        }
        }

        if (!tourStarted) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gestion des abonnés & imports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenExcel,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Importer Excel", maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = onOpenClients,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Outlined.Groups, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Clients", maxLines = 1)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onStartTour,
                    enabled = route.stops.isNotEmpty() && !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Démarrer la tournée sur cette zone",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(
                    text = if (tourStarted) {
                        "Feuille de route disponible localement pour la collecte hors connexion."
                    } else {
                        "La feuille de route sera utilisée localement pendant la tournée."
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ZoneReadyBanner(
    route: DailyCollectionRoute
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
            Text(
                text = "ZONE PRÊTE HORS CONNEXION",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (route.stops.isEmpty()) "À configurer" else "Prête",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ZoneConfigurationHeader(
    route: DailyCollectionRoute,
    onBack: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Retour"
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "SECTEUR ACTIF",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "· ID: ${shortZoneId(route.zoneId.value)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = route.zoneName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tournée du ${dayLabel(route.date.dayOfWeek).lowercase()} · ${route.stops.size} adresse${if (route.stops.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZoneMetric(
                label = "PRÉVUS",
                value = route.stops.size.toString(),
                modifier = Modifier.weight(1f)
            )
            ZoneMetric(
                label = "RESTANTS",
                value = route.remainingStops.toString(),
                modifier = Modifier.weight(1f)
            )
            ZoneMetric(
                label = "TERMINÉS",
                value = route.completedStops.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        if (route.stops.isNotEmpty()) {
            LinearProgressIndicator(
                progress = {
                    route.completedStops.toFloat() / route.stops.size
                },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
private fun ZoneMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ScheduleSection(
    route: DailyCollectionRoute
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jour de passage planifié",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusChip(
                text = "Aujourd’hui",
                tone = StatusTone.SUCCESS
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = dayLabel(route.date.dayOfWeek),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${route.stops.size} adresse${if (route.stops.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionStopCard(
    position: Int,
    stop: DailyCollectionStop,
    expanded: Boolean,
    busy: Boolean,
    tourStarted: Boolean,
    onToggleActions: () -> Unit,
    onOutcome: (CollectionVisitStatus) -> Unit,
    onCall: (() -> Unit)?,
    onWhatsApp: (() -> Unit)?
) {
    val scheduled = stop.visit.status == CollectionVisitStatus.SCHEDULED

    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (scheduled) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = if (scheduled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stop.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                stop.addressLabel?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                stop.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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

        if (tourStarted && scheduled) {
            Button(
                onClick = { onOutcome(CollectionVisitStatus.COLLECTED) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape
            ) {
                Text("Collecté")
            }
        } else if (!scheduled && stop.visit.revision > 1) {
            Text(
                text = "Corrigé ${stop.visit.revision - 1} fois · révision ${stop.visit.revision}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (tourStarted) {
            TextButton(
                onClick = onToggleActions,
                enabled = !busy
            ) {
                Text(
                    if (expanded) {
                        "Masquer les actions"
                    } else if (scheduled) {
                        "Autres actions"
                    } else {
                        "Corriger / actions"
                    }
                )
            }
        }

        if (tourStarted && expanded) {
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
                ContactButton(
                    text = "Appeler",
                    icon = Icons.Outlined.Phone,
                    onClick = { onCall?.invoke() },
                    enabled = !busy && onCall != null,
                    modifier = Modifier.weight(1f)
                )
                ContactButton(
                    text = "WhatsApp",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    onClick = { onWhatsApp?.invoke() },
                    enabled = !busy && onWhatsApp != null,
                    modifier = Modifier.weight(1f)
                )
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

@Composable
private fun ActiveTourHeader(
    route: DailyCollectionRoute,
    onBack: () -> Unit
) {
    val total = route.stops.size
    val progress = if (total == 0) 0f else route.completedStops.toFloat() / total

    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Retour"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.zoneName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tournée en cours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = "${route.completedStops}/$total collectés",
                tone = StatusTone.SUCCESS
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
private fun ActiveStopCard(
    stop: DailyCollectionStop,
    busy: Boolean,
    onOutcome: (CollectionVisitStatus) -> Unit,
    onCall: (() -> Unit)?,
    onWhatsApp: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ARRÊT IMMÉDIAT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stop.customerName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            stop.addressLabel?.let { address ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { onCall?.invoke() },
                    enabled = !busy && onCall != null,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Appel direct", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { onWhatsApp?.invoke() },
                    enabled = !busy && onWhatsApp != null,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", maxLines = 1)
                }
            }
            Button(
                onClick = { onOutcome(CollectionVisitStatus.COLLECTED) },
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Collecté", fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { onOutcome(CollectionVisitStatus.ABSENT) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Text("Absent", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { onOutcome(CollectionVisitStatus.NO_WASTE) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape
                ) {
                    Text("Pas de déchet", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun UpcomingStopRow(
    position: Int,
    stop: DailyCollectionStop
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                stop.addressLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            StatusChip(text = "À venir", tone = StatusTone.NEUTRAL)
        }
    }
}

@Composable
private fun ContactButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

private fun shortZoneId(value: String): String =
    value
        .replace("-", "")
        .takeLast(6)
        .uppercase()

private fun dayLabel(day: DayOfWeek): String =
    when (day) {
        DayOfWeek.MONDAY -> "Lundi"
        DayOfWeek.TUESDAY -> "Mardi"
        DayOfWeek.WEDNESDAY -> "Mercredi"
        DayOfWeek.THURSDAY -> "Jeudi"
        DayOfWeek.FRIDAY -> "Vendredi"
        DayOfWeek.SATURDAY -> "Samedi"
        DayOfWeek.SUNDAY -> "Dimanche"
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

internal fun sampleReadyRoute(): DailyCollectionRoute = DailyCollectionRoute(
    routeDayId = RouteDayId("route-ready"),
    date = LocalDate.of(2026, 9, 29),
    zoneId = ZoneId("zone-1"),
    zoneName = "Bonamoussadi",
    stops = listOf(
        previewStop("ready-1", "Mme Mballa", CollectionVisitStatus.SCHEDULED),
        previewStop("ready-2", "M. Ewane", CollectionVisitStatus.SCHEDULED),
        previewStop("ready-3", "Mme Nguema", CollectionVisitStatus.SCHEDULED)
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

@Preview(name = "Zone configuration", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ZoneConfigurationPreview() {
    CleanRouteTheme {
        DailyRouteScreen(
            route = sampleReadyRoute(),
            innerPadding = PaddingValues(),
            expandedVisitId = null,
            busy = false,
            errorMessage = null,
            onBack = {},
            onToggleActions = {},
            onOutcome = { _, _ -> },
            onCall = { _ -> },
            onWhatsApp = { _ -> },
            tourStarted = false,
            onStartTour = {}
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
            onCall = { _ -> },
            onWhatsApp = { _ -> },
            tourStarted = true
        )
    }
}
