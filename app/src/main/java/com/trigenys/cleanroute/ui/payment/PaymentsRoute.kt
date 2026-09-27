package com.trigenys.cleanroute.ui.payment

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.trigenys.cleanroute.communication.ContactLaunchResult
import com.trigenys.cleanroute.communication.CustomerContactService
import com.trigenys.cleanroute.communication.CustomerMessageData
import com.trigenys.cleanroute.communication.CustomerMessageKind
import com.trigenys.cleanroute.domain.ArrearsEntry
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentRepository
import java.time.Instant
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun PaymentsRoute(
    repository: PaymentRepository,
    contactService: CustomerContactService,
    innerPadding: PaddingValues
) {
    var periodText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val period = remember(periodText) { YearMonth.parse(periodText) }
    var query by rememberSaveable { mutableStateOf("") }
    var entries by remember { mutableStateOf(emptyList<ArrearsEntry>()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var paymentTarget by remember { mutableStateOf<ArrearsEntry?>(null) }
    var submissionId by remember { mutableStateOf<String?>(null) }
    var refreshVersion by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(period, query, refreshVersion) {
        loading = true
        errorMessage = null
        runCatching {
            repository.arrears(period, query)
        }.onSuccess {
            entries = it
        }.onFailure {
            errorMessage = it.message ?: "Impossible de charger les impayés."
        }
        loading = false
    }

    PaymentsScreen(
        entries = entries,
        period = period,
        query = query,
        loading = loading,
        errorMessage = errorMessage,
        innerPadding = innerPadding,
        onQueryChange = { query = it },
        onPreviousPeriod = { periodText = period.minusMonths(1).toString() },
        onNextPeriod = { periodText = period.plusMonths(1).toString() },
        onRecordPayment = { entry ->
            paymentTarget = entry
            submissionId = UUID.randomUUID().toString()
            errorMessage = null
        },
        onRemindPayment = { entry ->
            val phone = entry.phone
            if (phone != null) {
                scope.launch {
                    errorMessage = contactService.whatsApp(
                        context = context,
                        customerId = entry.customerId,
                        rawPhone = phone,
                        kind = CustomerMessageKind.PAYMENT_REMINDER,
                        data = CustomerMessageData(
                            customerName = entry.customerName,
                            outstandingXaf = entry.outstandingXaf,
                            servicePeriod = period
                        ),
                        at = Instant.now()
                    ).feedback()
                }
            }
        }
    )

    val target = paymentTarget
    val currentSubmissionId = submissionId
    if (target != null && currentSubmissionId != null) {
        PaymentEntryDialog(
            customerId = target.customerId,
            customerName = target.customerName,
            servicePeriod = period,
            suggestedAmountXaf = target.outstandingXaf,
            submissionId = currentSubmissionId,
            methods = repository.methods,
            errorMessage = errorMessage,
            onDismiss = {
                paymentTarget = null
                submissionId = null
                errorMessage = null
            },
            onSave = { draft: PaymentDraft ->
                scope.launch {
                    loading = true
                    runCatching {
                        repository.record(draft, Instant.now())
                    }.onSuccess {
                        paymentTarget = null
                        submissionId = null
                        errorMessage = null
                        refreshVersion += 1
                    }.onFailure {
                        errorMessage = it.message ?: "Impossible d’enregistrer le paiement."
                    }
                    loading = false
                }
            }
        )
    }
}


private fun ContactLaunchResult.feedback(): String? =
    when (this) {
        ContactLaunchResult.Launched -> null
        is ContactLaunchResult.Unavailable -> message
        is ContactLaunchResult.Failed -> message
    }
