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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.trigenys.cleanroute.communication.ContactLaunchResult
import com.trigenys.cleanroute.communication.CustomerContactService
import com.trigenys.cleanroute.communication.CustomerMessageData
import com.trigenys.cleanroute.communication.CustomerMessageKind
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerProfile
import com.trigenys.cleanroute.domain.CustomerRepository
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentRepository
import com.trigenys.cleanroute.domain.RetentionCustomerProfile
import com.trigenys.cleanroute.domain.RetentionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.launch
import com.trigenys.cleanroute.ui.payment.PaymentEntryDialog
import com.trigenys.cleanroute.ui.payment.PaymentReverseDialog
import com.trigenys.cleanroute.ui.retention.ReferralAttributionDialog

@Composable
fun CustomerDirectoryRoute(
    repository: CustomerRepository,
    paymentRepository: PaymentRepository,
    contactService: CustomerContactService,
    retentionRepository: RetentionRepository?,
    innerPadding: PaddingValues,
    onOpenImport: () -> Unit = {},
    onOpenCollection: () -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSector by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCustomerId by rememberSaveable { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf(emptyList<CustomerDirectoryEntry>()) }
    var sectors by remember { mutableStateOf(emptyList<String>()) }
    var outstandingByCustomer by remember { mutableStateOf<Map<String, Long>?>(null) }
    var totalCount by remember { mutableIntStateOf(0) }
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
    var contactFeedback by remember { mutableStateOf<String?>(null) }
    var retentionProfile by remember { mutableStateOf<RetentionCustomerProfile?>(null) }
    var retentionFeedback by remember { mutableStateOf<String?>(null) }
    var showReferralDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshVersion) {
        outstandingByCustomer = runCatching {
            paymentRepository.arrears(YearMonth.now(), "")
        }.getOrNull()?.associate { it.customerId.value to it.outstandingXaf }
    }

    LaunchedEffect(refreshVersion) {
        val allEntries = repository.search("")
        totalCount = allEntries.size
        sectors = allEntries
            .map { it.zoneName }
            .distinct()
            .sortedBy { it.lowercase() }
        if (selectedSector != null && selectedSector !in sectors) {
            selectedSector = null
        }
    }

    LaunchedEffect(query, selectedSector, refreshVersion) {
        loading = true
        val searchResults = repository.search(query)
        entries = selectedSector?.let { sector ->
            searchResults.filter { it.zoneName.equals(sector, ignoreCase = true) }
        } ?: searchResults
        loading = false
    }

    LaunchedEffect(selectedCustomerId, refreshVersion) {
        val id = selectedCustomerId
        if (id == null) {
            profile = null
            retentionProfile = null
            retentionFeedback = null
            profileLoading = false
        } else {
            profileLoading = true
            val customerId = CustomerId(id)
            profile = repository.getProfile(
                id = customerId,
                servicePeriod = YearMonth.now(),
                today = LocalDate.now()
            )
            retentionProfile = retentionRepository?.let { retention ->
                runCatching {
                    retention.getProfile(
                        customerId = customerId,
                        today = LocalDate.now(),
                        timeZone = ZoneId.systemDefault(),
                        at = Instant.now()
                    )
                }.onFailure { error ->
                    retentionFeedback = error.message
                        ?: "Impossible de charger le parrainage."
                }.getOrNull()
            }
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
            },
            onCall = {
                val current = requireNotNull(profile)
                val phone = current.customer.phone
                if (phone != null) {
                    scope.launch {
                        contactFeedback = contactService.call(
                            context = context,
                            customerId = current.customer.id,
                            rawPhone = phone,
                            at = Instant.now()
                        ).feedback()
                    }
                }
            },
            onWhatsApp = {
                val current = requireNotNull(profile)
                val phone = current.customer.phone
                if (phone != null) {
                    val period = YearMonth.now()
                    val completedVisit = current.recentVisits.firstOrNull {
                        it.status == CollectionVisitStatus.COLLECTED
                    }
                    val kind = when {
                        current.outstandingThisPeriodXaf > 0 ->
                            CustomerMessageKind.PAYMENT_REMINDER
                        current.nextCollectionDate != null ->
                            CustomerMessageKind.UPCOMING_COLLECTION
                        completedVisit != null ->
                            CustomerMessageKind.COLLECTION_COMPLETED
                        else -> null
                    }
                    val data = kind?.let { messageKind ->
                        CustomerMessageData(
                            customerName = current.customer.name,
                            collectionDate = when (messageKind) {
                                CustomerMessageKind.UPCOMING_COLLECTION ->
                                    current.nextCollectionDate
                                CustomerMessageKind.COLLECTION_COMPLETED ->
                                    completedVisit?.scheduledDate
                                CustomerMessageKind.PAYMENT_REMINDER -> null
                            },
                            outstandingXaf = if (
                                messageKind == CustomerMessageKind.PAYMENT_REMINDER
                            ) {
                                current.outstandingThisPeriodXaf
                            } else {
                                null
                            },
                            servicePeriod = if (
                                messageKind == CustomerMessageKind.PAYMENT_REMINDER
                            ) {
                                period
                            } else {
                                null
                            }
                        )
                    }
                    scope.launch {
                        contactFeedback = contactService.whatsApp(
                            context = context,
                            customerId = current.customer.id,
                            rawPhone = phone,
                            kind = kind,
                            data = data,
                            at = Instant.now()
                        ).feedback()
                    }
                }
            },
            contactFeedback = contactFeedback,
            retentionProfile = retentionProfile,
            retentionFeedback = retentionFeedback,
            onAttributeReferrer = {
                retentionFeedback = null
                showReferralDialog = true
            },
            onAwardReferral = { referralId ->
                val retention = retentionRepository
                if (retention != null) {
                    scope.launch {
                        runCatching {
                            retention.award(referralId, Instant.now())
                        }.onSuccess {
                            retentionFeedback = null
                            refreshVersion += 1
                        }.onFailure { error ->
                            retentionFeedback = error.message
                                ?: "Impossible d’accorder la récompense."
                        }
                    }
                }
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
            onCustomerSelected = { selectedCustomerId = it.customer.id.value },
            totalCount = totalCount,
            sectors = sectors,
            selectedSector = selectedSector,
            onSectorSelected = { selectedSector = it },
            onImportCustomers = onOpenImport,
            onOpenCollection = onOpenCollection,
            outstandingByCustomer = outstandingByCustomer,
            onCallCustomer = { entry ->
                val phone = entry.customer.phone
                if (phone != null) {
                    scope.launch {
                        contactFeedback = contactService.call(
                            context = context,
                            customerId = entry.customer.id,
                            rawPhone = phone,
                            at = Instant.now()
                        ).feedback()
                    }
                }
            },
            onWhatsAppCustomer = { entry ->
                val phone = entry.customer.phone
                if (phone != null) {
                    val owed = outstandingByCustomer?.get(entry.customer.id.value) ?: 0L
                    val reminder = owed > 0L
                    scope.launch {
                        contactFeedback = contactService.whatsApp(
                            context = context,
                            customerId = entry.customer.id,
                            rawPhone = phone,
                            kind = if (reminder) CustomerMessageKind.PAYMENT_REMINDER else null,
                            data = if (reminder) {
                                CustomerMessageData(
                                    customerName = entry.customer.name,
                                    collectionDate = null,
                                    outstandingXaf = owed,
                                    servicePeriod = YearMonth.now()
                                )
                            } else {
                                null
                            },
                            at = Instant.now()
                        ).feedback()
                    }
                }
            },
            onCollectCustomer = { entry ->
                paymentError = null
                paymentSubmissionId = UUID.randomUUID().toString()
                selectedCustomerId = entry.customer.id.value
            }
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

    if (showReferralDialog) {
        ReferralAttributionDialog(
            errorMessage = retentionFeedback,
            onDismiss = {
                showReferralDialog = false
                retentionFeedback = null
            },
            onConfirm = { code ->
                val retention = retentionRepository
                val current = profile
                if (retention != null && current != null) {
                    scope.launch {
                        runCatching {
                            retention.attribute(
                                referredCustomerId = current.customer.id,
                                referralCode = code,
                                at = Instant.now()
                            )
                        }.onSuccess {
                            showReferralDialog = false
                            retentionFeedback = null
                            refreshVersion += 1
                        }.onFailure { error ->
                            retentionFeedback = error.message
                                ?: "Impossible d’attribuer ce parrain."
                        }
                    }
                }
            }
        )
    }

    if (showEditor) {
        CustomerFormDialog(
            initialProfile = editorProfile,
            errorMessage = saveError,
            suggestedZones = sectors,
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


private fun ContactLaunchResult.feedback(): String? =
    when (this) {
        ContactLaunchResult.Launched -> null
        is ContactLaunchResult.Unavailable -> message
        is ContactLaunchResult.Failed -> message
    }
