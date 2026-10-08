package com.trigenys.cleanroute.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.communication.CustomerContactService
import com.trigenys.cleanroute.data.transfer.CustomerSpreadsheetService
import com.trigenys.cleanroute.domain.PaymentRepository
import com.trigenys.cleanroute.ui.payment.PaymentsRoute
import com.trigenys.cleanroute.ui.payment.PaymentsScreen
import com.trigenys.cleanroute.ui.transfer.DataTransferRoute
import com.trigenys.cleanroute.ui.transfer.DataTransferScreen
import java.time.YearMonth

enum class MoreSection {
    PAYMENTS,
    EXCEL
}

@Composable
fun MoreRoute(
    paymentRepository: PaymentRepository?,
    contactService: CustomerContactService?,
    spreadsheetService: CustomerSpreadsheetService?,
    innerPadding: PaddingValues,
    initialSection: MoreSection = MoreSection.PAYMENTS,
    emptyLedgerPeriod: YearMonth = YearMonth.now()
) {
    var sectionName by rememberSaveable {
        mutableStateOf(initialSection.name)
    }

    LaunchedEffect(initialSection) {
        sectionName = initialSection.name
    }

    val section = MoreSection.valueOf(sectionName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MoreTab(
                    label = "Impayés",
                    icon = Icons.Outlined.ReceiptLong,
                    selected = section == MoreSection.PAYMENTS,
                    onClick = { sectionName = MoreSection.PAYMENTS.name },
                    modifier = Modifier.weight(1f)
                )
                MoreTab(
                    label = "Données & Excel",
                    icon = Icons.Outlined.TableChart,
                    selected = section == MoreSection.EXCEL,
                    onClick = { sectionName = MoreSection.EXCEL.name },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (section) {
                MoreSection.PAYMENTS -> {
                    if (paymentRepository == null || contactService == null) {
                        PaymentsScreen(
                            entries = emptyList(),
                            period = emptyLedgerPeriod,
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
                    } else {
                        PaymentsRoute(
                            repository = paymentRepository,
                            contactService = contactService,
                            innerPadding = PaddingValues()
                        )
                    }
                }

                MoreSection.EXCEL -> {
                    if (spreadsheetService == null) {
                        DataTransferScreen(
                            innerPadding = PaddingValues(),
                            plan = null,
                            busy = false,
                            message = null,
                            errorMessage = null,
                            onPickImport = {},
                            onApplyImport = {},
                            onExport = {}
                        )
                    } else {
                        DataTransferRoute(
                            service = spreadsheetService,
                            innerPadding = PaddingValues()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 7.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
