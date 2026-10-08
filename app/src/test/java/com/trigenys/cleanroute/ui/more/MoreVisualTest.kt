package com.trigenys.cleanroute.ui.more

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.YearMonth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h1800dp")
class MoreVisualTest {
    @Test
    fun paymentsTab() {
        captureRoboImage {
            CleanRouteTheme {
                MoreRoute(
                    paymentRepository = null,
                    contactService = null,
                    spreadsheetService = null,
                    innerPadding = PaddingValues(),
                    initialSection = MoreSection.PAYMENTS,
                    emptyLedgerPeriod = YearMonth.of(2026, 9)
                )
            }
        }
    }

    @Test
    fun excelTab() {
        captureRoboImage {
            CleanRouteTheme {
                MoreRoute(
                    paymentRepository = null,
                    contactService = null,
                    spreadsheetService = null,
                    innerPadding = PaddingValues(),
                    initialSection = MoreSection.EXCEL
                )
            }
        }
    }
}
