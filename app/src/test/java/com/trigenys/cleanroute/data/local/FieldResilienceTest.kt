package com.trigenys.cleanroute.data.local

import com.trigenys.cleanroute.data.repository.RoomCollectionWorkflowRepository
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.repository.RoomPaymentRepository
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentMethods
import java.time.Instant
import java.time.LocalDate
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
class FieldResilienceTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-field-resilience.db"
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
    fun committedCollectionAndPaymentSurviveRestartAndRetry() = runBlocking {
        val now = Instant.parse("2026-09-27T08:00:00Z")
        val date = LocalDate.of(2026, 9, 27)
        val period = YearMonth.of(2026, 9)

        val customers = RoomCustomerRepository(database)
        val customerId = customers.saveDraft(
            draft = CustomerDraft(
                name = "Amina Résilience",
                phone = "+237690000001",
                zoneName = "Bonapriso",
                addressLabel = "Carrefour test",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            now = now
        )
        val zoneId = customers.search("Bonapriso").single().customer.zoneId

        val collections = RoomCollectionWorkflowRepository(database)
        val route = collections.ensureRoute(date, zoneId)
        val visitId = route.stops.single().visit.id
        collections.recordOutcome(
            visitId = visitId,
            outcome = CollectionVisitStatus.COLLECTED,
            at = now.plusSeconds(60)
        )

        val paymentDraft = PaymentDraft(
            submissionId = "restart-payment-001",
            customerId = customerId,
            servicePeriod = period,
            amountXaf = 5_000,
            method = PaymentMethods.MTN_MOMO.method
        )
        val payments = RoomPaymentRepository(database)
        val payment = payments.record(
            draft = paymentDraft,
            at = now.plusSeconds(120)
        )

        val outboxBeforeRestart = database.outboxDao().count()
        reopen()

        val collectionsAfterRestart = RoomCollectionWorkflowRepository(database)
        val paymentsAfterRestart = RoomPaymentRepository(database)

        val persistedRoute = requireNotNull(
            collectionsAfterRestart.getRoute(date, zoneId)
        )
        assertEquals(
            CollectionVisitStatus.COLLECTED,
            persistedRoute.stops.single().visit.status
        )
        assertEquals(1, collectionsAfterRestart.revisions(visitId).size)

        val persistedPayment = paymentsAfterRestart.get(payment.id)
        assertEquals(payment, persistedPayment)
        assertEquals(outboxBeforeRestart, database.outboxDao().count())

        val replayedPayment = paymentsAfterRestart.record(
            draft = paymentDraft,
            at = now.plusSeconds(300)
        )
        collectionsAfterRestart.recordOutcome(
            visitId = visitId,
            outcome = CollectionVisitStatus.COLLECTED,
            at = now.plusSeconds(360)
        )

        assertEquals(payment, replayedPayment)
        assertEquals(1, collectionsAfterRestart.revisions(visitId).size)
        assertEquals(outboxBeforeRestart, database.outboxDao().count())

        val finalRoute = requireNotNull(
            collectionsAfterRestart.getRoute(date, zoneId)
        )
        assertTrue(
            finalRoute.stops.all {
                it.visit.status != CollectionVisitStatus.SCHEDULED
            }
        )
    }

    private fun reopen() {
        database.close()
        database = CleanRouteDatabase.open(application, databaseName)
    }
}
