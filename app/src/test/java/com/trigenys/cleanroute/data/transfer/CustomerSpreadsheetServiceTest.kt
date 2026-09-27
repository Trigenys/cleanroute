package com.trigenys.cleanroute.data.transfer

import androidx.room.Room
import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
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
class CustomerSpreadsheetServiceTest {
    private val application
        get() = RuntimeEnvironment.getApplication()

    private lateinit var database: CleanRouteDatabase
    private lateinit var service: CustomerSpreadsheetService

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            application,
            CleanRouteDatabase::class.java
        ).build()
        service = CustomerSpreadsheetService(database)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun csvImportIsPreviewedAndIdempotent() = runBlocking {
        val file = copyFixture("fixtures/customers.csv", ".csv")
        val now = Instant.parse("2026-09-27T00:00:00Z")

        val first = service.previewImport(file, "clients.csv", now)

        assertEquals(4, first.summary.creates)
        assertEquals(0, first.summary.invalid)
        assertTrue(first.unknownHeaders.contains("service_day"))
        assertTrue(first.unknownHeaders.contains("notes"))

        service.applyImport(first)
        service.applyImport(first)

        assertEquals(4, database.customerDao().getAll().size)
        assertEquals(4, database.outboxDao().count())

        val second = service.previewImport(
            file = file,
            displayName = "clients.csv",
            now = now.plusSeconds(60)
        )

        assertEquals(0, second.summary.creates)
        assertEquals(0, second.summary.updates)
        assertEquals(4, second.summary.unchanged)
    }


    @Test
    fun canonicalReferenceWorkbookFirstSheetIsImportableIdempotentAndExportable() = runBlocking {
        val file = copyFixture(
            "fixtures/cleanroute-reference-clients-v1.csv",
            ".csv"
        )
        val now = Instant.parse("2026-09-27T00:00:00Z")

        val first = service.previewImport(
            file = file,
            displayName = "CleanRoute_Reference_Workbook_v1.csv",
            now = now
        )

        assertTrue(first.fatalIssues.isEmpty())
        assertTrue(first.unknownHeaders.isEmpty())
        assertEquals(40, first.summary.creates)
        assertEquals(0, first.summary.updates)
        assertEquals(0, first.summary.unchanged)
        assertEquals(0, first.summary.invalid)

        service.applyImport(first)

        assertEquals(40, database.customerDao().getAll().size)
        assertEquals(40, database.outboxDao().count())

        val retry = service.previewImport(
            file = file,
            displayName = "CleanRoute_Reference_Workbook_v1.csv",
            now = now.plusSeconds(60)
        )

        assertEquals(0, retry.summary.creates)
        assertEquals(0, retry.summary.updates)
        assertEquals(40, retry.summary.unchanged)
        assertEquals(0, retry.summary.invalid)

        val output = ByteArrayOutputStream()
        service.exportXlsx(output)
        val exported = File.createTempFile("cleanroute-reference-export-", ".xlsx")
        exported.writeBytes(output.toByteArray())

        val roundTrip = service.previewImport(
            file = exported,
            displayName = "cleanroute-export.xlsx",
            now = now.plusSeconds(120)
        )

        assertTrue(roundTrip.fatalIssues.isEmpty())
        assertEquals(0, roundTrip.summary.creates)
        assertEquals(0, roundTrip.summary.updates)
        assertEquals(40, roundTrip.summary.unchanged)
        assertEquals(0, roundTrip.summary.invalid)
        assertTrue(roundTrip.unknownHeaders.contains("created_at"))
        assertTrue(roundTrip.unknownHeaders.contains("updated_at"))
    }

    @Test
    fun missingRequiredHeadersAreActionable() = runBlocking {
        val file = File.createTempFile("cleanroute-invalid-", ".csv")
        file.writeText("nom,telephone\nAmina,+237690000001\n")

        val plan = service.previewImport(
            file = file,
            displayName = "incomplet.csv",
            now = Instant.parse("2026-09-27T00:00:00Z")
        )

        assertTrue(plan.fatalIssues.single().contains("zone/quartier"))
        assertTrue(plan.fatalIssues.single().contains("montant/abonnement"))
        assertTrue(!plan.canApply)
    }

    private fun copyFixture(resource: String, suffix: String): File {
        val file = File.createTempFile("cleanroute-fixture-", suffix)
        val input = requireNotNull(javaClass.classLoader?.getResourceAsStream(resource))
        input.use { source ->
            file.outputStream().use { target ->
                source.copyTo(target)
            }
        }
        return file
    }
}
