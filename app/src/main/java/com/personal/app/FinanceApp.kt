package com.personal.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.personal.app.ui.components.BlobBackground
import com.personal.app.ui.components.BubbleNavBar
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.rememberNavBarScrollState
import com.personal.app.ui.navigation.AppNavHost
import com.personal.app.ui.navigation.Destination
import com.personal.app.ui.navigation.navigateToTab

/**
 * Root of the UI. Layers, bottom to top: blob background, the current screen, the floating bar.
 * The screens draw edge-to-edge under the bar; the nested-scroll connection installed here is
 * what lets any scrolling screen collapse or expand the bar.
 */
@Composable
fun FinanceApp(
    animatedBackground: Boolean = true,
    blurBackground: Boolean = true,
) {
    val navController = rememberNavController()
    val scrollState = rememberNavBarScrollState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.fromRoute(backStackEntry?.destination?.route) ?: Destination.start

    Box(Modifier.fillMaxSize()) {
        BlobBackground(animated = animatedBackground, blur = blurBackground)

        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollState.connection),
        ) {
            AppNavHost(navController)
        }

        BubbleNavBar(
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
