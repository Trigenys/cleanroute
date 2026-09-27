package com.trigenys.cleanroute.ui.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.trigenys.cleanroute.domain.DashboardMetric
import com.trigenys.cleanroute.domain.DashboardRepository
import com.trigenys.cleanroute.domain.OwnerDashboardSnapshot
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DashboardRoute(
    repository: DashboardRepository,
    innerPadding: PaddingValues,
    onOpenCollection: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenPayments: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val timeZone = remember { ZoneId.systemDefault() }
    var snapshot by remember { mutableStateOf<OwnerDashboardSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedMetricName by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(today, timeZone) {
        loading = true
        errorMessage = null
        runCatching {
            repository.snapshot(today, timeZone)
        }.onSuccess {
            snapshot = it
        }.onFailure {
            errorMessage = it.message ?: "Impossible de charger le tableau de bord."
        }
        loading = false
    }

    val current = snapshot
    val selectedMetric = selectedMetricName?.let { DashboardMetric.valueOf(it) }

    if (current != null && selectedMetric != null) {
        DashboardDetailScreen(
            snapshot = current,
            metric = selectedMetric,
            innerPadding = innerPadding,
            onBack = { selectedMetricName = null }
        )
    } else {
        OwnerDashboardScreen(
            snapshot = current,
            loading = loading,
            errorMessage = errorMessage,
            innerPadding = innerPadding,
            onMetricSelected = { selectedMetricName = it.name },
            onOpenCollection = onOpenCollection,
            onOpenClients = onOpenClients,
            onOpenPayments = onOpenPayments
        )
    }
}
