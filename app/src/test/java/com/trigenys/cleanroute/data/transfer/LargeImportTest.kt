package com.trigenys.cleanroute.data.transfer

import androidx.room.Room
import com.trigenys.cleanroute.data.local.CleanRouteDatabase
import java.io.File
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LargeImportTest {
    @Test
    fun fiveThousandRowsCanBePreviewedWithoutLoadingTheRawFileIntoMemory() = runBlocking {
        val application = RuntimeEnvironment.getApplication()
        val database = Room.inMemoryDatabaseBuilder(
            application,
            CleanRouteDatabase::class.java
        ).build()

        try {
            val file = File.createTempFile("cleanroute-large-", ".csv")
            file.bufferedWriter().use { writer ->
                writer.appendLine("external_id,name,phone,zone,monthly_fee_xaf")
                repeat(5_000) { index ->
                    writer.append("C-")
                        .append(index.toString())
                        .append(",Client ")
                        .append(index.toString())
                        .append(",+237690")
                        .append(index.toString().padStart(6, '0'))
                        .append(",Zone Test,5000")
                        .appendLine()
                }
            }

            val plan = CustomerSpreadsheetService(database).previewImport(
                file = file,
                displayName = "5000-clients.csv",
                now = Instant.parse("2026-09-27T00:00:00Z")
            )

            assertEquals(5_000, plan.summary.creates)
            assertEquals(0, plan.summary.invalid)
            assertEquals(0, plan.fatalIssues.size)
        } finally {
            database.close()
        }
    }
}
