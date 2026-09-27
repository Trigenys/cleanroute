package com.trigenys.cleanroute.ui.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.domain.DashboardMetric
import com.trigenys.cleanroute.domain.OwnerDashboardSnapshot
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class DashboardVisualTest {
    @Test
    fun ownerDashboard() {
        captureRoboImage {
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
    }

    @Test
    fun arrearsDrillDown() {
        captureRoboImage {
            CleanRouteTheme {
                DashboardDetailScreen(
                    snapshot = sampleDashboardSnapshot(),
                    metric = DashboardMetric.ARREARS,
                    innerPadding = PaddingValues(),
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun firstUseExplainsNextAction() {
        captureRoboImage {
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
    }
}
