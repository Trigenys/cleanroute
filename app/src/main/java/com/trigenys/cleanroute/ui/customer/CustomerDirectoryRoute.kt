package com.trigenys.cleanroute.ui.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerProfile
import com.trigenys.cleanroute.domain.CustomerRepository
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.launch
import com.trigenys.cleanroute.ui.payment.PaymentEntryDialog
import com.trigenys.cleanroute.ui.payment.PaymentReverseDialog

@Composable
fun CustomerDirectoryRoute(
    repository: CustomerRepository,
    paymentRepository: PaymentRepository,
    innerPadding: PaddingValues
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCustomerId by rememberSaveable { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf(emptyList<CustomerDirectoryEntry>()) }
    var profile by remember { mutableStateOf<CustomerProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var profileLoading by remember { mutableStateOf(false) }
    var editorProfile by remember { mutableStateOf<CustomerProfile?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var refreshVersion by remember { mutableIntStateOf(0) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var paymentSubmissionId by remember { mutableStateOf<String?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var paymentToReverse by remember { mutableStateOf<Payment?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query, refreshVersion) {
        loading = true
        entries = repository.search(query)
        loading = false
    }

    LaunchedEffect(selectedCustomerId, refreshVersion) {
        val id = selectedCustomerId
        if (id == null) {
            profile = null
            profileLoading = false
        } else {
            profileLoading = true
            profile = repository.getProfile(
                id = CustomerId(id),
                servicePeriod = YearMonth.now(),
                today = LocalDate.now()
            )
            profileLoading = false
        }
    }

    when {
        selectedCustomerId != null && profileLoading -> CustomerLoadingScreen(innerPadding)
        selectedCustomerId != null && profile != null -> CustomerDetailScreen(
            profile = requireNotNull(profile),
            innerPadding = innerPadding,
            onBack = { selectedCustomerId = null },
            onEdit = {
                editorProfile = profile
                showEditor = true
            },
            onRecordPayment = {
                paymentError = null
                paymentSubmissionId = UUID.randomUUID().toString()
            },
            onReversePayment = { payment ->
                paymentError = null
                paymentToReverse = payment
            }
        )
        selectedCustomerId != null -> CustomerLoadingScreen(innerPadding)
        else -> CustomerDirectoryScreen(
            entries = entries,
            query = query,
            loading = loading,
            innerPadding = innerPadding,
            onQueryChange = { query = it },
            onAddCustomer = {
                editorProfile = null
                saveError = null
                showEditor = true
            },
            onCustomerSelected = { selectedCustomerId = it.customer.id.value }
        )
    }

    val currentProfile = profile
    val currentSubmissionId = paymentSubmissionId
    if (currentProfile != null && currentSubmissionId != null) {
        PaymentEntryDialog(
            customerId = currentProfile.customer.id,
            customerName = currentProfile.customer.name,
            servicePeriod = YearMonth.now(),
            suggestedAmountXaf = currentProfile.outstandingThisPeriodXaf,
            submissionId = currentSubmissionId,
            methods = paymentRepository.methods,
            errorMessage = paymentError,
            onDismiss = {
                paymentSubmissionId = null
                paymentError = null
            },
            onSave = { draft: PaymentDraft ->
                scope.launch {
                    runCatching {
                        paymentRepository.record(draft, Instant.now())
                    }.onSuccess {
                        paymentSubmissionId = null
                        paymentError = null
                        refreshVersion += 1
                    }.onFailure { error ->
                        paymentError = error.message ?: "Impossible d’enregistrer le paiement."
                    }
                }
            }
        )
    }

    paymentToReverse?.let { payment ->
        PaymentReverseDialog(
            payment = payment,
            onDismiss = {
                paymentToReverse = null
                paymentError = null
            },
            onConfirm = {
                scope.launch {
                    runCatching {
                        paymentRepository.reverse(payment.id, Instant.now())
                    }.onSuccess {
                        paymentToReverse = null
                        paymentError = null
                        refreshVersion += 1
                    }.onFailure { error ->
                        paymentError = error.message ?: "Impossible d’annuler le paiement."
                    }
                }
            }
        )
    }

    if (showEditor) {
        CustomerFormDialog(
            initialProfile = editorProfile,
            errorMessage = saveError,
            onDismiss = {
                showEditor = false
                saveError = null
            },
            onSave = { draft: CustomerDraft ->
                scope.launch {
                    runCatching {
                        repository.saveDraft(draft, Instant.now())
                    }.onSuccess { id ->
                        saveError = null
                        showEditor = false
                        selectedCustomerId = id.value
                        refreshVersion += 1
                    }.onFailure { error ->
                        saveError = error.message ?: "Impossible d’enregistrer le client."
                    }
                }
            }
        )
    }
}

@Composable
private fun CustomerLoadingScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
