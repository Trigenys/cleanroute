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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

@Composable
fun CustomerDirectoryRoute(
    repository: CustomerRepository,
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
