package com.personal.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.personal.app.R
import com.personal.app.ui.components.HeaderAction

// Phase 1: every screen is a boilerplate placeholder laid out like the guide.
// Real content arrives in Phase 3; header actions do nothing yet.

@Composable
fun HomeScreen() = PlaceholderScreen(
    title = stringResource(R.string.home_title),
    heroLabel = stringResource(R.string.balance_title),
    heroValue = "0,00 €",
    heroCaption = stringResource(R.string.placeholder_trend),
    trailing = listOf(
        HeaderAction(Icons.Outlined.Sync, "Sync"),
        HeaderAction(Icons.Outlined.NotificationsNone, "Notifications"),
    ),
    quickActions = listOf(
        QuickAction(Icons.Outlined.Add, stringResource(R.string.quick_add)),
        QuickAction(Icons.Outlined.Bolt, stringResource(R.string.quick_actions)),
        QuickAction(Icons.Outlined.Tune, stringResource(R.string.nav_settings)),
    ),
    groups = listOf(
        PlaceholderGroup(listOf("Latest transactions" to "Phase 3", "Quick add" to "Phase 2", "Overview" to "Phase 3")),
        PlaceholderGroup(listOf("This week" to "Phase 4", "Budgets" to "Phase 4", "Upcoming" to "Phase 3")),
        PlaceholderGroup(listOf("Insights" to "Phase 4", "Tips" to "Phase 3")),
        PlaceholderGroup(listOf("Recurring payments" to "Phase 3", "Savings goals" to "Phase 4", "Alerts" to "Phase 3")),
        PlaceholderGroup(listOf("Categories" to "Phase 3", "Search" to "Phase 3", "Archive" to "Phase 4")),
    ),
)

@Composable
fun AccountsScreen() = PlaceholderScreen(
    title = stringResource(R.string.accounts_title),
    heroLabel = stringResource(R.string.accounts_subtitle),
    heroValue = "0",
    heroCaption = stringResource(R.string.placeholder_phase, "Phase 2"),
    trailing = listOf(HeaderAction(Icons.Outlined.Add, "Add account")),
    groups = listOf(
        PlaceholderGroup(listOf("Linked banks" to "Phase 2", "Cards" to "Phase 3", "Cash" to "Phase 3")),
        PlaceholderGroup(listOf("Add account" to "Phase 2", "Manual entries" to "Phase 2")),
    ),
)

@Composable
fun TotalBalanceScreen() = PlaceholderScreen(
    title = stringResource(R.string.balance_title),
    heroLabel = stringResource(R.string.balance_subtitle),
    heroValue = "0,00 €",
    heroCaption = stringResource(R.string.placeholder_trend),
    leading = HeaderAction(Icons.AutoMirrored.Outlined.ArrowBack, "Back"),
    groups = listOf(
        PlaceholderGroup(listOf("Weekly summary" to "Phase 4", "Monthly summary" to "Phase 4", "Spending chart" to "Phase 4")),
        PlaceholderGroup(listOf("Export report (PDF)" to "Phase 4", "Export data (CSV)" to "Phase 4")),
    ),
)

@Composable
fun SettingsScreen() = PlaceholderScreen(
    title = stringResource(R.string.settings_title),
    heroLabel = stringResource(R.string.settings_subtitle),
    heroValue = "EUR",
    heroCaption = stringResource(R.string.placeholder_phase, "Phase 3"),
    groups = listOf(
        PlaceholderGroup(listOf("Currency" to "Phase 3", "Notifications" to "Phase 3", "Dark mode" to "Phase 3")),
        PlaceholderGroup(listOf("Manage bank APIs" to "Phase 2", "Manual entries" to "Phase 2", "Reports" to "Phase 4")),
        PlaceholderGroup(listOf("Data & backup" to "Phase 4", "Support & help" to "Phase 3")),
    ),
)

@Composable
fun ProfileScreen() = PlaceholderScreen(
    title = stringResource(R.string.profile_title),
    heroLabel = stringResource(R.string.profile_subtitle),
    heroValue = "Vic",
    heroCaption = stringResource(R.string.placeholder_phase, "Phase 3"),
    groups = listOf(
        PlaceholderGroup(listOf("Profile info" to "Phase 3", "Linked banks" to "Phase 2", "Security" to "Phase 3")),
        PlaceholderGroup(listOf("Privacy" to "Phase 3", "About" to "Phase 3")),
    ),
)
