package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.HeroCard
import com.personal.app.ui.components.HeroStat
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SectionLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText

@Composable
fun TotalBalanceScreen() {
    val p = LocalPalette.current
    val phase4 = stringResource(R.string.phase_4)
    ScreenScaffold {
        item {
            PageHeader(title = stringResource(R.string.balance_title), eyebrow = stringResource(R.string.balance_eyebrow))
            Spacer(Modifier.height(20.dp))
        }
        item {
            HeroCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.hero_net_worth).uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(6.dp))
                Text("0,00 €", style = MaterialTheme.typography.displaySmall, color = p.onAccent)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(stringResource(R.string.stat_weekly_spending), "0,00 €", Icons.Outlined.TrendingDown)
                    HeroStat(stringResource(R.string.stat_monthly_income), "0,00 €", Icons.Outlined.TrendingUp, highlighted = true)
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_weekly_summary))
            SurfaceCard(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(12.dp)).background(p.mossSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.placeholder_chart, phase4), style = MaterialTheme.typography.bodySmall, color = p.mossText)
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SurfaceCard(Modifier.weight(1f)) {
                    Text(stringResource(R.string.section_weekly_summary), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                    Spacer(Modifier.height(6.dp))
                    Text("0,00 €", style = MonoText.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize), color = p.ink)
                    Text(stringResource(R.string.stat_spending), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                }
                SurfaceCard(Modifier.weight(1f)) {
                    Text(stringResource(R.string.section_monthly_summary), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                    Spacer(Modifier.height(6.dp))
                    Text("+0,00 €", style = MonoText.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize), color = p.emberText)
                    Text(stringResource(R.string.stat_income), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                }
            }
        }
        item {
            SectionLabel(stringResource(R.string.section_export))
            OutlineButton(stringResource(R.string.export_pdf), onClick = {}, trailingIcon = Icons.Outlined.Description)
            Spacer(Modifier.height(10.dp))
            OutlineButton(stringResource(R.string.export_csv), onClick = {}, trailingIcon = Icons.Outlined.TableChart)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.placeholder_phase, phase4), style = MaterialTheme.typography.bodySmall, color = p.inkSoft, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
