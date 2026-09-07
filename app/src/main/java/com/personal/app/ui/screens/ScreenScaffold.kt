package com.personal.app.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.NavBarExpandedHeight

/**
 * Esforia's screen frame: `padding: calc(22px + safe-top) 18px 28px`. The status-bar inset is
 * paid here, by the content, so the ambient glow paints edge to edge behind the bar.
 * Items are laid out with no implicit spacing: each component carries Esforia's own margins.
 */
@Composable
fun ScreenScaffold(content: LazyListScope.() -> Unit) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("screen_list"),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = topInset + 22.dp,
            bottom = bottomInset + NavBarExpandedHeight + NavBarBottomMargin + 28.dp,
        ),
        content = content,
    )
}
