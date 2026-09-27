package com.trigenys.cleanroute.data.local

import androidx.room.Room
import com.trigenys.cleanroute.data.repository.RoomContactActionRepository
import com.trigenys.cleanroute.domain.ContactChannel
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.SyncOperationKind
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactActionRepositoryTest {
    private lateinit var database: CleanRouteDatabase
    private lateinit var repository: RoomContactActionRepository

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            CleanRouteDatabase::class.java
        ).build()
        repository = RoomContactActionRepository(database)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun explicitContactActionIsPersistedAndQueuedForSync() = runBlocking {
        val action = repository.record(
            customerId = CustomerId("customer-1"),
            channel = ContactChannel.WHATSAPP,
            at = Instant.parse("2026-09-27T11:00:00Z")
        )

        assertEquals(ContactChannel.WHATSAPP, action.channel)
        assertEquals(1, database.contactActionDao().count())

        val outbox = database.outboxDao().getAll()
        assertEquals(1, outbox.size)
        assertEquals(
            SyncOperationKind.RECORD_CONTACT_ACTION.name,
            outbox.single().kind
        )
        assertEquals(action.id.value, outbox.single().aggregateId)
    }
}
