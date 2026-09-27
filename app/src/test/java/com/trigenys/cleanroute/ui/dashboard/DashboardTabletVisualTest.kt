package com.trigenys.cleanroute.ui.dashboard

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
@Config(sdk = [35], qualifiers = "w840dp-h900dp")
class DashboardTabletVisualTest {
    @Test
    fun ownerDashboardTablet() {
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
}
