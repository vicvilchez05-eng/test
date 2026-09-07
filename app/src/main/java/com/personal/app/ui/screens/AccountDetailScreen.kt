package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.Source
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.ConfirmDialog
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.HeroStat
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.Sparkline
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.money
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.AccountDetailViewModel
import com.personal.app.ui.viewmodel.appViewModel

/** One account: balance hero, 30-day balance line, this month's flows, latest movements, removal. */
@Composable
fun AccountDetailScreen(accountId: String, onDone: () -> Unit, onAllTransactions: () -> Unit) {
    val vm = appViewModel { AccountDetailViewModel(it.repository, accountId) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var confirm by remember { mutableStateOf(false) }
    LaunchedEffect(s.deleted) { if (s.deleted && s.account == null) onDone() }
    val account = s.account ?: return

    ScreenScaffold {
        item { FormHeader(account.name, onBack = onDone) }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((account.institution ?: stringResource(account.type.labelRes)).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                    Chip(stringResource(if (account.source == Source.LINKED) R.string.linked else R.string.manual), background = Color.White.copy(alpha = 0.2f), color = Color.White)
                }
                Spacer(Modifier.height(6.dp))
                Text(money(account.balanceMinor, account.currency), style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_income), money(s.monthIncomeMinor, account.currency), Icons.Outlined.TrendingUp)
                    HeroStat(stringResource(R.string.stat_expenses), money(s.monthExpensesMinor, account.currency), Icons.Outlined.TrendingDown, highlighted = true)
                }
            }
        }
        if (s.history.size >= 2) {
            item {
                SectionLabel(stringResource(R.string.section_last_30_days))
                SurfaceCard(Modifier.fillMaxWidth()) {
                    val values = s.history.map { it.second }
                    val min = values.min(); val max = values.max()
                    val span = (max - min).coerceAtLeast(1L).toFloat()
                    Sparkline(values.map { (it - min) / span }, p.moss, Modifier.fillMaxWidth().height(72.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(money(min, account.currency), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                        Text(money(max, account.currency), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                    }
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_recent_transactions), trailing = {
                if (s.transactionCount > s.recent.size) Chip(stringResource(R.string.view_all_n, s.transactionCount), modifier = Modifier.padding(end = 2.dp))
            })
            if (s.recent.isEmpty()) {
                SurfaceCard(Modifier.fillMaxWidth()) { Text(stringResource(R.string.no_transactions_yet), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft) }
            } else {
                SurfaceCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    s.recent.forEachIndexed { i, tx ->
                        TransactionRow(tx)
                        if (i < s.recent.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 40.dp).height(1.dp).background(p.line))
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlineButton(stringResource(R.string.all_transactions), onClick = onAllTransactions)
            }
        }
        item {
            Spacer(Modifier.height(22.dp))
            Column {
                OutlineButton(
                    stringResource(if (account.source == Source.LINKED) R.string.unlink_bank else R.string.delete_account),
                    onClick = { confirm = true }, trailingIcon = null,
                )
            }
        }
    }

    if (confirm) {
        val linked = account.source == Source.LINKED
        ConfirmDialog(
            title = stringResource(if (linked) R.string.unlink_title else R.string.delete_account_title, account.institution ?: account.name),
            body = stringResource(if (linked) R.string.unlink_body else R.string.delete_account_body),
            confirmText = stringResource(if (linked) R.string.unlink else R.string.delete),
            dismissText = stringResource(R.string.cancel),
            danger = true,
            onConfirm = { confirm = false; if (linked && account.connectionId != null) vm.unlink(account.connectionId) else vm.delete() },
            onDismiss = { confirm = false },
        )
    }
}
