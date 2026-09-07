package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.GlassCard
import com.personal.app.ui.components.GlassRowGroup
import com.personal.app.ui.components.GlassRowItem
import com.personal.app.ui.components.HeaderAction
import com.personal.app.ui.components.NavBarBottomMargin
import com.personal.app.ui.components.NavBarExpandedHeight
import com.personal.app.ui.components.ScreenHeader
import com.personal.app.ui.theme.AppColors

/** A group of future features, rendered as a rows card. */
data class PlaceholderGroup(val rows: List<Pair<String, String>>)

/** One of the quick-action buttons under the hero (icon in a circle + tiny label). */
data class QuickAction(val icon: ImageVector, val label: String)

/**
 * Shared skeleton for the five Phase 1 screens, laid out like the guide: tracked-caps header,
 * a centred hero card with the big number, optional quick actions, then grouped row cards.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    heroLabel: String,
    heroValue: String,
    heroCaption: String,
    groups: List<PlaceholderGroup>,
    leading: HeaderAction? = null,
    trailing: List<HeaderAction> = emptyList(),
    quickActions: List<QuickAction> = emptyList(),
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topInset + 4.dp,
            bottom = bottomInset + NavBarExpandedHeight + NavBarBottomMargin + 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(title = title, leading = leading, trailing = trailing) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(heroLabel, style = MaterialTheme.typography.titleSmall, color = onBackground)
                    Spacer(Modifier.height(6.dp))
                    Text(heroValue, style = MaterialTheme.typography.displaySmall, color = onBackground)
                    Spacer(Modifier.height(8.dp))
                    Pill(text = heroCaption)
                    if (quickActions.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            quickActions.forEach { action ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(action.icon, contentDescription = action.label, tint = onBackground, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(action.label, style = MaterialTheme.typography.labelSmall, color = muted)
                                }
                            }
                        }
                    }
                }
            }
        }
        groups.forEach { group ->
            item {
                GlassRowGroup(
                    rows = group.rows.map { (name, phase) ->
                        GlassRowItem(title = name, value = stringResource(R.string.placeholder_phase, phase))
                    },
                )
            }
        }
    }
}

/** Small tinted pill, like the guide's "+ Trend" chip. */
@Composable
private fun Pill(text: String) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppColors.GreenSoft)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = AppColors.Green)
    }
}
