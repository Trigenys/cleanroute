package com.trigenys.cleanroute.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReferralRewardPolicyTest {
    private val policy = ReferralRewardPolicy(
        ReferralRewardRule(
            minCompletedCollections = 2,
            rewardLabel = "Avantage test"
        )
    )

    private val referral = Referral(
        id = ReferralId("referral-1"),
        referrerCustomerId = CustomerId("customer-a"),
        referredCustomerId = CustomerId("customer-b"),
        referralCode = "CR-TEST",
        rewardStatus = ReferralRewardStatus.PENDING,
        attributedAt = Instant.parse("2026-09-27T08:00:00Z")
    )

    @Test
    fun qualificationUsesConfiguredThreshold() {
        val tooEarly = policy.qualify(
            referral = referral,
            completedCollections = 1,
            at = Instant.parse("2026-09-28T08:00:00Z")
        )
        assertEquals(ReferralRewardStatus.PENDING, tooEarly.rewardStatus)
        assertNull(tooEarly.qualifiedAt)

        val eligible = policy.qualify(
            referral = referral,
            completedCollections = 2,
            at = Instant.parse("2026-09-29T08:00:00Z")
        )
        assertEquals(ReferralRewardStatus.ELIGIBLE, eligible.rewardStatus)
    }

    @Test
    fun awardingAnAlreadyAwardedReferralIsIdempotent() {
        val eligible = policy.qualify(
            referral = referral,
            completedCollections = 2,
            at = Instant.parse("2026-09-29T08:00:00Z")
        )
        val first = policy.award(
            eligible,
            Instant.parse("2026-09-30T08:00:00Z")
        )
        val replay = policy.award(
            first,
            Instant.parse("2026-10-01T08:00:00Z")
        )

        assertEquals(first, replay)
        assertEquals(ReferralRewardStatus.AWARDED, replay.rewardStatus)
    }
}
