package com.trigenys.cleanroute.ui.transfer

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.trigenys.cleanroute.data.transfer.CustomerImportPlan
import com.trigenys.cleanroute.data.transfer.CustomerSpreadsheetService
import com.trigenys.cleanroute.data.transfer.ImportApplyResult
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DataTransferRoute(
    service: CustomerSpreadsheetService,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var plan by remember { mutableStateOf<CustomerImportPlan?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(XLSX_MIME)
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            message = null
            errorMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: error("Impossible d’ouvrir le fichier de destination.")
                    output.use { service.exportXlsx(it) }
                }
            }.onSuccess {
                message = "Export Excel terminé."
            }.onFailure {
                errorMessage =
                    "Export impossible. Vérifiez l’espace disponible ou choisissez un autre emplacement, puis réessayez."
            }
            busy = false
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            message = null
            errorMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val displayName = displayName(context.contentResolver, uri)
                        ?: "import.xlsx"
                    val suffix = when {
                        displayName.endsWith(".csv", ignoreCase = true) -> ".csv"
                        displayName.endsWith(".xls", ignoreCase = true) -> ".xls"
                        else -> ".xlsx"
                    }
                    val temp = File.createTempFile("cleanroute-import-", suffix, context.cacheDir)
                    try {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("Impossible d’ouvrir le fichier sélectionné.")
                        input.use { source ->
                            temp.outputStream().buffered().use { target ->
                                source.copyTo(target)
                            }
                        }
                        service.previewImport(
                            file = temp,
                            displayName = displayName,
                            now = Instant.now()
                        )
                    } finally {
                        temp.delete()
                    }
                }
            }.onSuccess { preview ->
                plan = preview
            }.onFailure {
                errorMessage =
                    "Import impossible. Vérifiez le fichier et l’espace disponible, puis réessayez."
            }
            busy = false
        }
    }

    DataTransferScreen(
        innerPadding = innerPadding,
        plan = plan,
        busy = busy,
        message = message,
        errorMessage = errorMessage,
        onPickImport = {
            importLauncher.launch(
                arrayOf(
                    XLSX_MIME,
                    "text/csv",
                    "text/comma-separated-values",
                    "application/vnd.ms-excel"
                )
            )
        },
        onApplyImport = {
            val currentPlan = plan
            if (currentPlan != null) {
                scope.launch {
                busy = true
                message = null
                errorMessage = null
                runCatching {
                    withContext(Dispatchers.IO) {
                        service.applyImport(currentPlan)
                    }
                }.onSuccess { result: ImportApplyResult ->
                    message = buildString {
                        append("Import terminé : ")
                        append(result.created).append(" ajoutés, ")
                        append(result.updated).append(" mis à jour")
                        if (result.unchanged > 0) {
                            append(", ").append(result.unchanged).append(" inchangés")
                        }
                        if (result.invalid > 0) {
                            append(", ").append(result.invalid).append(" ignorés")
                        }
                        append(".")
                    }
                    plan = null
                }.onFailure {
                    errorMessage =
                        "Import interrompu. Vérifiez l’espace disponible puis relancez l’import : les lignes déjà enregistrées ne seront pas dupliquées."
                }
                    busy = false
                }
            }
        },
        onExport = {
            exportLauncher.launch("CleanRoute-export.xlsx")
        }
    )
}

private fun displayName(
    resolver: android.content.ContentResolver,
    uri: Uri
): String? {
    resolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}

private const val XLSX_MIME =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
