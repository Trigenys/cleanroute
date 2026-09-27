package com.trigenys.cleanroute.domain

import java.time.Instant
import java.time.YearMonth

data class PaymentMethod(val code: String) {
    init {
        require(code.isNotBlank()) { "Payment method code must not be blank" }
    }
}

data class PaymentMethodOption(
    val method: PaymentMethod,
    val label: String
) {
    init {
        require(label.isNotBlank()) { "Payment method label must not be blank" }
    }
}

object PaymentMethods {
    val CASH = PaymentMethodOption(
        method = PaymentMethod("cash"),
        label = "Espèces"
    )
    val ORANGE_MONEY = PaymentMethodOption(
        method = PaymentMethod("orange_money"),
        label = "Orange Money"
    )
    val MTN_MOMO = PaymentMethodOption(
        method = PaymentMethod("mtn_momo"),
        label = "MTN MoMo"
    )

    val DEFAULT: List<PaymentMethodOption> = listOf(
        CASH,
        ORANGE_MONEY,
        MTN_MOMO
    )

    fun labelFor(method: PaymentMethod): String =
        DEFAULT.firstOrNull { it.method == method }?.label ?: method.code
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

data class PaymentDraft(
    val submissionId: String,
    val customerId: CustomerId,
    val servicePeriod: YearMonth,
    val amountXaf: Long,
    val method: PaymentMethod
) {
    init {
        require(submissionId.isNotBlank()) { "Payment submission id must not be blank" }
        require(amountXaf > 0) { "Payment amount must be positive" }
    }
}

data class ArrearsEntry(
    val customerId: CustomerId,
    val customerName: String,
    val phone: String?,
    val zoneName: String,
    val monthlyFeeXaf: Long,
    val paidXaf: Long,
    val outstandingXaf: Long
) {
    init {
        require(monthlyFeeXaf >= 0) { "Monthly fee must not be negative" }
        require(paidXaf >= 0) { "Paid amount must not be negative" }
        require(outstandingXaf >= 0) { "Outstanding amount must not be negative" }
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
