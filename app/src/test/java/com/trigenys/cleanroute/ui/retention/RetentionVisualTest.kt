package com.trigenys.cleanroute.ui.retention

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.Referral
import com.trigenys.cleanroute.domain.ReferralId
import com.trigenys.cleanroute.domain.ReferralRewardStatus
import com.trigenys.cleanroute.domain.ReferralSummary
import com.trigenys.cleanroute.domain.RetentionCustomerProfile
import com.trigenys.cleanroute.domain.RetentionIndicators
import com.trigenys.cleanroute.ui.customer.CustomerDetailScreen
import com.trigenys.cleanroute.ui.customer.sampleCustomerProfile
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.Instant
import java.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class RetentionVisualTest {
    @Test
    fun customerProfileShowsReferralStateWithoutLeaderboard() {
        val retention = RetentionCustomerProfile(
            customerId = CustomerId("customer-1"),
            referralCode = "CR-A1B2C3D4",
            referralLink = "https://example.test/join?ref=CR-A1B2C3D4",
            rewardLabel = "Avantage parrainage",
            indicators = RetentionIndicators(
                customerSince = LocalDate.of(2026, 1, 10),
                tenureDays = 260,
                completedCollectionsLast90Days = 10,
                recordedVisitsLast90Days = 12
            ),
            referredBy = null,
            referralsMade = listOf(
                ReferralSummary(
                    referral = Referral(
                        id = ReferralId("referral-1"),
                        referrerCustomerId = CustomerId("customer-1"),
                        referredCustomerId = CustomerId("customer-2"),
                        referralCode = "CR-A1B2C3D4",
                        rewardStatus = ReferralRewardStatus.ELIGIBLE,
                        attributedAt = Instant.parse("2026-09-01T08:00:00Z"),
                        qualifiedAt = Instant.parse("2026-09-27T08:00:00Z")
                    ),
                    customerName = "M. Ewane"
                )
            )
        )

        captureRoboImage {
            CleanRouteTheme {
                CustomerDetailScreen(
                    profile = sampleCustomerProfile(),
                    retentionProfile = retention,
                    innerPadding = PaddingValues(),
                    onBack = {},
                    onEdit = {}
                )
            }
        }
    }
}
