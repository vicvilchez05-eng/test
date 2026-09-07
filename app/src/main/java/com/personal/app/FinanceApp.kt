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
import com.personal.app.ui.components.AmbientBackground
import com.personal.app.ui.components.BubbleNavBar
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.rememberNavBarScrollState
import com.personal.app.ui.navigation.AppNavHost
import com.personal.app.ui.navigation.Destination
import com.personal.app.ui.navigation.navigateToTab
import com.personal.app.ui.components.navygold.DockedNavBar
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.LocalSkin
import com.personal.app.ui.theme.Skin
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush

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
    val navController = rememberNavController()
    val scrollState = rememberNavBarScrollState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.fromRoute(backStackEntry?.destination?.route) ?: Destination.start

    val skin = LocalSkin.current
    val p = LocalPalette.current

    Box(Modifier.fillMaxSize()) {
        when (skin) {
            Skin.Esforia -> AmbientBackground(tone = current.tone, animated = animatedBackground, blur = blurBackground)
            Skin.NavyGold -> Box(Modifier.fillMaxSize().background(Brush.verticalGradient(if (p.bg.size > 1) p.bg else listOf(p.bg[0], p.bg[0]))))
        }

        Box(Modifier.fillMaxSize().nestedScroll(scrollState.connection)) {
            AppNavHost(navController)
        }

        val onSelect: (Destination) -> Unit = { destination ->
            scrollState.expand()
            if (destination != current) navController.navigateToTab(destination)
        }
        when (skin) {
            Skin.Esforia -> BubbleNavBar(
                destinations = Destination.entries,
                selected = current,
                collapsed = scrollState.collapsed,
                onSelect = onSelect,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = NavBarBottomMargin),
            )
            Skin.NavyGold -> DockedNavBar(
                destinations = Destination.entries,
                selected = current,
                collapsed = scrollState.collapsed,
                onSelect = onSelect,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
