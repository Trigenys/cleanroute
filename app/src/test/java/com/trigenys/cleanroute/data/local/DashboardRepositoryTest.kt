package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomCollectionWorkflowRepository
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.data.repository.RoomDashboardRepository
import com.trigenys.cleanroute.data.repository.RoomPaymentRepository
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentMethods
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DashboardRepositoryTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private lateinit var database: CleanRouteDatabase
    private lateinit var customers: RoomCustomerRepository
    private lateinit var payments: RoomPaymentRepository
    private lateinit var collections: RoomCollectionWorkflowRepository
    private lateinit var dashboard: RoomDashboardRepository

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            application,
            CleanRouteDatabase::class.java
        ).build()
        customers = RoomCustomerRepository(database)
        payments = RoomPaymentRepository(database)
        collections = RoomCollectionWorkflowRepository(database)
        dashboard = RoomDashboardRepository(database)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun snapshotIsComputedOnlyFromPersistedLocalRecords() = runBlocking {
        val date = LocalDate.of(2026, 9, 27)
        val zone = ZoneId.of("Africa/Douala")
        val firstCustomer = customers.saveDraft(
            CustomerDraft(
                name = "Amina Demo",
                phone = "+237690000001",
                zoneName = "Bonapriso",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            Instant.parse("2026-09-27T07:00:00Z")
        )
        customers.saveDraft(
            CustomerDraft(
                name = "Boris Demo",
                phone = "+237690000002",
                zoneName = "Bonapriso",
                monthlyFeeXaf = 5_000,
                status = CustomerStatus.ACTIVE
            ),
            Instant.parse("2026-09-27T07:05:00Z")
        )

        payments.record(
            draft = PaymentDraft(
                submissionId = "dashboard-payment",
                customerId = firstCustomer,
                servicePeriod = YearMonth.of(2026, 9),
                amountXaf = 5_000,
                method = PaymentMethods.MTN_MOMO.method
            ),
            at = Instant.parse("2026-09-27T08:00:00Z")
        )

        val zoneId = customers.search("Bonapriso").first().customer.zoneId
        val route = collections.ensureRoute(date, zoneId)
        collections.recordOutcome(
            visitId = route.stops.first().visit.id,
            outcome = CollectionVisitStatus.COLLECTED,
            at = Instant.parse("2026-09-27T09:00:00Z")
        )

        val snapshot = dashboard.snapshot(date, zone)

        assertEquals(2, snapshot.activeClients.size)
        assertEquals(2, snapshot.newClients.size)
        assertEquals(2, snapshot.collectionTotal)
        assertEquals(1, snapshot.collectionCompleted)
        assertEquals(5_000L, snapshot.receiptsXaf)
        assertEquals(1, snapshot.receipts.size)
        assertEquals(5_000L, snapshot.arrearsXaf)
        assertEquals(1, snapshot.arrears.size)
        assertEquals(1, snapshot.zones.size)
        assertEquals(2, snapshot.zones.single().totalStops)
        assertEquals(1, snapshot.zones.single().completedStops)
    }
}
