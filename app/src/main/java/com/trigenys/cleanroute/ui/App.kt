package com.trigenys.cleanroute.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.communication.CustomerContactService
import com.trigenys.cleanroute.data.transfer.CustomerSpreadsheetService
import com.trigenys.cleanroute.domain.CollectionWorkflowRepository
import com.trigenys.cleanroute.domain.CustomerRepository
import com.trigenys.cleanroute.domain.DashboardRepository
import com.trigenys.cleanroute.domain.OwnerDashboardSnapshot
import com.trigenys.cleanroute.domain.PaymentRepository
import com.trigenys.cleanroute.domain.RetentionRepository
import com.trigenys.cleanroute.ui.collection.CollectionWorkflowRoute
import com.trigenys.cleanroute.ui.collection.ZoneWorkloadScreen
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.CleanRoutePrimaryButton
import com.trigenys.cleanroute.ui.components.CleanRouteSecondaryButton
import com.trigenys.cleanroute.ui.components.KpiTile
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.customer.CustomerDirectoryRoute
import com.trigenys.cleanroute.ui.dashboard.DashboardRoute
import com.trigenys.cleanroute.ui.dashboard.OwnerDashboardScreen
import com.trigenys.cleanroute.ui.customer.CustomerDirectoryScreen
import com.trigenys.cleanroute.ui.more.MoreRoute
import com.trigenys.cleanroute.ui.more.MoreSection
import com.trigenys.cleanroute.ui.navigation.AppDestination
import com.trigenys.cleanroute.ui.navigation.CleanRouteBottomBar
import com.trigenys.cleanroute.ui.navigation.CleanRouteTopBar
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun App(
    customerRepository: CustomerRepository? = null,
    collectionWorkflowRepository: CollectionWorkflowRepository? = null,
    contactService: CustomerContactService? = null,
    dashboardRepository: DashboardRepository? = null,
    paymentRepository: PaymentRepository? = null,
    retentionRepository: RetentionRepository? = null,
    spreadsheetService: CustomerSpreadsheetService? = null
) {
    var selectedDestination by rememberSaveable {
        mutableStateOf(AppDestination.HOME)
    }
    var moreSectionName by rememberSaveable {
        mutableStateOf(MoreSection.PAYMENTS.name)
    }

    CleanRouteShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = { selectedDestination = it },
        moreSection = MoreSection.valueOf(moreSectionName),
        onOpenMoreSection = { section ->
            moreSectionName = section.name
            selectedDestination = AppDestination.MORE
        },
        customerRepository = customerRepository,
        collectionWorkflowRepository = collectionWorkflowRepository,
        contactService = contactService,
        dashboardRepository = dashboardRepository,
        paymentRepository = paymentRepository,
        retentionRepository = retentionRepository,
        spreadsheetService = spreadsheetService
    )
}

@Composable
private fun CleanRouteShell(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    moreSection: MoreSection,
    onOpenMoreSection: (MoreSection) -> Unit,
    customerRepository: CustomerRepository?,
    collectionWorkflowRepository: CollectionWorkflowRepository?,
    contactService: CustomerContactService?,
    dashboardRepository: DashboardRepository?,
    paymentRepository: PaymentRepository?,
    retentionRepository: RetentionRepository?,
    spreadsheetService: CustomerSpreadsheetService?
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CleanRouteTopBar(
                onProfileClick = { onOpenMoreSection(MoreSection.PAYMENTS) }
            )
        },
        bottomBar = {
            CleanRouteBottomBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected
            )
        }
    ) { innerPadding ->
        when (selectedDestination) {
            AppDestination.HOME -> {
                if (dashboardRepository == null) {
                    OwnerDashboardScreen(
                        snapshot = OwnerDashboardSnapshot(
                            date = LocalDate.now(),
                            servicePeriod = YearMonth.now(),
                            collectionRecords = emptyList(),
                            activeClients = emptyList(),
                            receipts = emptyList(),
                            arrears = emptyList(),
                            newClients = emptyList(),
                            zones = emptyList()
                        ),
                        loading = false,
                        errorMessage = null,
                        innerPadding = innerPadding,
                        onMetricSelected = {},
                        onOpenCollection = { onDestinationSelected(AppDestination.COLLECTION) },
                        onOpenClients = { onDestinationSelected(AppDestination.CLIENTS) },
                        onOpenPayments = { onOpenMoreSection(MoreSection.PAYMENTS) }
                    )
                } else {
                    DashboardRoute(
                        repository = dashboardRepository,
                        innerPadding = innerPadding,
                        onOpenCollection = { onDestinationSelected(AppDestination.COLLECTION) },
                        onOpenClients = { onDestinationSelected(AppDestination.CLIENTS) },
                        onOpenPayments = { onOpenMoreSection(MoreSection.PAYMENTS) }
                    )
                }
            }

            AppDestination.COLLECTION -> {
                if (collectionWorkflowRepository == null || contactService == null) {
                    ZoneWorkloadScreen(
                        workloads = emptyList(),
                        loading = false,
                        errorMessage = null,
                        innerPadding = innerPadding,
                        onZoneSelected = {},
                        onOpenClients = {
                            onDestinationSelected(AppDestination.CLIENTS)
                        },
                        onOpenExcel = {
                            onOpenMoreSection(MoreSection.EXCEL)
                        }
                    )
                } else {
                    CollectionWorkflowRoute(
                        repository = collectionWorkflowRepository,
                        contactService = contactService,
                        innerPadding = innerPadding,
                        onOpenClients = {
                            onDestinationSelected(AppDestination.CLIENTS)
                        },
                        onOpenExcel = {
                            onOpenMoreSection(MoreSection.EXCEL)
                        }
                    )
                }
            }

            AppDestination.CLIENTS -> {
                if (
                    customerRepository == null ||
                    paymentRepository == null ||
                    contactService == null
                ) {
                    CustomerDirectoryScreen(
                        entries = emptyList(),
                        query = "",
                        loading = false,
                        innerPadding = innerPadding,
                        onQueryChange = {},
                        onAddCustomer = {},
                        onCustomerSelected = {},
                        onImportCustomers = {
                            onOpenMoreSection(MoreSection.EXCEL)
                        },
                        onOpenCollection = {
                            onDestinationSelected(AppDestination.COLLECTION)
                        }
                    )
                } else {
                    CustomerDirectoryRoute(
                        repository = customerRepository,
                        paymentRepository = paymentRepository,
                        contactService = contactService,
                        retentionRepository = retentionRepository,
                        innerPadding = innerPadding,
                        onOpenImport = {
                            onOpenMoreSection(MoreSection.EXCEL)
                        },
                        onOpenCollection = {
                            onDestinationSelected(AppDestination.COLLECTION)
                        }
                    )
                }
            }

            AppDestination.MORE -> {
                MoreRoute(
                    paymentRepository = paymentRepository,
                    contactService = contactService,
                    spreadsheetService = spreadsheetService,
                    innerPadding = innerPadding,
                    initialSection = moreSection
                )
            }
        }
    }
}

@Preview(name = "Phone", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(name = "Tablet", widthDp = 840, heightDp = 900, showBackground = true)
@Preview(
    name = "Dark",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AppPreview() {
    CleanRouteTheme {
        App()
    }
}
