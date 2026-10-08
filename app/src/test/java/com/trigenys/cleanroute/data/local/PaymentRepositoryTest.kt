package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.repository.RoomPaymentRepository
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentMethods
import com.trigenys.cleanroute.domain.PaymentState
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PaymentRepositoryTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private lateinit var database: CleanRouteDatabase
    private lateinit var customers: RoomCustomerRepository
    private lateinit var payments: RoomPaymentRepository

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            application,
            CleanRouteDatabase::class.java
        ).build()
        customers = RoomCustomerRepository(database)
        payments = RoomPaymentRepository(database)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun duplicateSubmissionIsIdempotentButNewPartialPaymentIsAllowed() = runBlocking {
        val customerId = createCustomer()
        val period = YearMonth.of(2026, 9)
        val recordedAt = Instant.parse("2026-09-27T10:00:00Z")

        val firstDraft = PaymentDraft(
            submissionId = "submit-001",
            customerId = customerId,
            servicePeriod = period,
            amountXaf = 2_500,
            method = PaymentMethods.ORANGE_MONEY.method
        )

        val first = payments.record(firstDraft, recordedAt)
        val replay = payments.record(firstDraft, recordedAt.plusSeconds(30))

        assertEquals(first, replay)
        assertEquals(1, database.paymentDao().getAllForExport().size)

        val second = payments.record(
            draft = firstDraft.copy(submissionId = "submit-002"),
            at = recordedAt.plusSeconds(60)
        )

        assertTrue(second.id != first.id)
        assertEquals(2, database.paymentDao().getAllForExport().size)
        assertEquals(0, payments.arrears(period, "").size)
    }

    @Test
    fun reversalIsAuditableAndRestoresAmountDue() = runBlocking {
        val customerId = createCustomer()
        val period = YearMonth.of(2026, 9)
        val recordedAt = Instant.parse("2026-09-27T10:00:00Z")

        val payment = payments.record(
            draft = PaymentDraft(
                submissionId = "submit-reversal",
                customerId = customerId,
                servicePeriod = period,
                amountXaf = 5_000,
                method = PaymentMethods.CASH.method
            ),
            at = recordedAt
        )

        assertEquals(0, payments.arrears(period, "").size)

        val reversed = payments.reverse(
            id = payment.id,
            at = recordedAt.plusSeconds(120)
        )
        val replay = payments.reverse(
            id = payment.id,
            at = recordedAt.plusSeconds(240)
        )

        assertEquals(PaymentState.REVERSED, reversed.state)
        assertEquals(reversed, replay)
        assertEquals(1, database.paymentDao().getAllForExport().size)

        val arrears = payments.arrears(period, "Bonapriso")
        assertEquals(1, arrears.size)
        assertEquals(5_000L, arrears.single().outstandingXaf)

        val stored = payments.get(payment.id)
        assertEquals(PaymentState.REVERSED, stored?.state)
        assertEquals(recordedAt.plusSeconds(120), stored?.reversedAt)
    }

    @Test
    fun periodSummaryCountsActiveClientsAndRecordedCollections() = runBlocking {
        val customerId = createCustomer()
        val period = YearMonth.of(2026, 9)
        val recordedAt = Instant.parse("2026-09-27T10:00:00Z")

        val payment = payments.record(
            draft = PaymentDraft(
                submissionId = "submit-summary",
                customerId = customerId,
                servicePeriod = period,
                amountXaf = 5_000,
                method = PaymentMethods.CASH.method
            ),
            at = recordedAt
        )

        val collected = payments.periodSummary(period)
        assertEquals(1, collected?.activeClients)
        assertEquals(5_000L, collected?.collectedXaf)
        assertEquals(0L, payments.periodSummary(period.plusMonths(1))?.collectedXaf)

        payments.reverse(id = payment.id, at = recordedAt.plusSeconds(60))
        assertEquals(0L, payments.periodSummary(period)?.collectedXaf)
    }

    @Test
    fun paymentMethodsComeFromDomainCatalog() {
        assertEquals(
            listOf("cash", "orange_money", "mtn_momo"),
            payments.methods.map { it.method.code }
        )
    }

    private suspend fun createCustomer() =
        customers.saveDraft(
            draft = CustomerDraft(
                name = "Amina Demo",
                phone = "+237690000001",
                zoneName = "Bonapriso",
                addressLabel = "Carrefour",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            now = Instant.parse("2026-09-27T08:00:00Z")
        )
}
