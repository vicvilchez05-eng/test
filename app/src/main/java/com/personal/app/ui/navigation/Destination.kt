package com.personal.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.personal.app.R
import com.personal.app.ui.components.AmbientTone

/**
 * The five top-level screens, in bottom-bar order. Outlined icon at rest, filled when active
 * (Esforia thickens the lucide stroke; Material icons don't have a stroke to thicken).
 */
enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val tone: AmbientTone,
) {
    Home("home", R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home, AmbientTone.Home),
    Accounts("accounts", R.string.nav_accounts, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet, AmbientTone.Finance),
    Balance("balance", R.string.nav_balance, Icons.Outlined.PieChart, Icons.Filled.PieChart, AmbientTone.Progress),
    Settings("settings", R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings, AmbientTone.Settings),
    Profile("profile", R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person, AmbientTone.Profile);

    companion object {
        val start = Home
        fun fromRoute(route: String?): Destination? = entries.firstOrNull { it.route == route }
    }
}
