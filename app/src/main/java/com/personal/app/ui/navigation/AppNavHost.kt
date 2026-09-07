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

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
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
        composable(Destination.Home.route) { HomeScreen() }
        composable(Destination.Accounts.route) { AccountsScreen() }
        composable(Destination.Balance.route) { TotalBalanceScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
        composable(Destination.Profile.route) { ProfileScreen() }
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
