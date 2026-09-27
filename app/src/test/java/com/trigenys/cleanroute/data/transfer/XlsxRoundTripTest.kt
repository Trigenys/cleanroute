package com.trigenys.cleanroute.data.transfer

import androidx.room.Room
import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import com.trigenys.cleanroute.data.repository.RoomCollectionVisitRepository
import com.trigenys.cleanroute.data.repository.RoomPaymentRepository
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.RouteDayId
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.zip.ZipFile
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
class XlsxRoundTripTest {
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
    fun xlsxImportAndBusinessContinuityExportRoundTrip() = runBlocking {
        val source = File.createTempFile("cleanroute-source-", ".xlsx")
        source.outputStream().use { output ->
            SimpleXlsxWriter.write(
                output = output,
                sheets = listOf(
                    SpreadsheetSheet(
                        name = "Clients",
                        rows = sequenceOf(
                            textRow(
                                "external_id",
                                "name",
                                "phone",
                                "zone",
                                "monthly_fee_xaf",
                                "status"
                            ),
                            listOf(
                                SpreadsheetCell.Text("C-001"),
                                SpreadsheetCell.Text("Amina Demo"),
                                SpreadsheetCell.Text("+237690000001"),
                                SpreadsheetCell.Text("Bonapriso"),
                                SpreadsheetCell.Number(5_000),
                                SpreadsheetCell.Text("active")
                            )
                        )
                    )
                )
            )
        }

        val plan = service.previewImport(
            source,
            "clients.xlsx",
            Instant.parse("2026-09-27T00:00:00Z")
        )
        assertEquals(1, plan.summary.creates)
        service.applyImport(plan)

        val customer = database.customerDao().getAll().single()
        RoomPaymentRepository(database).upsert(
            Payment(
                id = PaymentId("payment-1"),
                customerId = CustomerId(customer.id),
                servicePeriod = YearMonth.of(2026, 9),
                amountXaf = 5_000,
                method = PaymentMethod("cash"),
                recordedAt = Instant.parse("2026-09-27T10:00:00Z")
            )
        )
        RoomCollectionVisitRepository(database).upsert(
            CollectionVisit(
                id = CollectionVisitId("visit-1"),
                routeDayId = RouteDayId("route-1"),
                customerId = CustomerId(customer.id),
                scheduledDate = LocalDate.of(2026, 9, 27),
                status = CollectionVisitStatus.COLLECTED,
                statusChangedAt = Instant.parse("2026-09-27T08:30:00Z"),
                revision = 1
            )
        )

        val bytes = ByteArrayOutputStream()
        service.exportXlsx(bytes)

        val exported = File.createTempFile("cleanroute-export-", ".xlsx")
        exported.writeBytes(bytes.toByteArray())

        val firstSheetRows = mutableListOf<List<String>>()
        XlsxRows.readFirstSheet(exported) { _, row -> firstSheetRows += row }

        assertEquals("external_id", firstSheetRows.first().first())
        assertEquals("C-001", firstSheetRows[1][0])
        assertEquals("Amina Demo", firstSheetRows[1][1])

        ZipFile(exported).use { zip ->
            val paymentsXml = zip.getInputStream(
                requireNotNull(zip.getEntry("xl/worksheets/sheet2.xml"))
            ).bufferedReader().readText()
            val collectionsXml = zip.getInputStream(
                requireNotNull(zip.getEntry("xl/worksheets/sheet3.xml"))
            ).bufferedReader().readText()

            assertTrue(paymentsXml.contains("payment-1"))
            assertTrue(collectionsXml.contains("visit-1"))
        }
    }

    private fun textRow(vararg values: String): List<SpreadsheetCell> =
        values.map(SpreadsheetCell::Text)
}
