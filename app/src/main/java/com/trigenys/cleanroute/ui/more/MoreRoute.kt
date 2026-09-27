package com.trigenys.cleanroute.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.data.transfer.CustomerSpreadsheetService
import com.trigenys.cleanroute.domain.PaymentRepository
import com.trigenys.cleanroute.ui.payment.PaymentsRoute
import com.trigenys.cleanroute.ui.payment.PaymentsScreen
import com.trigenys.cleanroute.ui.transfer.DataTransferRoute
import com.trigenys.cleanroute.ui.transfer.DataTransferScreen
import java.time.YearMonth

private enum class MoreSection {
    PAYMENTS,
    EXCEL
}

@Composable
fun MoreRoute(
    paymentRepository: PaymentRepository?,
    spreadsheetService: CustomerSpreadsheetService?,
    innerPadding: PaddingValues
) {
    var sectionName by rememberSaveable {
        mutableStateOf(MoreSection.PAYMENTS.name)
    }
    val section = MoreSection.valueOf(sectionName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = section == MoreSection.PAYMENTS,
                onClick = { sectionName = MoreSection.PAYMENTS.name },
                label = { Text("Impayés") }
            )
            FilterChip(
                selected = section == MoreSection.EXCEL,
                onClick = { sectionName = MoreSection.EXCEL.name },
                label = { Text("Excel") }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (section) {
                MoreSection.PAYMENTS -> {
                    if (paymentRepository == null) {
                        PaymentsScreen(
                            entries = emptyList(),
                            period = YearMonth.now(),
                            query = "",
                            loading = false,
                            errorMessage = null,
                            innerPadding = PaddingValues(),
                            onQueryChange = {},
                            onPreviousPeriod = {},
                            onNextPeriod = {},
                            onRecordPayment = {}
                        )
                    } else {
                        PaymentsRoute(
                            repository = paymentRepository,
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
