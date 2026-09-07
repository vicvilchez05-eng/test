package com.personal.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.personal.app.ui.screens.AccountsScreen
import com.personal.app.ui.screens.AddAccountScreen
import com.personal.app.ui.screens.AddTransactionScreen
import com.personal.app.ui.screens.HomeScreen
import com.personal.app.ui.screens.LinkBankScreen
import com.personal.app.ui.screens.ProfileScreen
import com.personal.app.ui.screens.SettingsScreen
import com.personal.app.ui.screens.TotalBalanceScreen

/** Non-tab routes (forms) slide up over the tabs and hide the bottom bar. */
object Routes {
    const val ADD_TRANSACTION = "add_transaction"
    const val ADD_ACCOUNT = "add_account"
    const val LINK_BANK = "link_bank"
    val forms = setOf(ADD_TRANSACTION, ADD_ACCOUNT, LINK_BANK)
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val back: () -> Unit = { navController.popBackStack() }
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
        composable(Destination.Home.route) {
            HomeScreen(
                onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                onLinkBank = { navController.navigate(Routes.LINK_BANK) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
            )
        }
        composable(Destination.Accounts.route) {
            AccountsScreen(
                onLinkBank = { navController.navigate(Routes.LINK_BANK) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
            )
        }
        composable(Destination.Balance.route) { TotalBalanceScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
        composable(Destination.Profile.route) { ProfileScreen() }

        val slideIn = slideInVertically(tween(260)) { it / 6 } + fadeIn(tween(200))
        val slideOut = slideOutVertically(tween(200)) { it / 6 } + fadeOut(tween(160))
        composable(Routes.ADD_TRANSACTION, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            AddTransactionScreen(onDone = back, onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) })
        }
        composable(Routes.ADD_ACCOUNT, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            AddAccountScreen(onDone = back)
        }
        composable(Routes.LINK_BANK, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            LinkBankScreen(onDone = back)
        }
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
