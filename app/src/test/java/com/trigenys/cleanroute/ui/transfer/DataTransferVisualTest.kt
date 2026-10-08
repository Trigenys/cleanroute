package com.trigenys.cleanroute.ui.transfer

import androidx.compose.foundation.layout.PaddingValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.data.transfer.CustomerImportPlan
import com.trigenys.cleanroute.data.transfer.ImportIssue
import com.trigenys.cleanroute.data.transfer.ImportSummary
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h1800dp")
class DataTransferVisualTest {
    @Test
    fun excelPreview() {
        captureRoboImage {
            CleanRouteTheme {
                DataTransferScreen(
                    innerPadding = PaddingValues(),
                    plan = CustomerImportPlan(
                        sourceName = "clients-septembre.xlsx",
                        summary = ImportSummary(
                            creates = 42,
                            updates = 6,
                            unchanged = 118,
                            invalid = 2
                        ),
                        unknownHeaders = listOf("Observation"),
                        fatalIssues = emptyList(),
                        rowIssues = listOf(
                            ImportIssue(17, "Montant d’abonnement invalide.")
                        ),
                        actions = emptyList()
                    ),
                    busy = false,
                    message = null,
                    errorMessage = null,
                    onPickImport = {},
                    onApplyImport = {},
                    onExport = {}
                )
            }
        }
    }
}
