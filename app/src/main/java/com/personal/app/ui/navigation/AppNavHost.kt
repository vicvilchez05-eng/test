package com.personal.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.personal.app.ui.screens.AccountsScreen
import com.personal.app.ui.screens.HomeScreen
import com.personal.app.ui.screens.ProfileScreen
import com.personal.app.ui.screens.SettingsScreen
import com.personal.app.ui.screens.TotalBalanceScreen
import com.personal.app.ui.screens.navygold.NavyAccountsScreen
import com.personal.app.ui.screens.navygold.NavyBalanceScreen
import com.personal.app.ui.screens.navygold.NavyHomeScreen
import com.personal.app.ui.screens.navygold.NavyProfileScreen
import com.personal.app.ui.screens.navygold.NavySettingsScreen
import com.personal.app.ui.theme.LocalSkin
import com.personal.app.ui.theme.Skin

/** Each skin has its own screens where the layouts differ; both sets stay in the tree. */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val skin = LocalSkin.current
    NavHost(
        navController = navController,
        startDestination = Destination.start.route,
        modifier = modifier,
        // Cross-fade between tabs; the background stays put, which is what makes it feel fluid.
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) },
    ) {
        composable(Destination.Home.route) { if (skin == Skin.Esforia) HomeScreen() else NavyHomeScreen() }
        composable(Destination.Accounts.route) { if (skin == Skin.Esforia) AccountsScreen() else NavyAccountsScreen() }
        composable(Destination.Balance.route) { if (skin == Skin.Esforia) TotalBalanceScreen() else NavyBalanceScreen() }
        composable(Destination.Settings.route) { if (skin == Skin.Esforia) SettingsScreen() else NavySettingsScreen() }
        composable(Destination.Profile.route) { if (skin == Skin.Esforia) ProfileScreen() else NavyProfileScreen() }
    }
}

/** Standard bottom-bar navigation: one back-stack entry per tab, state saved and restored. */
fun NavHostController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
