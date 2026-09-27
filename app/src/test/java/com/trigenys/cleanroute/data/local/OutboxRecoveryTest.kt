package com.trigenys.cleanroute.data.local

import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.repository.RoomPaymentRepository
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentMethods
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class OutboxRecoveryTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-outbox-recovery.db"
    private lateinit var database: CleanRouteDatabase

    @Before
    fun before() {
        application.deleteDatabase(databaseName)
        database = CleanRouteDatabase.open(application, databaseName)
    }

    @After
    fun after() {
        runCatching { database.close() }
        application.deleteDatabase(databaseName)
    }

    @Test
    fun interruptedFutureSyncKeepsPendingOperationsUntilExplicitAck() = runBlocking {
        val customers = RoomCustomerRepository(database)
        val customerId = customers.saveDraft(
            draft = CustomerDraft(
                name = "Sync Test",
                phone = "+237690000002",
                zoneName = "Makepe",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            now = Instant.parse("2026-09-27T08:00:00Z")
        )

        RoomPaymentRepository(database).record(
            draft = PaymentDraft(
                submissionId = "outbox-payment-001",
                customerId = customerId,
                servicePeriod = YearMonth.of(2026, 9),
                amountXaf = 2_500,
                method = PaymentMethods.CASH.method
            ),
            at = Instant.parse("2026-09-27T09:00:00Z")
        )

        val beforeInterruption = database.outboxDao().getAll()
        assertTrue(beforeInterruption.size >= 2)

        reopen()

        val afterInterruption = database.outboxDao().getAll()
        assertEquals(beforeInterruption.map { it.id }, afterInterruption.map { it.id })

        val acknowledgedId = afterInterruption.first().id
        assertEquals(
            1,
            database.outboxDao().deleteAcknowledged(listOf(acknowledgedId))
        )

        reopen()

        val remaining = database.outboxDao().getAll()
        assertFalse(remaining.any { it.id == acknowledgedId })
        assertEquals(afterInterruption.size - 1, remaining.size)
        assertTrue(
            remaining.all { pending ->
                beforeInterruption.any { it.id == pending.id }
            }
        )
    }

    private fun reopen() {
        database.close()
        database = CleanRouteDatabase.open(application, databaseName)
    }
}
