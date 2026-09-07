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
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.personal.app.ui.screens.AccountDetailScreen
import com.personal.app.ui.screens.CurrencyScreen
import com.personal.app.ui.screens.TransactionsScreen
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
    const val CURRENCY = "settings/currency"
    const val TRANSACTIONS = "transactions?accountId={accountId}"
    const val ACCOUNT = "account/{id}"
    val forms = setOf(ADD_TRANSACTION, ADD_ACCOUNT, LINK_BANK, CURRENCY, TRANSACTIONS, ACCOUNT)
    fun transactions(accountId: String? = null) = if (accountId == null) "transactions" else "transactions?accountId=$accountId"
    fun account(id: String) = "account/$id"
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
                onAccount = { navController.navigate(Routes.account(it)) },
                onAllTransactions = { navController.navigate(Routes.transactions()) },
            )
        }
        composable(Destination.Accounts.route) {
            AccountsScreen(
                onLinkBank = { navController.navigate(Routes.LINK_BANK) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccount = { navController.navigate(Routes.account(it)) },
            )
        }
        composable(Destination.Balance.route) { TotalBalanceScreen() }
        composable(Destination.Settings.route) {
            SettingsScreen(
                onCurrency = { navController.navigate(Routes.CURRENCY) },
                onAccounts = { navController.navigateToTab(Destination.Accounts) },
                onExport = { navController.navigateToTab(Destination.Balance) },
            )
        }
        composable(Destination.Profile.route) { ProfileScreen(onLinkBank = { navController.navigate(Routes.LINK_BANK) }) }

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
        composable(Routes.CURRENCY, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            CurrencyScreen(onDone = back)
        }
        composable(
            Routes.TRANSACTIONS,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType; nullable = true; defaultValue = null }),
            enterTransition = { slideIn }, popExitTransition = { slideOut },
        ) { entry ->
            TransactionsScreen(accountId = entry.arguments?.getString("accountId"), onDone = back)
        }
        composable(
            Routes.ACCOUNT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = { slideIn }, popExitTransition = { slideOut },
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            AccountDetailScreen(accountId = id, onDone = back, onAllTransactions = { navController.navigate(Routes.transactions(id)) })
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
