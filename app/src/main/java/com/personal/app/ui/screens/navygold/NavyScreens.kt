package com.personal.app.ui.screens.navygold

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.EsforiaSwitch
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.navygold.AccountCard
import com.personal.app.ui.components.navygold.BandHeader
import com.personal.app.ui.components.navygold.BarChart
import com.personal.app.ui.components.navygold.DonutChart
import com.personal.app.ui.components.navygold.GradientCard
import com.personal.app.ui.components.navygold.MiniAccountCard
import com.personal.app.ui.components.navygold.NavyButton
import com.personal.app.ui.components.navygold.NavyHero
import com.personal.app.ui.components.navygold.SectionTitle
import com.personal.app.ui.components.navygold.SmallOutlineButton
import com.personal.app.ui.components.navygold.TransactionRow
import com.personal.app.ui.theme.LocalPalette

/*
 * NavyGold screens. Phase 1 = layout only; the figures are ILLUSTRATIVE SAMPLE DATA so the
 * design can be judged (Esforia screens show zeros instead). Phase 2 replaces them with models.
 */

private const val USER = "Vic"

@Composable
fun NavyHomeScreen() {
    val p = LocalPalette.current
    val greeting = stringResource(R.string.greeting_hello, USER)
    NavyScaffold {
        item {
            BandHeader(stringResource(R.string.home_title), greeting, "V") {
                NavyHero(stringResource(R.string.hero_total_balance), "45.820,50 €", stringResource(R.string.hero_account_balance))
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = NavyInset), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { MiniAccountCard(Icons.Outlined.AccountBalance, "Chase Checking", "32.100 €", p.cardGradients[0]) }
                item { MiniAccountCard(Icons.Outlined.CreditCard, "BBVA Crédito", "-1.450 €", p.cardGradients[1]) }
                item { MiniAccountCard(Icons.Outlined.Payments, stringResource(R.string.stat_cash), "5.120 €", p.cardGradients[2]) }
                item { MiniAccountCard(Icons.Outlined.CurrencyBitcoin, "Crypto", "1.570 €", p.cardGradients[3]) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                SectionTitle(stringResource(R.string.section_monthly_expenses))
                BarChart(
                    listOf(
                        stringResource(R.string.cat_housing) to 0.40f,
                        stringResource(R.string.cat_food) to 0.25f,
                        stringResource(R.string.cat_leisure) to 0.20f,
                        stringResource(R.string.cat_transport) to 0.15f,
                    ),
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                SectionTitle(stringResource(R.string.section_recent_transactions))
                TransactionRow(Icons.Outlined.Home, p.chart[1], stringResource(R.string.tx_rent), "-1.200 €", positive = false)
                TransactionRow(Icons.Outlined.LocalCafe, p.positive, "Starbucks", "-7,50 €", positive = false)
                TransactionRow(Icons.Outlined.LocalGasStation, p.chart[1], stringResource(R.string.tx_fuel), "-55,00 €", positive = false)
                TransactionRow(Icons.Outlined.Work, p.positive, stringResource(R.string.tx_salary), "+5.500 €", positive = true)
                TransactionRow(Icons.Outlined.ShoppingCart, p.chart[3], "Mercadona", "-84,20 €", positive = false)
            }
        }
    }
}

@Composable
fun NavyAccountsScreen() {
    val p = LocalPalette.current
    NavyScaffold {
        item {
            BandHeader(stringResource(R.string.accounts_title), stringResource(R.string.greeting_hello, USER), "V") {
                NavyHero(stringResource(R.string.hero_total_in_accounts), "37.220 €", stringResource(R.string.hero_account_balance))
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountCard(Icons.Outlined.AccountBalance, "Chase Checking", "32.100 €", p.cardGradients[0], listOf(0.3f, 0.35f, 0.3f, 0.5f, 0.45f, 0.6f, 0.55f, 0.8f, 0.75f, 0.9f))
                AccountCard(Icons.Outlined.CreditCard, "BBVA Crédito", "-1.450 €", p.cardGradients[1], listOf(0.8f, 0.7f, 0.75f, 0.6f, 0.5f, 0.55f, 0.4f, 0.35f, 0.3f, 0.2f))
                AccountCard(Icons.Outlined.CurrencyBitcoin, "Crypto Wallet", "1.570 €", p.cardGradients[2], listOf(0.4f, 0.5f, 0.35f, 0.6f, 0.5f, 0.7f, 0.65f, 0.55f, 0.8f, 0.85f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NavyButton(stringResource(R.string.add_account), onClick = {}, modifier = Modifier.weight(1f), leading = Icons.Outlined.Add)
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).background(p.moss),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun NavyBalanceScreen() {
    val p = LocalPalette.current
    NavyScaffold {
        item {
            BandHeader(stringResource(R.string.balance_title), stringResource(R.string.greeting_hello, USER), "V") {
                NavyHero(stringResource(R.string.hero_expenses_month, "Septiembre"), "3.250,78 €", stringResource(R.string.hero_account_balance), positive = false)
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                Spacer(Modifier.height(20.dp))
                DonutChart(
                    listOf(
                        stringResource(R.string.cat_housing) to 0.40f,
                        stringResource(R.string.cat_food) to 0.25f,
                        stringResource(R.string.cat_leisure) to 0.20f,
                        stringResource(R.string.cat_transport) to 0.15f,
                    ),
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallOutlineButton(stringResource(R.string.view_full_report), onClick = {})
                    SmallOutlineButton(stringResource(R.string.filter_by_category), onClick = {})
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                SectionTitle(stringResource(R.string.section_recent_transactions))
                TransactionRow(Icons.Outlined.ShoppingCart, p.chart[1], "Supermercado Mercadona", "-220,00 €", positive = false)
                TransactionRow(Icons.Outlined.Restaurant, p.positive, "Restaurante La Paella", "-75,00 €", positive = false)
                TransactionRow(Icons.Outlined.Movie, p.negative, "Suscripción Netflix", "-15,99 €", positive = false)
                TransactionRow(Icons.Outlined.CardGiftcard, p.chart[3], stringResource(R.string.tx_gift), "-100,00 €", positive = false)
            }
        }
    }
}

@Composable
fun NavySettingsScreen() {
    val p = LocalPalette.current
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
    val phase4 = stringResource(R.string.phase_4)
    NavyScaffold {
        item {
            BandHeader(stringResource(R.string.settings_title), stringResource(R.string.settings_eyebrow), "V") {
                GradientCard(p.cardGradients[0], Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.row_currency), style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(4.dp))
                    Text("EUR · €", style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                Spacer(Modifier.height(22.dp))
                GroupLabel(stringResource(R.string.group_general))
                Group(
                    listOf(
                        GroupRow(stringResource(R.string.row_currency), Icons.Outlined.Paid, "EUR"),
                        GroupRow(stringResource(R.string.row_notifications), Icons.Outlined.NotificationsNone, trailing = { EsforiaSwitch(checked = true, onChange = {}) }),
                        GroupRow(stringResource(R.string.row_dark_mode), Icons.Outlined.DarkMode, trailing = { EsforiaSwitch(checked = p.isDark, onChange = {}) }),
                    ),
                )
                Spacer(Modifier.height(22.dp))
                GroupLabel(stringResource(R.string.group_data))
                Group(
                    listOf(
                        GroupRow(stringResource(R.string.row_bank_apis), Icons.Outlined.AccountBalance, phase2),
                        GroupRow(stringResource(R.string.row_manual_entries), Icons.Outlined.Edit, phase2),
                        GroupRow(stringResource(R.string.row_reports), Icons.Outlined.Summarize, phase4),
                        GroupRow(stringResource(R.string.row_backup), Icons.Outlined.Backup, phase4),
                    ),
                )
                Spacer(Modifier.height(22.dp))
                GroupLabel(stringResource(R.string.group_help))
                Group(
                    listOf(
                        GroupRow(stringResource(R.string.row_support), Icons.Outlined.HelpOutline, phase3),
                        GroupRow(stringResource(R.string.row_about), Icons.Outlined.Info, phase3),
                    ),
                )
            }
        }
    }
}

@Composable
fun NavyProfileScreen() {
    val p = LocalPalette.current
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
    NavyScaffold {
        item {
            BandHeader(stringResource(R.string.profile_title), stringResource(R.string.greeting_hello, USER), "V") {
                GradientCard(p.cardGradients[2], Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(p.cardGradients[0])),
                            contentAlignment = Alignment.Center,
                        ) { Text("V", style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White) }
                        Column {
                            Text(USER, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White)
                            Text(stringResource(R.string.profile_personal_use), style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = NavyInset)) {
                Spacer(Modifier.height(22.dp))
                GroupLabel(stringResource(R.string.group_account))
                Group(
                    listOf(
                        GroupRow(stringResource(R.string.row_profile_info), Icons.Outlined.Person, phase3),
                        GroupRow(stringResource(R.string.row_linked_banks), Icons.Outlined.AccountBalance, phase2),
                        GroupRow(stringResource(R.string.row_security), Icons.Outlined.Lock, phase3),
                    ),
                )
                Spacer(Modifier.height(22.dp))
                GroupLabel(stringResource(R.string.group_privacy))
                Group(
                    listOf(
                        GroupRow(stringResource(R.string.row_privacy), Icons.Outlined.Shield, phase3),
                        GroupRow(stringResource(R.string.row_about), Icons.Outlined.Info, phase3),
                    ),
                )
            }
        }
    }
}
