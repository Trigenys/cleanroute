package com.trigenys.cleanroute.communication

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class CustomerMessageKind {
    UPCOMING_COLLECTION,
    COLLECTION_COMPLETED,
    PAYMENT_REMINDER
}

data class CustomerMessageData(
    val customerName: String,
    val collectionDate: LocalDate? = null,
    val outstandingXaf: Long? = null,
    val servicePeriod: YearMonth? = null
) {
    init {
        require(customerName.isNotBlank()) { "Customer name must not be blank" }
        require(outstandingXaf == null || outstandingXaf >= 0) {
            "Outstanding amount must not be negative"
        }
    }
}

interface CustomerMessageTemplateCatalog {
    fun render(
        kind: CustomerMessageKind,
        data: CustomerMessageData
    ): String?
}

class FrenchCustomerMessageTemplates : CustomerMessageTemplateCatalog {
    override fun render(
        kind: CustomerMessageKind,
        data: CustomerMessageData
    ): String? {
        return when (kind) {
            CustomerMessageKind.UPCOMING_COLLECTION -> {
                val date = data.collectionDate ?: return null
                "Bonjour ${data.customerName}, rappel : notre équipe passera pour la collecte le ${formatDate(date)}. Merci."
            }

            CustomerMessageKind.COLLECTION_COMPLETED -> {
                val date = data.collectionDate ?: return null
                "Bonjour ${data.customerName}, votre collecte du ${formatDate(date)} a bien été effectuée. Merci."
            }

            CustomerMessageKind.PAYMENT_REMINDER -> {
                val amount = data.outstandingXaf ?: return null
                val period = data.servicePeriod ?: return null
                "Bonjour ${data.customerName}, rappel : il reste ${formatXaf(amount)} à régler pour $period. Merci."
            }
        }
    }

    private fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH))

    private fun formatXaf(amount: Long): String =
        NumberFormat.getIntegerInstance(Locale.FRENCH).format(amount) + " F CFA"
}
