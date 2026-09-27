package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomCustomerRepository
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerStatus
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
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
class CustomerRepositoryTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private val databaseName = "cleanroute-customer-test.db"

    @Before
    fun before() {
        application.deleteDatabase(databaseName)
    }

    @After
    fun after() {
        application.deleteDatabase(databaseName)
    }

    @Test
    fun saveSearchAndProfileWorkOffline() = runBlocking {
        val database = Room.databaseBuilder(
            application,
            CleanRouteDatabase::class.java,
            databaseName
        ).build()

        try {
            val repository = RoomCustomerRepository(database)
            val id = repository.saveDraft(
                draft = CustomerDraft(
                    name = "Amina Demo",
                    phone = "+237690000001",
                    zoneName = "Bonapriso",
                    addressLabel = "Carrefour",
                    monthlyFeeXaf = 5_000,
                    status = CustomerStatus.ACTIVE
                ),
                now = Instant.parse("2026-09-27T00:00:00Z")
            )

            assertEquals(1, repository.search("Amina").size)
            assertEquals(1, repository.search("690000001").size)
            assertEquals(1, repository.search("Bonapriso").size)

            val profile = repository.getProfile(
                id = id,
                servicePeriod = YearMonth.of(2026, 9),
                today = LocalDate.of(2026, 9, 27)
            )

            assertNotNull(profile)
            assertEquals("Bonapriso", profile?.zone?.name)
            assertEquals(5_000L, profile?.outstandingThisPeriodXaf)
            assertEquals(1, database.outboxDao().count())
        } finally {
            database.close()
        }
    }
}
