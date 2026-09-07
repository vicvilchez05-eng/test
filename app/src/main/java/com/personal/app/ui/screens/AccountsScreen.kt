package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.Account
import com.personal.app.data.model.Source
import com.personal.app.data.repository.SyncState
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.money
import com.personal.app.ui.components.HeaderAction
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.HeroStat
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import com.personal.app.ui.viewmodel.AccountsViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AccountsScreen(onLinkBank: () -> Unit, onAddAccount: () -> Unit, onAccount: (String) -> Unit) {
    val vm = appViewModel { AccountsViewModel(it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    val linked = s.accounts.filter { it.source == Source.LINKED }
    val manual = s.accounts.filter { it.source == Source.MANUAL }

    ScreenScaffold {
        item {
            PageHeader(
                title = stringResource(R.string.accounts_title),
                eyebrow = stringResource(R.string.accounts_eyebrow),
                actions = listOf(
                    HeaderAction(Icons.Outlined.Sync, stringResource(R.string.sync), onClick = vm::syncAll),
                    HeaderAction(Icons.Outlined.Add, stringResource(R.string.add_account_title), onClick = onAddAccount),
                ),
            )
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.hero_all_accounts).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Text(money(s.totalMinor), style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_banks), s.connections.size.toString(), Icons.Outlined.Link)
                    HeroStat(stringResource(R.string.stat_accounts), s.accounts.size.toString(), Icons.Outlined.AccountBalanceWallet, highlighted = true)
                }
            }
            when (val sync = s.syncState) {
                SyncState.Syncing -> { Spacer(Modifier.height(10.dp)); Text(stringResource(R.string.syncing), style = MaterialTheme.typography.bodySmall, color = p.inkSoft) }
                is SyncState.Error -> { Spacer(Modifier.height(10.dp)); Text(sync.message, style = MaterialTheme.typography.bodySmall, color = p.danger) }
                SyncState.Idle -> Unit
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_linked))
            if (linked.isEmpty()) {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_linked_banks), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(12.dp))
                    OutlineButton(stringResource(R.string.link_bank_title), onClick = onLinkBank)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    linked.forEach { AccountCard(it, onClick = { onAccount(it.id) }) }
                    OutlineButton(stringResource(R.string.link_another_bank), onClick = onLinkBank)
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_manual))
            if (manual.isEmpty()) {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_manual_accounts), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(12.dp))
                    OutlineButton(stringResource(R.string.add_account_manually), onClick = onAddAccount)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    manual.forEach { AccountCard(it, onClick = { onAccount(it.id) }) }
                    OutlineButton(stringResource(R.string.add_account_manually), onClick = onAddAccount)
                }
            }
        }
    }
}

@Composable
private fun AccountCard(account: Account, onClick: () -> Unit) {
    val p = LocalPalette.current
    SurfaceCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(p.mossSoft), contentAlignment = Alignment.Center) {
                Icon(account.type.icon, contentDescription = null, tint = p.mossText, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.titleMedium, color = p.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                val subtitle = listOfNotNull(account.institution, stringResource(account.type.labelRes)).joinToString(" · ")
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = p.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(money(account.balanceMinor, account.currency), style = MonoText, color = if (account.balanceMinor < 0) p.negative else p.ink)
                Spacer(Modifier.height(4.dp))
                if (account.source == Source.LINKED) {
                    val synced = account.lastSyncedAt?.let { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) }
                    Chip(if (synced != null) stringResource(R.string.synced_at, synced) else stringResource(R.string.linked))
                } else {
                    Chip(stringResource(R.string.manual), background = p.blueSoft, color = p.blue)
                }
            }
        }
    }
}
