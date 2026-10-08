package com.trigenys.cleanroute.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.CustomerId
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
    onRecordPayment: (ArrearsEntry) -> Unit,
    onRemindPayment: (ArrearsEntry) -> Unit = {}
) {
    val totalOutstanding = entries.sumOf { it.outstandingXaf }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = "Paiements & impayés",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Les paiements sont enregistrés hors connexion sur cet appareil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousPeriod) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mois précédent")
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatPeriod(period),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(onClick = onNextPeriod) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Mois suivant")
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESTE À ENCAISSER",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusChip(
                            text = if (entries.isEmpty()) "Tout est à jour" else "${entries.size} impayé${if (entries.size > 1) "s" else ""}",
                            tone = if (entries.isEmpty()) StatusTone.SUCCESS else StatusTone.WARNING
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatXaf(totalOutstanding),
                            style = MaterialTheme.typography.displayMedium,
                            color = if (totalOutstanding == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "restant",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (entries.isEmpty()) "Aucun solde en attente" else "Soldes à traiter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (entries.isEmpty()) {
                                        "Aucun montant restant trouvé pour cette période."
                                    } else {
                                        "${entries.size} client${if (entries.size > 1) "s" else ""} avec un solde restant."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(24.dp))
                },
                trailingIcon = {
                    Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(22.dp))
                },
                placeholder = { Text("Rechercher par nom, téléphone ou zone…") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (loading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        errorMessage?.let { error ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (!loading && entries.isEmpty()) {
            item { PaymentsEmptyState(query = query) }
        } else {
            items(items = entries, key = { it.customerId.value }) { entry ->
                ArrearsCard(
                    entry = entry,
                    onRecordPayment = { onRecordPayment(entry) },
                    onRemindPayment = { onRemindPayment(entry) }
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paiements disponibles hors connexion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Les écritures restent dans la base locale de l’appareil même sans réseau.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsEmptyState(query: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(74.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = if (query.isBlank()) "Tout le monde est à jour 🎉" else "Aucun impayé trouvé",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (query.isBlank()) {
                    "Aucun retard de paiement identifié pour cette période."
                } else {
                    "Essayez un autre nom, téléphone ou quartier."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArrearsCard(
    entry: ArrearsEntry,
    onRecordPayment: () -> Unit,
    onRemindPayment: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = entry.customerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = buildString {
                            append(entry.zoneName)
                            entry.phone?.let { append(" · ").append(it) }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(text = formatXaf(entry.outstandingXaf), tone = StatusTone.WARNING)
            }

            if (entry.paidXaf > 0) {
                Text(
                    text = "Déjà payé : ${formatXaf(entry.paidXaf)} sur ${formatXaf(entry.monthlyFeeXaf)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Encaisser", fontWeight = FontWeight.ExtraBold)
                }
                FilledTonalButton(
                    onClick = onRemindPayment,
                    enabled = entry.phone != null,
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "WhatsApp", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

private fun formatPeriod(period: YearMonth): String {
    val month = when (period.monthValue) {
        1 -> "Janvier"
        2 -> "Février"
        3 -> "Mars"
        4 -> "Avril"
        5 -> "Mai"
        6 -> "Juin"
        7 -> "Juillet"
        8 -> "Août"
        9 -> "Septembre"
        10 -> "Octobre"
        11 -> "Novembre"
        else -> "Décembre"
    }
    return "$month ${period.year}"
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
            onRecordPayment = {},
            onRemindPayment = {}
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
            onRecordPayment = {},
            onRemindPayment = {}
        )
    }
}
