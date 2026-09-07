package com.personal.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.personal.app.R

/** The five top-level screens, in bottom-bar order. */
enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Rounded.Home),
    Accounts("accounts", R.string.nav_accounts, Icons.Rounded.AccountBalanceWallet),
    Balance("balance", R.string.nav_balance, Icons.Rounded.Insights),
    Settings("settings", R.string.nav_settings, Icons.Rounded.Settings),
    Profile("profile", R.string.nav_profile, Icons.Rounded.Person);

    companion object {
        val start = Home
        fun fromRoute(route: String?): Destination? = entries.firstOrNull { it.route == route }
    }
}
