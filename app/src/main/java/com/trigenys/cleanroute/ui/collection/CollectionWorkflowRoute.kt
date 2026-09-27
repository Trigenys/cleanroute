package com.trigenys.cleanroute.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.trigenys.cleanroute.communication.ContactLaunchResult
import com.trigenys.cleanroute.communication.CustomerContactService
import com.trigenys.cleanroute.communication.CustomerMessageData
import com.trigenys.cleanroute.communication.CustomerMessageKind
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CollectionWorkflowRepository
import com.trigenys.cleanroute.domain.DailyCollectionRoute
import com.trigenys.cleanroute.domain.ZoneWorkload
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun CollectionWorkflowRoute(
    repository: CollectionWorkflowRepository,
    contactService: CustomerContactService,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }

    var workloads by remember { mutableStateOf(emptyList<ZoneWorkload>()) }
    var route by remember { mutableStateOf<DailyCollectionRoute?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedVisitId by rememberSaveable { mutableStateOf<String?>(null) }
    var refreshVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshVersion) {
        loading = true
        runCatching {
            repository.zoneWorkloads(today)
        }.onSuccess {
            workloads = it
        }.onFailure {
            errorMessage = it.message ?: "Impossible de charger les zones."
        }
        loading = false
    }

    if (route == null) {
        ZoneWorkloadScreen(
            workloads = workloads,
            loading = loading,
            errorMessage = errorMessage,
            innerPadding = innerPadding,
            onZoneSelected = { workload ->
                scope.launch {
                    loading = true
                    errorMessage = null
                    runCatching {
                        repository.ensureRoute(today, workload.zoneId)
                    }.onSuccess { loadedRoute ->
                        route = loadedRoute
                        expandedVisitId = loadedRoute.stops
                            .firstOrNull { it.visit.status == CollectionVisitStatus.SCHEDULED }
                            ?.visit
                            ?.id
                            ?.value
                    }.onFailure {
                        errorMessage = it.message ?: "Impossible de démarrer la tournée."
                    }
                    loading = false
                }
            }
        )
    } else {
        val currentRoute = requireNotNull(route)
        DailyRouteScreen(
            route = currentRoute,
            innerPadding = innerPadding,
            expandedVisitId = expandedVisitId,
            busy = loading,
            errorMessage = errorMessage,
            onBack = {
                route = null
                expandedVisitId = null
                refreshVersion += 1
            },
            onToggleActions = { visitId ->
                expandedVisitId = if (expandedVisitId == visitId.value) {
                    null
                } else {
                    visitId.value
                }
            },
            onOutcome = { visitId, outcome ->
                scope.launch {
                    loading = true
                    errorMessage = null
                    runCatching {
                        repository.recordOutcome(
                            visitId = visitId,
                            outcome = outcome,
                            at = Instant.now()
                        )
                        repository.getRoute(today, currentRoute.zoneId)
                    }.onSuccess { refreshed ->
                        if (refreshed != null) {
                            route = refreshed
                            expandedVisitId = refreshed.stops
                                .firstOrNull {
                                    it.visit.status == CollectionVisitStatus.SCHEDULED
                                }
                                ?.visit
                                ?.id
                                ?.value
                            refreshVersion += 1
                        }
                    }.onFailure {
                        errorMessage = it.message ?: "Impossible d’enregistrer le résultat."
                    }
                    loading = false
                }
            },
            onCall = { stop ->
                val phone = stop.phone
                if (phone != null) {
                    scope.launch {
                        errorMessage = contactService.call(
                            context = context,
                            customerId = stop.visit.customerId,
                            rawPhone = phone,
                            at = Instant.now()
                        ).feedback()
                    }
                }
            },
            onWhatsApp = { stop ->
                val phone = stop.phone
                if (phone != null) {
                    scope.launch {
                        val kind = when (stop.visit.status) {
                            CollectionVisitStatus.SCHEDULED ->
                                CustomerMessageKind.UPCOMING_COLLECTION
                            CollectionVisitStatus.COLLECTED ->
                                CustomerMessageKind.COLLECTION_COMPLETED
                            CollectionVisitStatus.ABSENT,
                            CollectionVisitStatus.NO_WASTE -> null
                        }
                        val data = kind?.let {
                            CustomerMessageData(
                                customerName = stop.customerName,
                                collectionDate = stop.visit.scheduledDate
                            )
                        }
                        errorMessage = contactService.whatsApp(
                            context = context,
                            customerId = stop.visit.customerId,
                            rawPhone = phone,
                            kind = kind,
                            data = data,
                            at = Instant.now()
                        ).feedback()
                    }
                }
            }
        )
    }
}

private fun ContactLaunchResult.feedback(): String? =
    when (this) {
        ContactLaunchResult.Launched -> null
        is ContactLaunchResult.Unavailable -> message
        is ContactLaunchResult.Failed -> message
    }
