package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.GlassCard
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.NavBarExpandedHeight

/** A section the real screen will grow into. */
data class PlaceholderSection(val title: String, val phase: String, val tint: Color = Color.Unspecified)

/**
 * Shared skeleton for the five Phase 1 screens: big title, hero glass card and one glass card per
 * future section. Enough content to scroll, so the bubble bar behaviour can be felt right away.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    heroTint: Color,
    sections: List<PlaceholderSection>,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_list"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = topInset + 28.dp,
            bottom = bottomInset + NavBarExpandedHeight + NavBarBottomMargin + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), tint = heroTint) {
                Text(
                    stringResource(R.string.placeholder_hero_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text("0,00 €", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(sections) { section ->
            GlassCard(modifier = Modifier.fillMaxWidth(), tint = section.tint) {
                Text(section.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.placeholder_phase, section.phase),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
