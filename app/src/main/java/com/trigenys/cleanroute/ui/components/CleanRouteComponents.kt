package com.trigenys.cleanroute.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme

enum class StatusTone {
    SUCCESS,
    WARNING,
    NEUTRAL
}

@Composable
fun CleanRouteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val colors = when (tone) {
        StatusTone.SUCCESS ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StatusTone.WARNING ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatusTone.NEUTRAL ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colors.first,
        contentColor = colors.second
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun KpiTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CleanRoutePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun CleanRouteSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ComponentsPreview() {
    CleanRouteTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("Actif", StatusTone.SUCCESS)
                StatusChip("À payer", StatusTone.WARNING)
                StatusChip("En attente", StatusTone.NEUTRAL)
            }
            KpiTile(
                label = "Clients actifs",
                value = "178",
                supportingText = "Exemple visuel",
                modifier = Modifier.fillMaxWidth()
            )
            CleanRoutePrimaryButton(
                text = "Commencer une tournée",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            CleanRouteSecondaryButton(
                text = "Ajouter un client",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
