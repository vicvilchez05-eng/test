package com.personal.app.ui.screens.navygold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.personal.app.ui.components.navygold.DockedNavExpandedHeight

/**
 * NavyGold screen frame. No top padding: the first item is the [BandHeader], which pays the
 * status-bar inset and runs edge to edge. Side padding is applied per item (the band is full
 * width, everything else is inset 16dp).
 */
@Composable
fun NavyScaffold(content: LazyListScope.() -> Unit) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("screen_list"),
        contentPadding = PaddingValues(bottom = bottomInset + DockedNavExpandedHeight + 24.dp),
        content = content,
    )
}

/** Horizontal inset used by every item below the band. */
val NavyInset = 16.dp
