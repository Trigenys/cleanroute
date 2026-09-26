package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.local.entity.ServicePlanEntity
import com.trigenys.cleanroute.data.local.entity.ZoneEntity
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.ZoneId
import java.time.Instant
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
class RoomPersistenceTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-room-test.db"

    @Before
    fun before() {
        application.deleteDatabase(databaseName)
    }

    @After
    fun after() {
        application.deleteDatabase(databaseName)
    }

    @Test
    fun customerAndOutboxSurviveDatabaseRestart() = runBlocking {
        val createdAt = Instant.parse("2026-09-26T08:00:00Z")
        val customer = Customer(
            id = CustomerId("customer-1"),
            externalId = "legacy-001",
            name = "Amina Demo",
            phone = "+237690000001",
            zoneId = ZoneId("zone-1"),
            addressLabel = "Carrefour A",
            servicePlanId = ServicePlanId("plan-1"),
            createdAt = createdAt,
            updatedAt = createdAt
        )

        openDatabase().use { database ->
            database.catalogDao().upsertZone(
                ZoneEntity(id = "zone-1", name = "Zone Nord")
            )
            database.catalogDao().upsertServicePlan(
                ServicePlanEntity(
                    id = "plan-1",
                    label = "Hebdomadaire",
                    cadence = "WEEKLY",
                    monthlyFeeXaf = 5_000
                )
            )

            val repository = RoomCustomerRepository(database)
            repository.upsert(customer)
            repository.upsert(customer)

            assertEquals(1, database.outboxDao().count())
            assertEquals(customer, repository.get(customer.id))
        }

        openDatabase().use { reopened ->
            val repository = RoomCustomerRepository(reopened)
            assertNotNull(repository.get(customer.id))
            assertEquals(1, reopened.outboxDao().count())
        }
    }

    private fun openDatabase(): CleanRouteDatabase =
        Room.databaseBuilder(
            application,
            CleanRouteDatabase::class.java,
            databaseName
        )
            .addMigrations(*CleanRouteMigrations.ALL)
            .build()
}
