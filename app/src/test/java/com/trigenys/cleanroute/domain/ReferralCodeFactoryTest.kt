package com.trigenys.cleanroute.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferralCodeFactoryTest {
    @Test
    fun codeAndLinkComeFromConfiguration() {
        val config = RetentionProgramConfig(
            enabled = true,
            codePrefix = "route",
            referralBaseUrl = "https://example.test/join/",
            rewardRule = ReferralRewardRule(
                minCompletedCollections = 1,
                rewardLabel = "Reward"
            )
        )
        val factory = ReferralCodeFactory(config)

        val code = factory.codeFor(CustomerId("customer-1"))

        assertTrue(code.startsWith("ROUTE-"))
        assertEquals(code, factory.codeFor(CustomerId("customer-1")))
        assertEquals(
            "https://example.test/join?ref=$code",
            factory.linkFor(code)
        )
    }
}
