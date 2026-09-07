package com.personal.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText

/*
 * Small, palette-driven charts. Colours come from `Palette.chart` in order, so every chart in
 * the app reads as one system. Kept deliberately plain (no axes, no grid): Esforia's charts are
 * shapes inside a card, not a plotting library.
 */

/** Vertical bars: (label, fraction 0..1) per bar. Label and percentage underneath. */
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
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(p.chart[i % p.chart.size]),
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = p.ink, maxLines = 1)
                Text("${(f * 100).toInt()} %", style = MonoText.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = p.inkSoft)
            }
        }
    }
}

/** Donut with a legend beside it: (label, fraction) per slice. */
@Composable
fun DonutChart(slices: List<Pair<String, Float>>, modifier: Modifier = Modifier, size: Dp = 132.dp) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(size)) {
            val stroke = 30.dp.toPx()
            val inset = stroke / 2
            var start = -90f
            slices.forEachIndexed { i, (_, f) ->
                val sweep = 360f * f
                drawArc(
                    color = p.chart[i % p.chart.size],
                    startAngle = start, sweepAngle = sweep - 2f, useCenter = false,
                    topLeft = Offset(inset, inset), size = Size(this.size.width - stroke, this.size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        Spacer(Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEachIndexed { i, (label, f) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(p.chart[i % p.chart.size]))
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = p.ink)
                    Text("${(f * 100).toInt()} %", style = MonoText.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = p.inkSoft)
                }
            }
        }
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
