package com.trigenys.cleanroute.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trigenys.cleanroute.R
import com.trigenys.cleanroute.ui.components.CleanRouteCard
import com.trigenys.cleanroute.ui.components.CleanRoutePrimaryButton
import com.trigenys.cleanroute.ui.components.CleanRouteSecondaryButton
import com.trigenys.cleanroute.ui.components.KpiTile
import com.trigenys.cleanroute.ui.components.StatusChip
import com.trigenys.cleanroute.ui.components.StatusTone
import com.trigenys.cleanroute.ui.navigation.AppDestination
import com.trigenys.cleanroute.ui.navigation.CleanRouteBottomBar
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme

@Composable
fun App() {
    var selectedDestination by rememberSaveable {
        mutableStateOf(AppDestination.HOME)
    }

    CleanRouteShell(
        selectedDestination = selectedDestination,
        onDestinationSelected = { selectedDestination = it }
    )
}

@Composable
private fun CleanRouteShell(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CleanRouteBottomBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected
            )
        }
    ) { innerPadding ->
        when (selectedDestination) {
            AppDestination.HOME -> FoundationHome(innerPadding)
            AppDestination.COLLECTION -> ModulePlaceholder(
                innerPadding = innerPadding,
                title = stringResource(R.string.nav_collection),
                description = stringResource(R.string.collection_placeholder),
                icon = Icons.Outlined.Route
            )
            AppDestination.CLIENTS -> ModulePlaceholder(
                innerPadding = innerPadding,
                title = stringResource(R.string.nav_clients),
                description = stringResource(R.string.clients_placeholder),
                icon = Icons.Outlined.Group
            )
            AppDestination.MORE -> ModulePlaceholder(
                innerPadding = innerPadding,
                title = stringResource(R.string.nav_more),
                description = stringResource(R.string.more_placeholder),
                icon = Icons.Outlined.MoreHoriz
            )
        }
    }
}

@Composable
private fun FoundationHome(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CleanRouteCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.no_route_loaded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(
                        text = stringResource(R.string.offline_ready),
                        tone = StatusTone.SUCCESS
                    )
                }

                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth()
                )

                CleanRoutePrimaryButton(
                    text = stringResource(R.string.start_route),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.overview),
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiTile(
                    label = stringResource(R.string.active_clients),
                    value = "—",
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = stringResource(R.string.unpaid),
                    value = "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CleanRouteSecondaryButton(
                text = stringResource(R.string.add_client),
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ModulePlaceholder(
    innerPadding: PaddingValues,
    title: String,
    description: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Phone", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(name = "Tablet", widthDp = 840, heightDp = 900, showBackground = true)
@Preview(
    name = "Dark",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AppPreview() {
    CleanRouteTheme {
        App()
    }
}
