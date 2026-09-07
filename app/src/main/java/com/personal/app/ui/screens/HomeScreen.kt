package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.SouthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.Account
import com.personal.app.data.model.Transaction
import com.personal.app.ui.components.BarChart
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.CircleIconButton
import com.personal.app.ui.components.LocalMoneyDisplay
import com.personal.app.ui.components.money
import com.personal.app.ui.components.pressable
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.softShadow
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import com.personal.app.ui.viewmodel.HomeViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Home. Structure from the NavyGold study (HANDOFF D-024): greeting + avatar, centred hero with
 * the total and a trend line, account tile strip, monthly expenses as bars, recent transactions.
 * Every surface is Esforia's. Data comes from HomeViewModel (Phase 2).
 */

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onLinkBank: () -> Unit,
    onAddAccount: () -> Unit,
    onAccount: (String) -> Unit,
    onAllTransactions: () -> Unit,
) {
    val vm = appViewModel { HomeViewModel(it.repository, it.preferences) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).replaceFirstChar { it.uppercase() } }

    ScreenScaffold {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(4.dp))
                    val greeting = if (s.userName.isBlank()) stringResource(R.string.greeting_hello_anon) else stringResource(R.string.greeting_hello, s.userName)
                    Text(greeting, style = MaterialTheme.typography.headlineMedium, color = p.ink)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                    CircleIconButton(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_transaction_title), onClick = onAddTransaction, size = 38.dp, modifier = Modifier.testTag("home_add"))
                    Avatar(initial = s.userName.trim().take(1).uppercase().ifEmpty { "·" })
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.hero_total_balance).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(6.dp))
                    Text(money(s.totalMinor), style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                    Spacer(Modifier.height(6.dp))
                    val pct = s.monthOverMonthPercent
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (pct != null) {
                            Icon(if (pct >= 0) Icons.Outlined.NorthEast else Icons.Outlined.SouthEast, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(String.format(Locale.getDefault(), "%+.1f %% · %s", pct, stringResource(R.string.this_month)), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        } else {
                            Text(stringResource(R.string.no_history_yet), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
                val privacy = LocalMoneyDisplay.current.privacy
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .pressable(vm::togglePrivacy)
                        .testTag("home_privacy"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(if (privacy) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = stringResource(R.string.row_privacy_mode), tint = Color.White, modifier = Modifier.size(15.dp))
                }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        if (!s.hasData) {
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.empty_home_title), style = MaterialTheme.typography.titleMedium, color = p.ink)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.empty_home_body), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(14.dp))
                    OutlineButton(stringResource(R.string.link_bank_title), onClick = onLinkBank, modifier = Modifier.testTag("home_link_bank"))
                    Spacer(Modifier.height(8.dp))
                    OutlineButton(stringResource(R.string.add_account_manually), onClick = onAddAccount, modifier = Modifier.testTag("home_add_account"))
                }
            }
        } else {
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 0.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.accounts.size) { i -> AccountTile(s.accounts[i], onClick = { onAccount(s.accounts[i].id) }) }
                }
            }
            if (s.expenseShares.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.section_monthly_expenses))
                    SurfaceCard(Modifier.fillMaxWidth()) {
                        BarChart(s.expenseShares.map { (cat, f) -> stringResource(cat.labelRes) to f })
                    }
                }
            }
            item {
                SectionLabel(stringResource(R.string.section_recent_transactions), trailing = {
                    if (s.recent.isNotEmpty()) Box(Modifier.pressable(onAllTransactions).testTag("home_all_transactions")) { Chip(stringResource(R.string.view_all)) }
                })
                if (s.recent.isEmpty()) {
                    SurfaceCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.no_transactions_yet), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                        Spacer(Modifier.height(12.dp))
                        OutlineButton(stringResource(R.string.add_transaction_title), onClick = onAddTransaction)
                    }
                } else {
                    SurfaceCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                        s.recent.forEachIndexed { i, tx ->
                            TransactionRow(tx)
                            if (i < s.recent.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 40.dp).height(1.dp).background(p.line))
                        }
                    }
                }
            }
        }
    }
}

/** Round avatar with the brand gradient and the user's initial; same size as the header buttons. */
@Composable
private fun Avatar(initial: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Box(
        modifier
            .size(38.dp)
            .softShadow(CircleShape, 14.dp, p.heroShadow, offsetY = 6.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(p.financeGradient, start = Offset.Zero, end = Offset.Infinite))
            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, style = MaterialTheme.typography.titleSmall, color = p.onAccent)
    }
}

/** One account in the horizontal strip: an Esforia card with an icon tile, name and amount. */
@Composable
private fun AccountTile(account: Account, onClick: () -> Unit) {
    val p = LocalPalette.current
    SurfaceCard(Modifier.width(132.dp).testTag("tile_${account.id}"), contentPadding = PaddingValues(14.dp), onClick = onClick) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(p.mossSoft), contentAlignment = Alignment.Center) {
            Icon(account.type.icon, contentDescription = null, tint = p.mossText, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(account.name, style = MaterialTheme.typography.bodySmall, color = p.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(money(account.balanceMinor, account.currency), style = MonoText, color = if (account.balanceMinor < 0) p.negative else p.ink, maxLines = 1)
    }
}

/** One transaction line: soft icon tile, description, amount in mono coloured by sign. */
@Composable
fun TransactionRow(tx: Transaction) {
    val p = LocalPalette.current
    val positive = tx.amountMinor > 0
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(if (positive) p.emberSoft else p.mossSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tx.category.icon, contentDescription = null, tint = if (positive) p.emberText else p.mossText, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(tx.description, style = MaterialTheme.typography.bodyLarge, color = p.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(money(tx.amountMinor, tx.currency, signed = true), style = MonoText, color = if (positive) p.emberText else p.ink)
    }
}
