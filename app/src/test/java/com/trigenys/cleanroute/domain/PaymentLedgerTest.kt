package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PaymentLedgerTest {
    private val customerId = CustomerId("customer-1")
    private val period = YearMonth.of(2026, 9)

    @Test
    fun partialPaymentsReduceOutstandingAmount() {
        val payments = listOf(
            payment("p1", 3_000),
            payment("p2", 2_000)
        )

        assertEquals(
            5_000L,
            PaymentLedger.outstandingAmountXaf(
                customerId = customerId,
                servicePeriod = period,
                expectedAmountXaf = 10_000,
                payments = payments
            )
        )
    }

    @Test
    fun reversedPaymentNoLongerCountsAndReverseIsIdempotent() {
        val recorded = payment("p1", 5_000)
        val reversedAt = Instant.parse("2026-09-27T10:00:00Z")
        val reversed = recorded.reverse(reversedAt)

        assertSame(reversed, reversed.reverse(reversedAt.plusSeconds(30)))
        assertEquals(
            10_000L,
            PaymentLedger.outstandingAmountXaf(
                customerId = customerId,
                servicePeriod = period,
                expectedAmountXaf = 10_000,
                payments = listOf(reversed)
            )
        )
    }

    @Test
    fun overpaymentNeverCreatesNegativeOutstandingBalance() {
        assertEquals(
            0L,
            PaymentLedger.outstandingAmountXaf(
                customerId = customerId,
                servicePeriod = period,
                expectedAmountXaf = 10_000,
                payments = listOf(payment("p1", 12_000))
            )
        )
    }

    private fun payment(id: String, amountXaf: Long) = Payment(
        id = PaymentId(id),
        customerId = customerId,
        servicePeriod = period,
        amountXaf = amountXaf,
        method = PaymentMethod("cash"),
        recordedAt = Instant.parse("2026-09-26T10:00:00Z")
    )
}
