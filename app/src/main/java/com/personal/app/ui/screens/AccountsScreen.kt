package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.theme.LocalPalette

@Composable
fun AccountsScreen() {
    val p = LocalPalette.current
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
    ScreenScaffold {
        item {
            PageHeader(
                title = stringResource(R.string.accounts_title),
                eyebrow = stringResource(R.string.accounts_eyebrow),
                actions = listOf(HeaderAction(Icons.Outlined.Add, stringResource(R.string.action_add_account))),
            )
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.hero_all_accounts).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Text("0,00 €", style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_banks), "0", Icons.Outlined.AccountBalance)
                    HeroStat(stringResource(R.string.stat_cards), "0", Icons.Outlined.CreditCard)
                    HeroStat(stringResource(R.string.stat_cash), "0,00 €", Icons.Outlined.Payments, highlighted = true)
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_linked))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_linked_banks), Icons.Outlined.AccountBalance, phase2),
                    GroupRow(stringResource(R.string.row_cards), Icons.Outlined.CreditCard, phase3),
                    GroupRow(stringResource(R.string.row_cash), Icons.Outlined.Payments, phase3),
                ),
            )
        }
        item {
            SectionLabel(stringResource(R.string.section_manual))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_link_bank), Icons.Outlined.Link, phase2),
                    GroupRow(stringResource(R.string.row_manual_entries), Icons.Outlined.Edit, phase2),
                ),
            )
        }
    }
}
