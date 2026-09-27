package com.trigenys.cleanroute.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
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
