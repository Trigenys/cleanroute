package com.trigenys.cleanroute.ui.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.data.transfer.CustomerImportPlan
import com.trigenys.cleanroute.data.transfer.ImportIssue
import com.trigenys.cleanroute.data.transfer.ImportSummary
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.KpiTile
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme

@Composable
fun DataTransferScreen(
    innerPadding: PaddingValues,
    plan: CustomerImportPlan?,
    busy: Boolean,
    message: String?,
    errorMessage: String?,
    onPickImport: () -> Unit,
    onApplyImport: () -> Unit,
    onExport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Données & Excel",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Gardez Excel comme filet de sécurité sans gérer le quotidien dedans.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (busy) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        message?.let { success ->
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = success,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        errorMessage?.let { error ->
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Text("Importer des clients", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Fichiers .xlsx ou .csv. La première feuille est analysée avant toute modification.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onPickImport,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.FileUpload, contentDescription = null)
                    Text(" Choisir un fichier")
                }
            }
        }

        plan?.let { preview ->
            item {
                ImportPreviewCard(
                    plan = preview,
                    busy = busy,
                    onApplyImport = onApplyImport
                )
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Text("Exporter une sauvegarde", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Un fichier .xlsx avec 3 feuilles : Clients, Paiements et Collectes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onExport,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null)
                    Text(" Exporter vers Excel")
                }
            }
        }
    }
}

@Composable
private fun ImportPreviewCard(
    plan: CustomerImportPlan,
    busy: Boolean,
    onApplyImport: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text("Aperçu avant import", style = MaterialTheme.typography.titleLarge)
        Text(
            plan.sourceName,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTile(
                label = "À ajouter",
                value = plan.summary.creates.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                label = "À mettre à jour",
                value = plan.summary.updates.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTile(
                label = "Inchangés",
                value = plan.summary.unchanged.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                label = "Ignorés",
                value = plan.summary.invalid.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        if (plan.unknownHeaders.isNotEmpty()) {
            Text(
                text = "Colonnes non utilisées : " + plan.unknownHeaders.joinToString(", "),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        plan.fatalIssues.forEach { issue ->
            Text(
                text = issue,
                color = MaterialTheme.colorScheme.error
            )
        }

        plan.rowIssues.take(5).forEach { issue ->
            Text(
                text = buildString {
                    issue.rowNumber?.let { append("Ligne ").append(it).append(" : ") }
                    append(issue.message)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (plan.rowIssues.size > 5) {
            Text(
                text = "+ ${plan.rowIssues.size - 5} autres erreurs de ligne",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = onApplyImport,
            enabled = plan.canApply && !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmer l’import")
        }

        if (!plan.canApply && plan.fatalIssues.isEmpty()) {
            Text(
                text = "Aucune modification à appliquer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(name = "Excel transfer", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DataTransferPreview() {
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
                    ImportIssue(17, "Montant d’abonnement invalide."),
                    ImportIssue(39, "Zone/quartier manquant.")
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
