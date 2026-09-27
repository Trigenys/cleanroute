package com.trigenys.cleanroute.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.domain.CustomerId
import com.trigenys.cleanroute.domain.Payment
import com.trigenys.cleanroute.domain.PaymentDraft
import com.trigenys.cleanroute.domain.PaymentMethodOption
import com.trigenys.cleanroute.domain.PaymentMethods
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@Composable
fun PaymentEntryDialog(
    customerId: CustomerId,
    customerName: String,
    servicePeriod: YearMonth,
    suggestedAmountXaf: Long,
    submissionId: String,
    methods: List<PaymentMethodOption>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (PaymentDraft) -> Unit
) {
    var amount by remember(customerId, servicePeriod, submissionId) {
        mutableStateOf(suggestedAmountXaf.coerceAtLeast(0).toString())
    }
    var selectedMethodCode by remember(customerId, servicePeriod, submissionId) {
        mutableStateOf(methods.firstOrNull()?.method?.code.orEmpty())
    }

    val parsedAmount = amount.toLongOrNull()
    val selectedMethod = methods.firstOrNull {
        it.method.code == selectedMethodCode
    }
    val valid = parsedAmount != null && parsedAmount > 0 && selectedMethod != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enregistrer un paiement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    customerName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Période : $servicePeriod",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("Montant (F CFA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Mode de paiement",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { option ->
                        FilterChip(
                            selected = selectedMethodCode == option.method.code,
                            onClick = { selectedMethodCode = option.method.code },
                            label = { Text(option.label) }
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        PaymentDraft(
                            submissionId = submissionId,
                            customerId = customerId,
                            servicePeriod = servicePeriod,
                            amountXaf = requireNotNull(parsedAmount),
                            method = requireNotNull(selectedMethod).method
                        )
                    )
                },
                enabled = valid
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun PaymentReverseDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Annuler ce paiement ?") },
        text = {
            Text(
                "Le paiement de ${formatXaf(payment.amountXaf)} restera dans l’historique avec le statut « Annulé »."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmer l’annulation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Retour")
            }
        }
    )
}

internal fun paymentMethodLabel(payment: Payment): String =
    PaymentMethods.labelFor(payment.method)

internal fun formatXaf(amount: Long): String =
    NumberFormat.getIntegerInstance(Locale.FRENCH).format(amount) + " F"
