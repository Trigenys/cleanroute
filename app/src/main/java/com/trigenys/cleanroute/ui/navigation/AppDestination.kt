package com.trigenys.cleanroute.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.trigenys.cleanroute.R

enum class AppDestination(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        R.string.nav_home,
        Icons.Filled.Home,
        Icons.Outlined.Home
    ),
    COLLECTION(
        R.string.nav_collection,
        Icons.Filled.LocalShipping,
        Icons.Outlined.LocalShipping
    ),
    CLIENTS(
        R.string.nav_clients,
        Icons.Filled.Group,
        Icons.Outlined.Group
    ),
    MORE(
        R.string.nav_more,
        Icons.Filled.MoreHoriz,
        Icons.Outlined.MoreHoriz
    )
}
