package com.personal.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.personal.app.R
import com.personal.app.ui.theme.AppColors

// Phase 1: every screen is a boilerplate placeholder. Real content arrives in Phase 3.

@Composable
fun HomeScreen() = PlaceholderScreen(
    title = stringResource(R.string.home_title),
    subtitle = stringResource(R.string.home_subtitle),
    heroTint = AppColors.Indigo,
    sections = listOf(
        PlaceholderSection("Quick add", "Phase 2"),
        PlaceholderSection("Latest transactions", "Phase 3"),
        PlaceholderSection("This week", "Phase 4"),
        PlaceholderSection("Upcoming", "Phase 3"),
        PlaceholderSection("Budgets", "Phase 4"),
        PlaceholderSection("Insights", "Phase 4"),
        PlaceholderSection("Tips", "Phase 3"),
    ),
)

@Composable
fun AccountsScreen() = PlaceholderScreen(
    title = stringResource(R.string.accounts_title),
    subtitle = stringResource(R.string.accounts_subtitle),
    heroTint = AppColors.Teal,
    sections = listOf(
        PlaceholderSection("Linked banks", "Phase 2"),
        PlaceholderSection("Cards", "Phase 3"),
        PlaceholderSection("Cash", "Phase 3"),
        PlaceholderSection("Add account", "Phase 2"),
    ),
)

@Composable
fun TotalBalanceScreen() = PlaceholderScreen(
    title = stringResource(R.string.balance_title),
    subtitle = stringResource(R.string.balance_subtitle),
    heroTint = AppColors.Violet,
    sections = listOf(
        PlaceholderSection("Net worth over time", "Phase 4"),
        PlaceholderSection("Spending by category", "Phase 4"),
        PlaceholderSection("Monthly summary", "Phase 4"),
        PlaceholderSection("Export", "Phase 4"),
    ),
)

@Composable
fun SettingsScreen() = PlaceholderScreen(
    title = stringResource(R.string.settings_title),
    subtitle = stringResource(R.string.settings_subtitle),
    heroTint = AppColors.Amber,
    sections = listOf(
        PlaceholderSection("Currency", "Phase 3"),
        PlaceholderSection("Theme", "Phase 3"),
        PlaceholderSection("Bank connection", "Phase 2"),
        PlaceholderSection("Data & backup", "Phase 4"),
    ),
)

@Composable
fun ProfileScreen() = PlaceholderScreen(
    title = stringResource(R.string.profile_title),
    subtitle = stringResource(R.string.profile_subtitle),
    heroTint = AppColors.Rose,
    sections = listOf(
        PlaceholderSection("Your details", "Phase 3"),
        PlaceholderSection("Privacy", "Phase 3"),
        PlaceholderSection("About", "Phase 3"),
    ),
)
