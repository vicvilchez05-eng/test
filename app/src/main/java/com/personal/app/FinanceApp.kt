package com.personal.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.personal.app.ui.components.LocalMoneyDisplay
import com.personal.app.ui.components.MoneyDisplay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.personal.app.ui.components.AmbientBackground
import com.personal.app.ui.components.BubbleNavBar
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.rememberNavBarScrollState
import com.personal.app.ui.navigation.AppNavHost
import com.personal.app.ui.navigation.Destination
import com.personal.app.ui.navigation.Routes
import com.personal.app.ui.navigation.navigateToTab

/**
 * Root of the UI. Layers, bottom to top: ambient glow (tone follows the current tab), the
 * current screen, the floating bar. The nested-scroll connection installed here is what lets any
 * scrolling screen collapse or expand the bar.
 */
@Composable
fun FinanceApp(
    animatedBackground: Boolean = true,
    blurBackground: Boolean = true,
) {
    val container = LocalAppContainer.current
    val prefs by container.preferences.prefs.collectAsState()
    val navController = rememberNavController()
    val scrollState = rememberNavBarScrollState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val current = Destination.fromRoute(route) ?: Destination.start
    val onForm = route != null && Destination.fromRoute(route) == null

    CompositionLocalProvider(LocalMoneyDisplay provides MoneyDisplay(prefs.currency, prefs.privacyMode)) {
    Box(Modifier.fillMaxSize()) {
        AmbientBackground(tone = current.tone, animated = animatedBackground, blur = blurBackground)

        Box(Modifier.fillMaxSize().nestedScroll(scrollState.connection)) {
            AppNavHost(navController)
        }

        if (!onForm) BubbleNavBar(
            destinations = Destination.entries,
            selected = current,
            collapsed = scrollState.collapsed,
            onSelect = { destination ->
                scrollState.expand()
                if (destination != current) navController.navigateToTab(destination)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = NavBarBottomMargin),
        )
    }
    }
}
