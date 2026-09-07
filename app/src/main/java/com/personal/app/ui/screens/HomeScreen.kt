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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.BarChart
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.softShadow
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Home. Structure inherited from the NavyGold study (HANDOFF D-024): greeting + title + avatar,
 * centred hero with the total and a trend line, a horizontal strip of account tiles, monthly
 * expenses as bars, then recent transactions. Every surface is Esforia's.
 *
 * Phase 1: the figures are ILLUSTRATIVE SAMPLE DATA so the layout can be judged with content.
 */

private const val USER = "Vic"

@Composable
fun HomeScreen() {
    val p = LocalPalette.current
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).replaceFirstChar { it.uppercase() } }

    ScreenScaffold {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.greeting_hello, USER), style = MaterialTheme.typography.headlineMedium, color = p.ink)
                }
                Avatar(initial = USER.take(1), modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.hero_total_balance).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.height(6.dp))
                    Text("45.820,50 €", style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.NorthEast, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("+2,4 % · ${stringResource(R.string.this_month)}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { AccountTile(Icons.Outlined.AccountBalance, "Chase Checking", "32.100 €") }
                item { AccountTile(Icons.Outlined.CreditCard, "BBVA Crédito", "-1.450 €", negative = true) }
                item { AccountTile(Icons.Outlined.Payments, stringResource(R.string.stat_cash), "5.120 €") }
                item { AccountTile(Icons.Outlined.CurrencyBitcoin, "Crypto", "1.570 €") }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_monthly_expenses))
            SurfaceCard(Modifier.fillMaxWidth()) {
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
            SectionLabel(stringResource(R.string.section_recent_transactions))
            SurfaceCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                val rows = listOf(
                    Tx(Icons.Outlined.Home, stringResource(R.string.tx_rent), "-1.200,00 €", false),
                    Tx(Icons.Outlined.LocalCafe, "Starbucks", "-7,50 €", false),
                    Tx(Icons.Outlined.LocalGasStation, stringResource(R.string.tx_fuel), "-55,00 €", false),
                    Tx(Icons.Outlined.Work, stringResource(R.string.tx_salary), "+5.500,00 €", true),
                    Tx(Icons.Outlined.ShoppingCart, "Mercadona", "-84,20 €", false),
                )
                rows.forEachIndexed { i, tx ->
                    TransactionRow(tx)
                    if (i < rows.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 40.dp).height(1.dp).background(p.line))
                }
            }
        }
    }
}

private data class Tx(val icon: ImageVector, val name: String, val amount: String, val positive: Boolean)

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
private fun AccountTile(icon: ImageVector, name: String, amount: String, negative: Boolean = false) {
    val p = LocalPalette.current
    SurfaceCard(Modifier.width(128.dp), contentPadding = PaddingValues(14.dp)) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(p.mossSoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = p.mossText, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.bodySmall, color = p.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(amount, style = MonoText, color = if (negative) p.negative else p.ink, maxLines = 1)
    }
}

/** One transaction line: soft icon tile, name, amount in mono coloured by sign. */
@Composable
private fun TransactionRow(tx: Tx) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(if (tx.positive) p.emberSoft else p.mossSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tx.icon, contentDescription = null, tint = if (tx.positive) p.emberText else p.mossText, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(tx.name, style = MaterialTheme.typography.bodyLarge, color = p.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(tx.amount, style = MonoText, color = if (tx.positive) p.emberText else p.ink)
    }
}
