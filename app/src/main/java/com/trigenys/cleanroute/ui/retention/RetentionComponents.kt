package com.trigenys.cleanroute.ui.retention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.ReferralId
import com.trigenys.cleanroute.domain.ReferralRewardStatus
import com.trigenys.cleanroute.domain.ReferralSummary
import com.trigenys.cleanroute.domain.RetentionCustomerProfile
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone

@Composable
fun RetentionCard(
    profile: RetentionCustomerProfile,
    feedback: String?,
    onAttributeReferrer: () -> Unit,
    onAwardReferral: (ReferralId) -> Unit
) {
    CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Fidélité & parrainage",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Client depuis",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${profile.indicators.tenureDays} jours",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Collectes sur 90 j",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${profile.indicators.completedCollectionsLast90Days} / " +
                        "${profile.indicators.recordedVisitsLast90Days}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Text(
            "Code de parrainage",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            profile.referralCode,
            style = MaterialTheme.typography.headlineSmall
        )
        profile.referralLink?.let { link ->
            Text(
                link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val inbound = profile.referredBy
        if (inbound == null) {
            OutlinedButton(
                onClick = onAttributeReferrer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Attribuer un parrain")
            }
        } else {
            Text(
                "Parrain : ${inbound.customerName}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (profile.referralsMade.isNotEmpty()) {
            Text(
                "Parrainages",
                style = MaterialTheme.typography.labelLarge
            )
            profile.referralsMade.forEach { referral ->
                ReferralRewardRow(
                    summary = referral,
                    rewardLabel = profile.rewardLabel,
                    onAward = { onAwardReferral(referral.referral.id) }
                )
            }
        }

        feedback?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            "Aucun classement public : uniquement le suivi opérationnel du parrainage.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReferralRewardRow(
    summary: ReferralSummary,
    rewardLabel: String,
    onAward: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                summary.customerName,
                style = MaterialTheme.typography.bodyMedium
            )
            StatusChip(
                text = rewardStatusLabel(summary.referral.rewardStatus),
                tone = when (summary.referral.rewardStatus) {
                    ReferralRewardStatus.PENDING -> StatusTone.NEUTRAL
                    ReferralRewardStatus.ELIGIBLE -> StatusTone.WARNING
                    ReferralRewardStatus.AWARDED -> StatusTone.SUCCESS
                }
            )
        }

        if (summary.referral.rewardStatus == ReferralRewardStatus.ELIGIBLE) {
            Button(
                onClick = onAward,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accorder · $rewardLabel")
            }
        } else if (summary.referral.rewardStatus == ReferralRewardStatus.AWARDED) {
            Text(
                rewardLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReferralAttributionDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attribuer un parrain") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Saisissez le code communiqué par le client parrain.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Code de parrainage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(code.trim()) },
                enabled = code.isNotBlank()
            ) {
                Text("Attribuer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

private fun rewardStatusLabel(status: ReferralRewardStatus): String =
    when (status) {
        ReferralRewardStatus.PENDING -> "En attente"
        ReferralRewardStatus.ELIGIBLE -> "Éligible"
        ReferralRewardStatus.AWARDED -> "Accordée"
    }
