package com.personal.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.personal.app.ui.screens.AccountDetailScreen
import com.personal.app.ui.screens.AccountsScreen
import com.personal.app.ui.screens.AddAccountScreen
import com.personal.app.ui.screens.AddTransactionScreen
import com.personal.app.ui.screens.CaptureSettingsScreen
import com.personal.app.ui.screens.CurrencyScreen
import com.personal.app.ui.screens.InboxScreen
import com.personal.app.ui.screens.HomeScreen
import com.personal.app.ui.screens.LinkBankScreen
import com.personal.app.ui.screens.ProfileScreen
import com.personal.app.ui.screens.SettingsScreen
import com.personal.app.ui.screens.TotalBalanceScreen
import com.personal.app.ui.screens.TransactionsScreen

/**
 * Two layers of navigation:
 *  - the five tabs live in ONE route ([Routes.TABS]) as a [HorizontalPager], so a horizontal
 *    swipe moves between them and the bottom bar just mirrors the pager (HANDOFF D-030);
 *  - forms and detail screens are real routes pushed on top; they slide up and hide the bar.
 */
object Routes {
    const val TABS = "tabs"
    const val ADD_TRANSACTION = "add_transaction"
    const val ADD_ACCOUNT = "add_account"
    const val LINK_BANK = "link_bank"
    const val CURRENCY = "settings/currency"
    const val CAPTURE = "settings/capture"
    const val INBOX = "inbox"
    const val TRANSACTIONS = "transactions?accountId={accountId}"
    const val ACCOUNT = "account/{id}"
    fun transactions(accountId: String? = null) = if (accountId == null) "transactions" else "transactions?accountId=$accountId"
    fun account(id: String) = "account/$id"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    pagerState: PagerState,
    onTab: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val back: () -> Unit = { navController.popBackStack() }
    val slideIn = slideInVertically(tween(260)) { it / 6 } + fadeIn(tween(200))
    val slideOut = slideOutVertically(tween(200)) { it / 6 } + fadeOut(tween(160))

    NavHost(
        navController = navController,
        startDestination = Routes.TABS,
        modifier = modifier,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) },
    ) {
        composable(Routes.TABS) {
            TabsPager(pagerState, navController, onTab)
        }
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
        composable(Routes.CAPTURE, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            CaptureSettingsScreen(onDone = back, onInbox = { navController.navigate(Routes.INBOX) })
        }
        composable(Routes.INBOX, enterTransition = { slideIn }, popExitTransition = { slideOut }) {
            InboxScreen(onDone = back, onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) })
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

/** The five tabs side by side. Neighbours stay composed so a swipe never lands on a blank page. */
@Composable
private fun TabsPager(pagerState: PagerState, navController: NavHostController, onTab: (Destination) -> Unit) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        key = { Destination.entries[it].route },
    ) { page ->
        when (Destination.entries[page]) {
            Destination.Home -> HomeScreen(
                onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                onLinkBank = { navController.navigate(Routes.LINK_BANK) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccount = { navController.navigate(Routes.account(it)) },
                onAllTransactions = { navController.navigate(Routes.transactions()) },
                onInbox = { navController.navigate(Routes.INBOX) },
            )
            Destination.Accounts -> AccountsScreen(
                onLinkBank = { navController.navigate(Routes.LINK_BANK) },
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onAccount = { navController.navigate(Routes.account(it)) },
            )
            Destination.Balance -> TotalBalanceScreen()
            Destination.Settings -> SettingsScreen(
                onCurrency = { navController.navigate(Routes.CURRENCY) },
                onAccounts = { onTab(Destination.Accounts) },
                onExport = { onTab(Destination.Balance) },
                onCapture = { navController.navigate(Routes.CAPTURE) },
            )
            Destination.Profile -> ProfileScreen(onLinkBank = { navController.navigate(Routes.LINK_BANK) })
        }
    }
}
