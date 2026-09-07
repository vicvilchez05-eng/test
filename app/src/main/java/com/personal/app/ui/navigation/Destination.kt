package com.personal.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.personal.app.R

/** The five top-level screens, in bottom-bar order. Outlined icons, as in the guide. */
enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Outlined.Home),
    Accounts("accounts", R.string.nav_accounts, Icons.Outlined.AccountBalanceWallet),
    Balance("balance", R.string.nav_balance, Icons.Outlined.PieChart),
    Settings("settings", R.string.nav_settings, Icons.Outlined.Settings),
    Profile("profile", R.string.nav_profile, Icons.Outlined.Person);

    companion object {
        val start = Home
        fun fromRoute(route: String?): Destination? = entries.firstOrNull { it.route == route }
    }
}
