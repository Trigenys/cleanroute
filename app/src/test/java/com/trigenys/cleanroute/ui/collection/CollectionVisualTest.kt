package com.trigenys.cleanroute.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.domain.ZoneWorkload
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class CollectionVisualTest {
    @Test
    fun zoneReadySelection() {
        captureRoboImage {
            CleanRouteTheme {
                ZoneWorkloadScreen(
                    workloads = listOf(
                        ZoneWorkload(
                            zoneId = ZoneId("zone-1"),
                            zoneName = "Bonamoussadi",
                            totalStops = 12,
                            completedStops = 0
                        ),
                        ZoneWorkload(
                            zoneId = ZoneId("zone-2"),
                            zoneName = "Makepe",
                            totalStops = 8,
                            completedStops = 3
                        )
                    ),
                    loading = false,
                    errorMessage = null,
                    innerPadding = PaddingValues(),
                    onZoneSelected = {},
                    onOpenClients = {},
                    onOpenExcel = {}
                )
            }
        }
    }

    @Test
    fun zoneLoadError() {
        captureRoboImage {
            CleanRouteTheme {
                ZoneWorkloadScreen(
                    workloads = emptyList(),
                    loading = false,
                    errorMessage = "Impossible de charger les zones.",
                    innerPadding = PaddingValues(),
                    onZoneSelected = {}
                )
            }
        }
    }

    @Test
    fun routeReadyConfiguration() {
        captureRoboImage {
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
                    onCall = {},
                    onWhatsApp = {},
                    tourStarted = false,
                    onStartTour = {}
                )
            }
        }
    }

    @Test
    fun routeWithExpandedStopActions() {
        captureRoboImage {
            CleanRouteTheme {
                DailyRouteScreen(
                    route = sampleDailyRoute(),
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
    }
}
