package com.trigenys.cleanroute.domain

import org.junit.Test

class CustomerDraftTest {
    @Test(expected = IllegalArgumentException::class)
    fun nameIsRequired() {
        CustomerDraft(
            name = " ",
            zoneName = "Bonapriso",
            monthlyFeeXaf = 5_000
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun zoneIsRequired() {
        CustomerDraft(
            name = "Amina Demo",
            zoneName = " ",
            monthlyFeeXaf = 5_000
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun monthlyFeeCannotBeNegative() {
        CustomerDraft(
            name = "Amina Demo",
            zoneName = "Bonapriso",
            monthlyFeeXaf = -1
        )
    }
}
