package com.trigenys.cleanroute.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trigenys.cleanroute.domain.CollectionCadence
import com.trigenys.cleanroute.domain.CollectionVisit
import com.trigenys.cleanroute.domain.CollectionVisitId
import com.trigenys.cleanroute.domain.CollectionVisitStatus
import com.trigenys.cleanroute.domain.Customer
import com.trigenys.cleanroute.domain.CustomerDirectoryEntry
import com.trigenys.cleanroute.domain.CustomerDraft
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.CustomerProfile
import com.trigenys.cleanroute.domain.CustomerStatus
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentId
import com.trigenys.cleanroute.domain.PaymentMethod
import com.trigenys.cleanroute.domain.PaymentMethods
import com.trigenys.cleanroute.domain.PaymentState
import com.trigenys.cleanroute.domain.ReferralId
import com.trigenys.cleanroute.domain.RetentionCustomerProfile
import com.trigenys.cleanroute.domain.RouteDayId
import com.trigenys.cleanroute.domain.ServicePlan
import com.trigenys.cleanroute.domain.ServicePlanId
import com.trigenys.cleanroute.domain.Zone
import com.trigenys.cleanroute.domain.ZoneId
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.retention.RetentionCard
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun CustomerDirectoryScreen(
    entries: List<CustomerDirectoryEntry>,
    query: String,
    loading: Boolean,
    innerPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onAddCustomer: () -> Unit,
    onCustomerSelected: (CustomerDirectoryEntry) -> Unit,
    totalCount: Int = entries.size,
    sectors: List<String> = emptyList(),
    selectedSector: String? = null,
    onSectorSelected: (String?) -> Unit = {},
    onImportCustomers: () -> Unit = {},
    onOpenCollection: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 18.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Clients",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(9.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {}
                        Text(
                            text = if (totalCount == 1) {
                                "1 CLIENT ENREGISTRÉ"
                            } else {
                                "$totalCount CLIENTS ENREGISTRÉS"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onAddCustomer,
                    modifier = Modifier.heightIn(min = 56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ajouter",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                },
                placeholder = { Text("Nom, téléphone ou zone…") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (sectors.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSector == null,
                        onClick = { onSectorSelected(null) },
                        label = { Text("Tous les secteurs") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    sectors.forEach { sector ->
                        FilterChip(
                            selected = selectedSector == sector,
                            onClick = { onSectorSelected(sector) },
                            label = { Text(sector) }
                        )
                    }
                }
            }
        }

        if (loading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (entries.isEmpty()) {
            item {
                CustomerEmptyState(
                    query = query,
                    selectedSector = selectedSector,
                    onAddCustomer = onAddCustomer,
                    onImportCustomers = onImportCustomers
                )
            }
        } else {
            items(
                items = entries,
                key = { it.customer.id.value }
            ) { entry ->
                CustomerRow(
                    entry = entry,
                    onClick = { onCustomerSelected(entry) }
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "OUTILS & PRISES RAPIDES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomerQuickTool(
                        icon = Icons.Outlined.UploadFile,
                        title = "Modèle Excel",
                        subtitle = "Importer ou exporter",
                        onClick = onImportCustomers,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerQuickTool(
                        icon = Icons.Outlined.LocalShipping,
                        title = "Zones",
                        subtitle = "Ouvrir les tournées",
                        onClick = onOpenCollection,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.48f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Base locale active : vos clients restent disponibles hors connexion sur cet appareil.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerEmptyState(
    query: String,
    selectedSector: String?,
    onAddCustomer: () -> Unit,
    onImportCustomers: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Text(
                text = if (query.isBlank() && selectedSector == null) {
                    "Aucun client pour le moment"
                } else {
                    "Aucun client trouvé"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (query.isBlank() && selectedSector == null) {
                    "Démarrez votre activité en créant un premier profil ou en important votre registre Excel."
                } else {
                    "Modifiez la recherche ou choisissez un autre secteur."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (query.isBlank() && selectedSector == null) {
                Button(
                    onClick = onAddCustomer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ajouter un client",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onImportCustomers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Importer un fichier Excel")
                }
            }
        }
    }
}

@Composable
private fun CustomerQuickTool(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomerRow(
    entry: CustomerDirectoryEntry,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildString {
                        append(entry.zoneName)
                        entry.customer.phone?.let { append(" · ").append(it) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (entry.customer.status == CustomerStatus.ACTIVE) "Actif" else "Suspendu",
                tone = if (entry.customer.status == CustomerStatus.ACTIVE) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.WARNING
                }
            )
        }
    }
}

@Composable
fun CustomerDetailScreen(
    profile: CustomerProfile,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRecordPayment: () -> Unit = {},
    onReversePayment: (Payment) -> Unit = {},
    onCall: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    contactFeedback: String? = null,
    retentionProfile: RetentionCustomerProfile? = null,
    retentionFeedback: String? = null,
    onAttributeReferrer: () -> Unit = {},
    onAwardReferral: (ReferralId) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text(
                    "Fiche client",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Modifier")
                }
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.customer.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            profile.customer.phone ?: "Aucun numéro",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            buildString {
                                append(profile.zone.name)
                                profile.customer.addressLabel?.let { append(" · ").append(it) }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(
                        text = if (profile.customer.status == CustomerStatus.ACTIVE) "Actif" else "Suspendu",
                        tone = if (profile.customer.status == CustomerStatus.ACTIVE) {
                            StatusTone.SUCCESS
                        } else {
                            StatusTone.WARNING
                        }
                    )
                }

                Text(
                    profile.nextCollectionDate?.let { "Prochain passage : $it" }
                        ?: "Prochain passage : non planifié",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContactActionButton(
                        label = "Appeler",
                        icon = Icons.Outlined.Phone,
                        enabled = profile.customer.phone != null,
                        onClick = onCall,
                        modifier = Modifier.weight(1f)
                    )
                    ContactActionButton(
                        label = "WhatsApp",
                        icon = Icons.Outlined.ChatBubbleOutline,
                        enabled = profile.customer.phone != null,
                        onClick = onWhatsApp,
                        modifier = Modifier.weight(1f)
                    )
                    PlaceholderAction(
                        label = "Localiser",
                        icon = Icons.Outlined.LocationOn,
                        modifier = Modifier.weight(1f)
                    )
                }

                contactFeedback?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    "WhatsApp prépare le message : rien n’est envoyé sans votre action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Text("Abonnement", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatXaf(profile.servicePlan.monthlyFeeXaf) + " / mois",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Payé ce mois : ${formatXaf(profile.paidThisPeriodXaf)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (profile.outstandingThisPeriodXaf == 0L) {
                        "À payer : rien"
                    } else {
                        "À payer : ${formatXaf(profile.outstandingThisPeriodXaf)}"
                    },
                    color = if (profile.outstandingThisPeriodXaf == 0L) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                )

                if (profile.outstandingThisPeriodXaf > 0L) {
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enregistrer un paiement")
                    }
                }
            }
        }

        retentionProfile?.let { retention ->
            item {
                RetentionCard(
                    profile = retention,
                    feedback = retentionFeedback,
                    onAttributeReferrer = onAttributeReferrer,
                    onAwardReferral = onAwardReferral
                )
            }
        }

        item {
            Text("Historique récent", style = MaterialTheme.typography.titleMedium)
        }

        if (profile.recentVisits.isEmpty() && profile.recentPayments.isEmpty()) {
            item {
                Text(
                    "Aucune activité enregistrée.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(profile.recentVisits, key = { "visit-${it.id.value}" }) { visit ->
                ActivityRow(
                    title = when (visit.status) {
                        CollectionVisitStatus.COLLECTED -> "Collecte effectuée"
                        CollectionVisitStatus.ABSENT -> "Client absent"
                        CollectionVisitStatus.NO_WASTE -> "Pas de déchets"
                        CollectionVisitStatus.SCHEDULED -> "Collecte planifiée"
                    },
                    detail = visit.scheduledDate.toString()
                )
            }
            items(profile.recentPayments, key = { "payment-${it.id.value}" }) { payment ->
                PaymentActivityRow(
                    payment = payment,
                    onReverse = { onReversePayment(payment) }
                )
            }
        }
    }
}

@Composable
private fun ContactActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun PlaceholderAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun ActivityRow(title: String, detail: String) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentActivityRow(
    payment: Payment,
    onReverse: () -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Paiement · ${formatXaf(payment.amountXaf)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${payment.servicePeriod} · ${PaymentMethods.labelFor(payment.method)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (payment.state == PaymentState.RECORDED) "Enregistré" else "Annulé",
                tone = if (payment.state == PaymentState.RECORDED) {
                    StatusTone.SUCCESS
                } else {
                    StatusTone.NEUTRAL
                }
            )
        }

        if (payment.state == PaymentState.REVERSED) {
            Text(
                "Annulé le ${payment.reversedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(onClick = onReverse) {
                Text("Annuler ce paiement")
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    initialProfile: CustomerProfile?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (CustomerDraft) -> Unit,
    suggestedZones: List<String> = emptyList()
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 8.dp
        ) {
            CustomerFormContent(
                initialProfile = initialProfile,
                errorMessage = errorMessage,
                onDismiss = onDismiss,
                onSave = onSave,
                suggestedZones = suggestedZones
            )
        }
    }
}

@Composable
internal fun CustomerFormContent(
    initialProfile: CustomerProfile?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (CustomerDraft) -> Unit,
    suggestedZones: List<String> = emptyList()
) {
    var name by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.name.orEmpty())
    }
    var phone by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.phone.orEmpty())
    }
    var zone by remember(initialProfile) {
        mutableStateOf(initialProfile?.zone?.name.orEmpty())
    }
    var address by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.addressLabel.orEmpty())
    }
    var monthlyFee by remember(initialProfile) {
        mutableStateOf(
            initialProfile?.servicePlan?.monthlyFeeXaf?.takeIf { it > 0 }?.toString().orEmpty()
        )
    }
    var active by remember(initialProfile) {
        mutableStateOf(initialProfile?.customer?.status != CustomerStatus.SUSPENDED)
    }

    val feeValue = monthlyFee.toLongOrNull()
    val valid = name.isNotBlank() && zone.isNotBlank() && feeValue != null && feeValue >= 0

    LazyColumn(
        modifier = Modifier.heightIn(max = 720.dp),
        contentPadding = PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (initialProfile == null) "Nouveau client" else "Modifier le client",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Enregistrement local du point de collecte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = "LOCAL DB",
                    tone = StatusTone.SUCCESS
                )
            }
        }

        item {
            CustomerFormField(
                label = "Nom et prénom *",
                value = name,
                onValueChange = { name = it },
                placeholder = "Ex. Amina T.",
                leadingIcon = Icons.Outlined.Groups
            )
        }

        item {
            CustomerFormField(
                label = "Numéro de téléphone",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "+237 6XX XX XX XX",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                CustomerFormField(
                    label = "Zone / quartier *",
                    value = zone,
                    onValueChange = { zone = it },
                    placeholder = "Ex. Bonamoussadi",
                    leadingIcon = Icons.Outlined.HomeWork
                )

                if (suggestedZones.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedZones.take(6).forEach { suggestion ->
                            FilterChip(
                                selected = zone.equals(suggestion, ignoreCase = true),
                                onClick = { zone = suggestion },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }
        }

        item {
            CustomerFormField(
                label = "Repère visuel",
                value = address,
                onValueChange = { address = it },
                placeholder = "Ex. portail vert, face boulangerie",
                leadingIcon = Icons.Outlined.LocationOn
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Abonnement mensuel *",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    feeValue?.let {
                        Text(
                            text = formatXaf(it) + " / mois",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                OutlinedTextField(
                    value = monthlyFee,
                    onValueChange = { monthlyFee = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Montant en FCFA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2500L, 4000L, 6500L).forEach { amount ->
                        FilterChip(
                            selected = feeValue == amount,
                            onClick = { monthlyFee = amount.toString() },
                            label = { Text(formatXaf(amount)) }
                        )
                    }
                }
            }
        }

        if (initialProfile != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (active) "Client actif" else "Client suspendu",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Le statut existant est conservé tant que vous ne le changez pas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = active,
                            onCheckedChange = { active = it }
                        )
                    }
                }
            }
        }

        errorMessage?.let { message ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Text(
                text = "* Nom, zone et abonnement mensuel sont obligatoires.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.8f)
                        .heightIn(min = 56.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Annuler",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        onSave(
                            CustomerDraft(
                                customerId = initialProfile?.customer?.id,
                                name = name,
                                phone = phone.trim().takeIf(String::isNotEmpty),
                                zoneName = zone,
                                addressLabel = address.trim().takeIf(String::isNotEmpty),
                                monthlyFeeXaf = requireNotNull(feeValue),
                                status = if (active) {
                                    CustomerStatus.ACTIVE
                                } else {
                                    CustomerStatus.SUSPENDED
                                }
                            )
                        )
                    },
                    enabled = valid,
                    modifier = Modifier
                        .weight(1.7f)
                        .heightIn(min = 56.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (initialProfile == null) {
                            "Enregistrer le client"
                        } else {
                            "Enregistrer"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
            },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

private fun formatXaf(amount: Long): String =
    NumberFormat.getIntegerInstance(Locale.FRENCH).format(amount) + " F"

private fun sampleCustomer(
    id: String = "customer-1",
    name: String = "Mme Mballa",
    status: CustomerStatus = CustomerStatus.ACTIVE
): Customer = Customer(
    id = CustomerId(id),
    name = name,
    phone = "+237 690 00 00 01",
    zoneId = ZoneId("zone-1"),
    addressLabel = "Immeuble vert",
    servicePlanId = ServicePlanId("plan-1"),
    status = status,
    createdAt = Instant.parse("2026-09-01T08:00:00Z"),
    updatedAt = Instant.parse("2026-09-26T08:00:00Z")
)

internal fun sampleCustomerProfile(
    status: CustomerStatus = CustomerStatus.ACTIVE
): CustomerProfile {
    val customer = sampleCustomer(status = status)
    return CustomerProfile(
        customer = customer,
        zone = Zone(ZoneId("zone-1"), "Bonamoussadi"),
        servicePlan = ServicePlan(
            id = ServicePlanId("plan-1"),
            label = "Abonnement",
            cadence = CollectionCadence.CUSTOM,
            monthlyFeeXaf = 5_000
        ),
        nextCollectionDate = LocalDate.of(2026, 9, 28),
        paidThisPeriodXaf = 5_000,
        outstandingThisPeriodXaf = 0,
        recentVisits = listOf(
            CollectionVisit(
                id = CollectionVisitId("visit-1"),
                routeDayId = RouteDayId("route-1"),
                customerId = customer.id,
                scheduledDate = LocalDate.of(2026, 9, 24),
                status = CollectionVisitStatus.COLLECTED,
                statusChangedAt = Instant.parse("2026-09-24T09:42:00Z"),
                revision = 1
            )
        ),
        recentPayments = listOf(
            Payment(
                id = PaymentId("payment-1"),
                customerId = customer.id,
                servicePeriod = YearMonth.of(2026, 9),
                amountXaf = 5_000,
                method = PaymentMethod("cash"),
                recordedAt = Instant.parse("2026-09-02T14:10:00Z")
            )
        )
    )
}

@Preview(name = "Clients - empty", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun EmptyDirectoryPreview() {
    CleanRouteTheme {
        CustomerDirectoryScreen(
            entries = emptyList(),
            query = "",
            loading = false,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onAddCustomer = {},
            onCustomerSelected = {}
        )
    }
}

@Preview(name = "Clients - populated", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun PopulatedDirectoryPreview() {
    CleanRouteTheme {
        CustomerDirectoryScreen(
            entries = listOf(
                CustomerDirectoryEntry(sampleCustomer(), "Bonamoussadi"),
                CustomerDirectoryEntry(
                    sampleCustomer("customer-2", "M. Ewane", CustomerStatus.SUSPENDED),
                    "Makepe"
                )
            ),
            query = "",
            loading = false,
            innerPadding = PaddingValues(),
            onQueryChange = {},
            onAddCustomer = {},
            onCustomerSelected = {}
        )
    }
}

@Preview(name = "Client - suspended", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SuspendedDetailPreview() {
    CleanRouteTheme {
        CustomerDetailScreen(
            profile = sampleCustomerProfile(CustomerStatus.SUSPENDED),
            innerPadding = PaddingValues(),
            onBack = {},
            onEdit = {}
        )
    }
}
