package com.trigenys.cleanroute.communication

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerMessageTemplatesTest {
    private val templates = FrenchCustomerMessageTemplates()

    @Test
    fun upcomingCollectionUsesOnlyProvidedCustomerAndDate() {
        val text = templates.render(
            CustomerMessageKind.UPCOMING_COLLECTION,
            CustomerMessageData(
                customerName = "Mme Mballa",
                collectionDate = LocalDate.of(2026, 9, 28)
            )
        )

        assertTrue(text?.contains("Mme Mballa") == true)
        assertTrue(text?.contains("28/09/2026") == true)
    }

    @Test
    fun completedCollectionUsesProvidedDate() {
        val text = templates.render(
            CustomerMessageKind.COLLECTION_COMPLETED,
            CustomerMessageData(
                customerName = "M. Ewane",
                collectionDate = LocalDate.of(2026, 9, 27)
            )
        )

        assertTrue(text?.contains("M. Ewane") == true)
        assertTrue(text?.contains("27/09/2026") == true)
        assertTrue(text?.contains("effectuée") == true)
    }

    @Test
    fun paymentReminderRequiresAmountAndPeriod() {
        val text = templates.render(
            CustomerMessageKind.PAYMENT_REMINDER,
            CustomerMessageData(
                customerName = "Mme Nguema",
                outstandingXaf = 5_000,
                servicePeriod = YearMonth.of(2026, 9)
            )
        )

        assertTrue(text?.contains("5") == true)
        assertTrue(text?.contains("000") == true)
        assertTrue(text?.contains("2026-09") == true)

        assertNull(
            templates.render(
                CustomerMessageKind.PAYMENT_REMINDER,
                CustomerMessageData(customerName = "Mme Nguema")
            )
        )
    }
}
