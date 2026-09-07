package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.LocalAppContainer
import com.personal.app.R
import com.personal.app.data.model.Money
import com.personal.app.ui.components.BarChart
import com.personal.app.ui.components.DonutChart
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.HeroStat
import com.personal.app.ui.components.LineChart
import com.personal.app.ui.components.LocalMoneyDisplay
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SegmentedToggle
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.money
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import com.personal.app.ui.viewmodel.BalanceViewModel
import com.personal.app.ui.viewmodel.ReportPeriod
import com.personal.app.ui.viewmodel.appViewModel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Total Balance: net worth with assets/liabilities, the 30-day evolution line, a week/month
 * summary (per-day bars or category donut), the last six months, per-account breakdown, and the
 * PDF / CSV exports (Phase 4).
 */
@Composable
fun TotalBalanceScreen() {
    val container = LocalAppContainer.current
    val vm = appViewModel { BalanceViewModel(it.repository, it.preferences, it.exporter) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.export_share_title)
    val privacy = LocalMoneyDisplay.current.privacy

    LaunchedEffect(Unit) {
        vm.share.collect { req ->
            container.exporter?.let { context.startActivity(it.shareIntent(req.file, req.mime, shareTitle)) }
        }
    }

    val shortMoney: (Long) -> String = { if (privacy) "••••" else Money.format(it, s.currency) }
    val dayFmt = DateTimeFormatter.ofPattern("d MMM")

    ScreenScaffold {
        item {
            PageHeader(title = stringResource(R.string.balance_title), eyebrow = stringResource(R.string.balance_eyebrow))
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.hero_net_worth).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Text(money(s.totalMinor), style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                s.monthOverMonthPercent?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(String.format(Locale.getDefault(), "%+.1f %% · %s", it, stringResource(R.string.this_month)), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_assets), money(s.assetsMinor), Icons.Outlined.Savings)
                    HeroStat(stringResource(R.string.stat_liabilities), money(s.liabilitiesMinor), Icons.Outlined.CreditCard, highlighted = true)
                }
            }
        }
        if (s.history.size >= 2) {
            item {
                SectionLabel(stringResource(R.string.section_evolution))
                SurfaceCard(Modifier.fillMaxWidth()) {
                    LineChart(
                        values = s.history.map { it.second },
                        format = shortMoney,
                        startLabel = s.history.first().first.format(dayFmt),
                        endLabel = s.history.last().first.format(dayFmt),
                    )
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_summary))
            SegmentedToggle(
                stringResource(R.string.this_week_label), stringResource(R.string.this_month_label),
                leftSelected = s.period == ReportPeriod.WEEK,
                onChange = { vm.setPeriod(if (it) ReportPeriod.WEEK else ReportPeriod.MONTH) },
            )
            Spacer(Modifier.height(12.dp))
            val sum = s.summary
            if (sum != null) {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text("${sum.start.format(dayFmt)} – ${sum.endInclusive.format(dayFmt)}", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Stat(stringResource(R.string.stat_income), money(sum.incomeMinor), p.emberText, Modifier.weight(1f))
                        Stat(stringResource(R.string.stat_expenses), money(sum.expensesMinor), p.ink, Modifier.weight(1f))
                        Stat(stringResource(R.string.stat_net), money(sum.netMinor), if (sum.netMinor >= 0) p.moss else p.danger, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    if (sum.expensesMinor == 0L) {
                        Text(stringResource(R.string.no_expenses_period), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    } else if (s.period == ReportPeriod.WEEK) {
                        val max = sum.perDayExpenses.maxOf { it.second }.coerceAtLeast(1)
                        BarChart(
                            bars = sum.perDayExpenses.map { (d, v) -> d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replaceFirstChar { it.uppercase() } to v.toFloat() / max },
                            valueLabels = sum.perDayExpenses.map { (_, v) -> if (v == 0L) "–" else shortMoneyCompact(v, s.currency, privacy) },
                            singleColor = p.moss,
                            barHeight = 56.dp,
                        )
                    } else {
                        val total = sum.expensesMinor.toFloat()
                        val top = sum.byCategory.take(5)
                        DonutChart(top.map { (c, v) -> stringResource(c.labelRes) to v / total })
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.avg_daily_expense, shortMoney(sum.averageDailyExpenseMinor)), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                }
            }
        }
        if (s.series.any { it.expensesMinor > 0 || it.incomeMinor > 0 }) {
            item {
                SectionLabel(stringResource(R.string.section_last_6_months))
                SurfaceCard(Modifier.fillMaxWidth()) {
                    val max = s.series.maxOf { it.expensesMinor }.coerceAtLeast(1)
                    BarChart(
                        bars = s.series.map { pt -> pt.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replaceFirstChar { it.uppercase() } to pt.expensesMinor.toFloat() / max },
                        valueLabels = s.series.map { shortMoneyCompact(it.expensesMinor, s.currency, privacy) },
                        singleColor = p.blue,
                        barHeight = 64.dp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.expenses_per_month), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                }
            }
        }
        if (s.accounts.isNotEmpty()) {
            item {
                SectionLabel(stringResource(R.string.section_by_account))
                Group(s.accounts.map { a -> GroupRow(a.name, a.type.icon, money(a.balanceMinor, a.currency), chevron = false) })
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_export))
            OutlineButton(stringResource(R.string.export_pdf), onClick = vm::exportPdf, trailingIcon = Icons.Outlined.Description)
            Spacer(Modifier.height(10.dp))
            OutlineButton(stringResource(R.string.export_csv), onClick = vm::exportCsv, trailingIcon = Icons.Outlined.TableChart)
            Spacer(Modifier.height(8.dp))
            val note = when {
                s.exporting -> stringResource(R.string.export_generating)
                s.exportError != null -> s.exportError!!
                else -> stringResource(R.string.export_note)
            }
            Text(note, style = MaterialTheme.typography.bodySmall, color = if (s.exportError != null) p.danger else p.inkSoft, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = p.inkSoft)
        Spacer(Modifier.height(3.dp))
        Text(value, style = MonoText, color = color, maxLines = 1)
    }
}

/** "1,2k" style for tight bar labels. */
private fun shortMoneyCompact(minor: Long, currency: String, privacy: Boolean): String {
    if (privacy) return "••"
    val units = minor / 100.0
    return when {
        units >= 10_000 -> String.format(Locale.getDefault(), "%.0fk", units / 1000)
        units >= 1_000 -> String.format(Locale.getDefault(), "%.1fk", units / 1000)
        else -> String.format(Locale.getDefault(), "%.0f", units)
    }
}
