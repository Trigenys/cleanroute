package com.trigenys.cleanroute.ui.payment

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.domain.PaymentState
import com.trigenys.cleanroute.ui.customer.CustomerDetailScreen
import com.trigenys.cleanroute.ui.customer.sampleCustomerProfile
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.Instant
import java.time.YearMonth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h1800dp")
class PaymentVisualTest {
    @Test
    fun ledgerError() {
        captureRoboImage {
            CleanRouteTheme {
                PaymentsScreen(
                    entries = emptyList(),
                    period = YearMonth.of(2026, 9),
                    query = "",
                    loading = false,
                    errorMessage = "Impossible de charger les impayés.",
                    innerPadding = PaddingValues(),
                    onQueryChange = {},
                    onPreviousPeriod = {},
                    onNextPeriod = {},
                    onRecordPayment = {},
                    onRemindPayment = {}
                )
            }
        }
    }

    @Test
    fun unpaidLedger() {
        captureRoboImage {
            CleanRouteTheme {
                PaymentsScreen(
                    entries = sampleArrearsEntries(),
                    period = YearMonth.of(2026, 9),
                    query = "",
                    loading = false,
                    errorMessage = null,
                    innerPadding = PaddingValues(),
                    onQueryChange = {},
                    onPreviousPeriod = {},
                    onNextPeriod = {},
                    onRecordPayment = {}
                )
            }
        }
    }

    @Test
    fun paidLedger() {
        captureRoboImage {
            CleanRouteTheme {
                PaymentsScreen(
                    entries = emptyList(),
                    period = YearMonth.of(2026, 9),
                    query = "",
                    loading = false,
                    errorMessage = null,
                    innerPadding = PaddingValues(),
                    onQueryChange = {},
                    onPreviousPeriod = {},
                    onNextPeriod = {},
                    onRecordPayment = {}
                )
            }
        }
    }

    @Test
    fun reversedPaymentRemainsVisibleInCustomerHistory() {
        val base = sampleCustomerProfile()
        val payment = base.recentPayments.single().copy(
            state = PaymentState.REVERSED,
            reversedAt = Instant.parse("2026-09-03T09:00:00Z")
        )
        val profile = base.copy(
            paidThisPeriodXaf = 0,
            outstandingThisPeriodXaf = base.servicePlan.monthlyFeeXaf,
            recentPayments = listOf(payment)
        )

        captureRoboImage {
            CleanRouteTheme {
                CustomerDetailScreen(
                    profile = profile,
                    innerPadding = PaddingValues(),
                    onBack = {},
                    onEdit = {}
                )
            }
        }
    }
}
