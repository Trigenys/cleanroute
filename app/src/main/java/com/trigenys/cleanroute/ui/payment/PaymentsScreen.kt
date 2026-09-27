package com.trigenys.cleanroute.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.time.YearMonth

@Composable
fun PaymentsScreen(
    entries: List<ArrearsEntry>,
    period: YearMonth,
    query: String,
    loading: Boolean,
    errorMessage: String?,
    innerPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onRecordPayment: (ArrearsEntry) -> Unit
) {
    val totalOutstanding = entries.sumOf { it.outstandingXaf }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Paiements & impayés", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Les paiements sont enregistrés hors connexion.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onPreviousPeriod) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mois précédent")
                }
                Text(
                    text = period.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onNextPeriod) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Mois suivant")
                }
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Reste à encaisser",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatXaf(totalOutstanding),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    StatusChip(
                        text = if (entries.isEmpty()) "À jour" else "${entries.size} impayés",
                        tone = if (entries.isEmpty()) {
                            StatusTone.SUCCESS
                        } else {
                            StatusTone.WARNING
                        }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                label = { Text("Nom, téléphone ou zone") }
            )
        }

        if (loading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        errorMessage?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!loading && entries.isEmpty()) {
            item {
                CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (query.isBlank()) "Tout le monde est à jour 🎉"
                        else "Aucun impayé trouvé",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (query.isBlank()) {
                            "Aucun montant restant pour cette période."
                        } else {
                            "Essayez un autre nom, téléphone ou quartier."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = entries,
                key = { it.customerId.value }
            ) { entry ->
                ArrearsCard(
                    entry = entry,
                    onRecordPayment = { onRecordPayment(entry) }
                )
            }
        }
    }
}

@Composable
private fun ArrearsCard(
    entry: ArrearsEntry,
    onRecordPayment: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.customerName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    entry.zoneName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.phone?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusChip(
                text = "À payer ${formatXaf(entry.outstandingXaf)}",
                tone = StatusTone.WARNING
            )
        }

        if (entry.paidXaf > 0) {
            Text(
                "Déjà payé : ${formatXaf(entry.paidXaf)} / ${formatXaf(entry.monthlyFeeXaf)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onRecordPayment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Encaisser")
        }
    }
}

internal fun sampleArrearsEntries(): List<ArrearsEntry> = listOf(
    ArrearsEntry(
        customerId = CustomerId("customer-1"),
        customerName = "Mme Mballa",
        phone = "+237 690 00 00 01",
        zoneName = "Bonamoussadi",
        monthlyFeeXaf = 5_000,
        paidXaf = 0,
        outstandingXaf = 5_000
    ),
    ArrearsEntry(
        customerId = CustomerId("customer-2"),
        customerName = "M. Ewane",
        phone = "+237 650 00 00 02",
        zoneName = "Makepe",
        monthlyFeeXaf = 7_500,
        paidXaf = 2_500,
        outstandingXaf = 5_000
    )
)

@Preview(name = "Impayés", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PaymentsUnpaidPreview() {
    CleanRouteTheme {
        PaymentsScreen(
            entries = sampleArrearsEntries(),
            period = YearMonth.of(2026, 9),
            query = "",
            loading = false,
            errorMessage = null,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onRecordPayment = {}
        )
    }
}

@Preview(name = "À jour", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PaymentsPaidPreview() {
    CleanRouteTheme {
        PaymentsScreen(
            entries = emptyList(),
            period = YearMonth.of(2026, 9),
            query = "",
            loading = false,
            errorMessage = null,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onRecordPayment = {}
        )
    }
}
