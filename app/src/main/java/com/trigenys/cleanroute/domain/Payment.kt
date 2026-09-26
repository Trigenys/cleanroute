package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.YearMonth

data class PaymentMethod(val code: String) {
    init {
        require(code.isNotBlank()) { "Payment method code must not be blank" }
    }
}

enum class PaymentState {
    RECORDED,
    REVERSED
}

data class Payment(
    val id: PaymentId,
    val customerId: CustomerId,
    val servicePeriod: YearMonth,
    val amountXaf: Long,
    val method: PaymentMethod,
    val recordedAt: Instant,
    val state: PaymentState = PaymentState.RECORDED,
    val reversedAt: Instant? = null
) {
    init {
        require(amountXaf > 0) { "Payment amount must be positive" }
        require(
            (state == PaymentState.RECORDED && reversedAt == null) ||
                (state == PaymentState.REVERSED && reversedAt != null)
        ) {
            "Payment reversal state and timestamp must agree"
        }
    }

    fun reverse(at: Instant): Payment {
        if (state == PaymentState.REVERSED) return this
        require(!at.isBefore(recordedAt)) {
            "Payment cannot be reversed before it was recorded"
        }
        return copy(state = PaymentState.REVERSED, reversedAt = at)
    }
}

object PaymentLedger {
    fun paidAmountXaf(
        customerId: CustomerId,
        servicePeriod: YearMonth,
        payments: Iterable<Payment>
    ): Long = payments
        .asSequence()
        .filter { it.customerId == customerId }
        .filter { it.servicePeriod == servicePeriod }
        .filter { it.state == PaymentState.RECORDED }
        .sumOf { it.amountXaf }

    fun outstandingAmountXaf(
        customerId: CustomerId,
        servicePeriod: YearMonth,
        expectedAmountXaf: Long,
        payments: Iterable<Payment>
    ): Long {
        require(expectedAmountXaf >= 0) { "Expected amount must not be negative" }
        return (expectedAmountXaf - paidAmountXaf(customerId, servicePeriod, payments))
            .coerceAtLeast(0)
    }
}
