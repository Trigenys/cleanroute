package com.trigenys.cleanroute.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.trigenys.cleanroute.R

enum class AppDestination(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HOME(R.string.nav_home, Icons.Outlined.Home),
    COLLECTION(R.string.nav_collection, Icons.Outlined.Route),
    CLIENTS(R.string.nav_clients, Icons.Outlined.Group),
    MORE(R.string.nav_more, Icons.Outlined.MoreHoriz)
}

@Composable
fun CleanRouteBottomBar(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AppDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
