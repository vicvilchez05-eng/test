package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.HeaderAction
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.HeroStat
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Home, laid out like Esforia's Today: date line, title, gradient hero, summary card, sections. */
@Composable
fun HomeScreen() {
    val p = LocalPalette.current
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
    val phase4 = stringResource(R.string.phase_4)

    ScreenScaffold {
        item {
            PageHeader(
                title = stringResource(R.string.home_title),
                eyebrow = date.replaceFirstChar { it.uppercase() },
                actions = listOf(
                    HeaderAction(Icons.Outlined.Sync, stringResource(R.string.action_sync)),
                    HeaderAction(Icons.Outlined.NotificationsNone, stringResource(R.string.action_notifications)),
                ),
            )
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Text(
                    "${stringResource(R.string.hero_net_available)} · ${stringResource(R.string.this_month)}".uppercase(),
                    style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(6.dp))
                Text("0,00 €", style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_income), "0,00 €", Icons.Outlined.TrendingUp)
                    HeroStat(stringResource(R.string.stat_expenses), "0,00 €", Icons.Outlined.TrendingDown)
                    HeroStat(stringResource(R.string.stat_savings), "0,00 €", Icons.Outlined.Savings, highlighted = true)
                }
            }
        }
        item {
            Spacer(Modifier.height(14.dp))
            SurfaceCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(stringResource(R.string.latest_transactions), style = MaterialTheme.typography.titleMedium, color = p.ink)
                    Text("· ${stringResource(R.string.this_month)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = p.inkSoft)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        stringResource(R.string.stat_income) to p.ink,
                        stringResource(R.string.stat_expenses) to p.ink,
                        stringResource(R.string.hero_net_available) to p.moss,
                    ).forEach { (label, tint) ->
                        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium.copy(fontSize = androidx.compose.ui.unit.TextUnit(10.5f, androidx.compose.ui.unit.TextUnitType.Sp)), color = p.inkSoft, maxLines = 2)
                            Spacer(Modifier.height(3.dp))
                            Text("0,00 €", style = MonoText, color = tint)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.placeholder_phase, phase3), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                Spacer(Modifier.height(12.dp))
                OutlineButton(stringResource(R.string.open_accounts), onClick = {})
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_this_week))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_budgets), Icons.Outlined.Flag, phase4),
                    GroupRow(stringResource(R.string.row_upcoming), Icons.Outlined.CalendarMonth, phase3),
                    GroupRow(stringResource(R.string.row_insights), Icons.Outlined.Insights, phase4),
                ),
            )
        }
        item {
            SectionLabel(stringResource(R.string.section_more))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_recurring), Icons.Outlined.Repeat, phase3),
                    GroupRow(stringResource(R.string.row_savings_goals), Icons.Outlined.Savings, phase4),
                    GroupRow(stringResource(R.string.row_categories), Icons.Outlined.Category, phase3),
                    GroupRow(stringResource(R.string.row_quick_add), Icons.Outlined.TrendingUp, phase2),
                ),
            )
        }
    }
}

@Composable
private fun <T> remember(calculation: () -> T): T = androidx.compose.runtime.remember(calculation)
