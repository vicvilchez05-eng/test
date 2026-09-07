package com.personal.app.ui.components.navygold

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.personal.app.ui.components.pressable
import com.personal.app.ui.components.softShadow
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.moneyStyle

/*
 * NavyGold skin primitives (docs/design/navy-gold-*.png).
 */

val BandOverlap = 32.dp
private val BandContentHeight = 132.dp

/**
 * The petrol band at the top of every screen: title (white, bold) and greeting (gold) with a
 * ringed avatar on the right; the [hero] card starts inside the band and overlaps its bottom
 * edge by [BandOverlap]. The band pays the status-bar inset itself so it runs edge to edge.
 */
@Composable
fun BandHeader(
    title: String,
    subtitle: String,
    avatarInitial: String,
    hero: @Composable () -> Unit,
) {
    val p = LocalPalette.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(topInset + BandContentHeight)
                .background(Brush.verticalGradient(p.headerBand)),
        )
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = topInset + 14.dp)) {
            Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(subtitle, style = MaterialTheme.typography.labelLarge, color = p.gold)
                }
                Box(
                    Modifier
                        .size(42.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(p.cardGradients[2])),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(avatarInitial, style = MaterialTheme.typography.titleSmall, color = Color.White)
                }
            }
            Spacer(Modifier.height(BandContentHeight - 14.dp - 58.dp - BandOverlap))
            hero()
        }
    }
}

/** Centred hero: label, big figure (gold gradient in dark), and a trend caption. */
@Composable
fun NavyHero(label: String, value: String, caption: String, modifier: Modifier = Modifier, positive: Boolean = true) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .softShadow(shape, 18.dp, Color.Black.copy(alpha = if (p.isDark) 0.35f else 0.09f), offsetY = 8.dp)
            .clip(shape)
            .background(Brush.linearGradient(p.heroGradient, start = Offset.Zero, end = Offset.Infinite))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = p.heroInk)
        Spacer(Modifier.height(4.dp))
        val valueStyle = MaterialTheme.typography.displaySmall
        if (p.heroValueGradient != null) {
            Text(value, style = valueStyle.copy(brush = Brush.linearGradient(p.heroValueGradient)))
        } else {
            Text(value, style = valueStyle, color = p.heroInk)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.NorthEast, contentDescription = null, tint = if (positive) p.positive else p.negative, modifier = Modifier.size(13.dp))
            Text(caption, style = MaterialTheme.typography.bodyMedium, color = p.heroInkSoft)
        }
    }
}

/** A card filled with one of the skin's accent gradients (135°), white ink. */
@Composable
fun GradientCard(
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    radius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier
            .clip(shape)
            .background(Brush.linearGradient(gradient, start = Offset.Zero, end = Offset.Infinite))
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** An account card: icon in a circle + name, big amount, sparkline on the right. */
@Composable
fun AccountCard(icon: ImageVector, name: String, amount: String, gradient: List<Color>, trend: List<Float>, modifier: Modifier = Modifier) {
    GradientCard(gradient, modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconCircle(icon, size = 26.dp)
                    Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text(amount, style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Sparkline(trend, Color.White.copy(alpha = 0.9f), Modifier.width(96.dp).height(44.dp))
        }
    }
}

/** A small account tile for the horizontal strip on Home. */
@Composable
fun MiniAccountCard(icon: ImageVector, name: String, amount: String, gradient: List<Color>, modifier: Modifier = Modifier) {
    GradientCard(gradient, modifier.width(112.dp), radius = 14.dp, contentPadding = 12.dp) {
        IconCircle(icon, size = 24.dp)
        Spacer(Modifier.height(10.dp))
        Text(name, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(amount, style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize), color = Color.White, maxLines = 1)
    }
}

@Composable
private fun IconCircle(icon: ImageVector, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.58f))
    }
}

/** A thin polyline through normalised points (0..1), rounded joins. */
@Composable
fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (points.size < 2) return@Canvas
        val stepX = size.width / (points.size - 1)
        val path = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - v.coerceIn(0f, 1f) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Solid button: navy in light, raised surface in dark; optional round "+" at the right. */
@Composable
fun NavyButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, leading: ImageVector? = null) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(p.moss)
            .pressable(onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let { Icon(it, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = Color.White)
    }
}

/** Small outlined pill button (the "Ver reporte completo" pair). */
@Composable
fun SmallOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .clip(shape)
            .border(1.dp, if (p.isDark) Color.White.copy(alpha = 0.35f) else p.ink.copy(alpha = 0.6f), shape)
            .pressable(onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = p.ink, maxLines = 1)
    }
}

/** Section title, semibold 16, with the design's spacing. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.headlineSmall, color = LocalPalette.current.ink, modifier = modifier.padding(top = 18.dp, bottom = 10.dp))
}

/** One transaction line: tinted icon circle, name, amount, up/down triangle. */
@Composable
fun TransactionRow(icon: ImageVector, tint: Color, name: String, amount: String, positive: Boolean) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(tint.copy(alpha = if (p.isDark) 0.22f else 0.16f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, color = p.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(amount, style = moneyStyle(), color = p.ink)
        Icon(
            if (positive) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = if (positive) p.positive else p.negative,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Donut with a legend beside it. Slices are (label, fraction). */
@Composable
fun DonutChart(slices: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val colors = p.chart
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(140.dp)) {
            val stroke = 34.dp.toPx()
            val inset = stroke / 2
            var start = -90f
            slices.forEachIndexed { i, (_, f) ->
                val sweep = 360f * f
                drawArc(
                    color = colors[i % colors.size],
                    startAngle = start, sweepAngle = sweep - 1.5f, useCenter = false,
                    topLeft = Offset(inset, inset), size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke),
                )
                start += sweep
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEachIndexed { i, (label, f) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(colors[i % colors.size]))
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = p.ink, modifier = Modifier.weight(1f, fill = false))
                    Text("${(f * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                }
            }
        }
    }
}

/** Simple bar chart: (label, fraction) per bar, coloured from the palette in order. */
@Composable
fun BarChart(bars: List<Pair<String, Float>>, modifier: Modifier = Modifier, barHeight: Dp = 64.dp) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        bars.forEachIndexed { i, (label, f) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .width(30.dp)
                        .height(barHeight * f.coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(p.chart[i % p.chart.size]),
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize), color = p.ink, maxLines = 1)
                Text("${(f * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = p.inkSoft)
            }
        }
    }
}
