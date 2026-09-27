package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomCollectionWorkflowRepository
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CollectionWorkflowRepositoryTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private lateinit var database: CleanRouteDatabase
    private lateinit var customerRepository: RoomCustomerRepository
    private lateinit var workflowRepository: RoomCollectionWorkflowRepository

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            application,
            CleanRouteDatabase::class.java
        ).build()
        customerRepository = RoomCustomerRepository(database)
        workflowRepository = RoomCollectionWorkflowRepository(database)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun routeCreationAndOutcomeCorrectionsAreIdempotentAndAudited() = runBlocking {
        val now = Instant.parse("2026-09-27T07:00:00Z")
        saveCustomer("Amina Demo", CustomerStatus.ACTIVE, now)
        saveCustomer("Boris Demo", CustomerStatus.ACTIVE, now.plusSeconds(1))
        saveCustomer("Suspended Demo", CustomerStatus.SUSPENDED, now.plusSeconds(2))

        val directory = customerRepository.search("Bonapriso")
        val active = directory.filter { it.customer.status == CustomerStatus.ACTIVE }
        assertEquals(2, active.size)

        val zoneId = active.first().customer.zoneId
        val date = LocalDate.of(2026, 9, 27)

        val beforeRoute = workflowRepository.zoneWorkloads(date).single()
        assertEquals(2, beforeRoute.totalStops)
        assertEquals(0, beforeRoute.completedStops)

        val first = workflowRepository.ensureRoute(date, zoneId)
        val replay = workflowRepository.ensureRoute(date, zoneId)

        assertEquals(first.routeDayId, replay.routeDayId)
        assertEquals(2, replay.stops.size)

        val outboxBeforeOutcome = database.outboxDao().count()
        val visitId = first.stops.first().visit.id

        workflowRepository.recordOutcome(
            visitId = visitId,
            outcome = CollectionVisitStatus.COLLECTED,
            at = now.plusSeconds(60)
        )
        workflowRepository.recordOutcome(
            visitId = visitId,
            outcome = CollectionVisitStatus.COLLECTED,
            at = now.plusSeconds(120)
        )

        assertEquals(1, workflowRepository.revisions(visitId).size)
        assertEquals(outboxBeforeOutcome + 1, database.outboxDao().count())

        workflowRepository.recordOutcome(
            visitId = visitId,
            outcome = CollectionVisitStatus.ABSENT,
            at = now.plusSeconds(180)
        )

        val revisions = workflowRepository.revisions(visitId)
        assertEquals(2, revisions.size)
        assertEquals(CollectionVisitStatus.COLLECTED, revisions[0].status)
        assertEquals(CollectionVisitStatus.ABSENT, revisions[1].status)
        assertEquals(outboxBeforeOutcome + 2, database.outboxDao().count())

        val routeAfterCorrection = workflowRepository.getRoute(date, zoneId)
        assertNotNull(routeAfterCorrection)
        assertEquals(
            CollectionVisitStatus.ABSENT,
            routeAfterCorrection?.stops?.first { it.visit.id == visitId }?.visit?.status
        )

        val workloadAfter = workflowRepository.zoneWorkloads(date).single()
        assertEquals(1, workloadAfter.completedStops)
        assertEquals(1, workloadAfter.remainingStops)
    }

    private suspend fun saveCustomer(
        name: String,
        status: CustomerStatus,
        now: Instant
    ) {
        customerRepository.saveDraft(
            draft = CustomerDraft(
                name = name,
                phone = "+237690000001",
                zoneName = "Bonapriso",
                addressLabel = "Repère test",
                monthlyFeeXaf = 5_000,
                status = status
            ),
            now = now
        )
    }
}
