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
fun BarChart(
    bars: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 64.dp,
    /** Text under each label; defaults to the fraction as a percentage. */
    valueLabels: List<String>? = null,
    /** One colour for every bar instead of the categorical sequence. */
    singleColor: Color? = null,
) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        bars.forEachIndexed { i, (label, f) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .width(if (bars.size > 5) 22.dp else 30.dp)
                        .height(barHeight * f.coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(singleColor ?: p.chart[i % p.chart.size]),
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = p.ink, maxLines = 1)
                Text(valueLabels?.getOrNull(i) ?: "${(f * 100).toInt()} %", style = MonoText.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize), color = p.inkSoft)
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


/**
 * A filled line chart: the series as a stroke with a soft gradient under it, min/max captions
 * and optional first/last x labels. Values are normalised internally.
 */
@Composable
fun LineChart(
    values: List<Long>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    color: Color = LocalPalette.current.moss,
    format: (Long) -> String = { it.toString() },
    startLabel: String? = null,
    endLabel: String? = null,
) {
    val p = LocalPalette.current
    if (values.size < 2) return
    val min = values.min(); val max = values.max()
    val span = (max - min).coerceAtLeast(1L).toFloat()
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(format(max), style = MonoText.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = p.inkSoft)
            Text(format(values.last()), style = MonoText.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = p.ink)
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val stepX = size.width / (values.size - 1)
            val pts = values.mapIndexed { i, v -> Offset(i * stepX, size.height - (v - min) / span * (size.height - 8f) - 4f) }
            val line = Path().apply { pts.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) } }
            val area = Path().apply {
                addPath(line); lineTo(pts.last().x, size.height); lineTo(pts.first().x, size.height); close()
            }
            drawPath(area, brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0f))))
            drawPath(line, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(color, radius = 4.dp.toPx(), center = pts.last())
            drawCircle(p.surface, radius = 2.dp.toPx(), center = pts.last())
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(startLabel ?: "", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
            Text(format(min), style = MonoText.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), color = p.inkSoft)
            Text(endLabel ?: "", style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
        }
    }
}
