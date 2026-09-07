package com.personal.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.Palette
import kotlin.math.PI
import kotlin.math.cos

/**
 * Which palette colour each of the three blob slots takes, and how strong the whole layer is.
 * A straight copy of Esforia's `TONES` (AmbientBackground.tsx): the construction is identical on
 * every screen, only the colour mix and the strength change.
 */
enum class AmbientTone(val slots: List<Palette.() -> Color>, val strength: Float) {
    Home(listOf({ blue }, { moss }, { moss }), 0.9f),
    Finance(listOf({ moss }, { moss }, { blue }), 1.0f),
    Progress(listOf({ blue }, { blue }, { moss }), 0.7f),
    Profile(listOf({ moss }, { blue }, { ember }), 0.6f),
    Settings(listOf({ moss }, { blue }, { moss }), 0.4f),
}

/** One of Esforia's three `.ambient-blob` slots: geometry in dp, drift keyframes as in app.css. */
private data class Slot(
    val diameter: Float,
    /** Rest centre: fractions of width/height plus dp offsets (so "top:-70px; left:-60px" survives). */
    val fx: Float, val fy: Float, val dx: Float, val dy: Float,
    /** 50 % keyframe: translate (dp), scale from → to, opacity from → to. */
    val tx: Float, val ty: Float, val scaleFrom: Float, val scaleTo: Float, val alphaFrom: Float, val alphaTo: Float,
    val durationMillis: Int,
)

private val SLOTS = listOf(
    // .a  240px  top:-70 left:-60      drift1 26s  → (26,34)  1→1.12  .55→.8
    Slot(240f, 0f, 0f, 60f, 50f, 26f, 34f, 1f, 1.12f, 0.55f, 0.80f, 26_000),
    // .b  200px  top:30% right:-70     drift2 32s  → (-30,-26) 1.05→1  .5→.75
    Slot(200f, 1f, 0.30f, -30f, 100f, -30f, -26f, 1.05f, 1f, 0.50f, 0.75f, 32_000),
    // .c  220px  bottom:-80 left:20%   drift3 29s  → (22,-30) 1→1.14  .45→.7
    Slot(220f, 0.20f, 1f, 110f, -30f, 22f, -30f, 1f, 1.14f, 0.45f, 0.70f, 29_000),
)

/**
 * The ambient glow behind every screen, after Esforia's `AmbientBackground` + `.ambient-blob`:
 * three blurred colour blobs parked in the page margins, drifting on 26/32/29 s ease-in-out
 * loops, each with its own scale and opacity swing. Blobs sit at the edges on purpose, so the glow
 * bleeds into the padding around cards rather than under the text inside them.
 *
 * Blur: `filter: blur(46px)` becomes a 46dp canvas blur on API 31+. Below that (and in
 * Robolectric) each blob is drawn as a radial gradient that fades out from 45 % of its radius, which
 * is the cheapest stand-in for the same softness.
 *
 * Tone changes (tab switches) cross-fade colours and strength instead of jumping.
 */
@Composable
fun AmbientBackground(
    tone: AmbientTone,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    blur: Boolean = true,
) {
    val p = LocalPalette.current
    val colors = tone.slots.mapIndexed { i, pick ->
        animateColorAsState(pick(p), tween(600), label = "ambient$i").value
    }
    val strength by animateFloatAsState(tone.strength, tween(600), label = "ambientStrength")
    val phases: List<Float> = if (animated) animatedPhases() else SLOTS.map { 0f }

    Box(
        modifier
            .fillMaxSize()
            .background(if (p.bg.size > 1) Brush.verticalGradient(p.bg) else Brush.verticalGradient(listOf(p.bg[0], p.bg[0]))),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .then(if (blur) Modifier.blur(46.dp, BlurredEdgeTreatment.Unbounded) else Modifier),
        ) {
            val density = this.density
            SLOTS.forEachIndexed { i, s ->
                // ease-in-out, there and back: 0 → 1 → 0 over one loop.
                val k = (1f - cos(2f * PI.toFloat() * phases[i])) / 2f
                val cx = s.fx * size.width + s.dx * density + s.tx * density * k
                val cy = s.fy * size.height + s.dy * density + s.ty * density * k
                val scale = s.scaleFrom + (s.scaleTo - s.scaleFrom) * k
                val alpha = (s.alphaFrom + (s.alphaTo - s.alphaFrom) * k) * strength
                val r = s.diameter / 2f * density * scale
                val color = colors[i]
                val stops = if (blur) {
                    // Blur does the softening: keep the disc mostly solid so the glow has body.
                    arrayOf(0f to color.copy(alpha = alpha), 0.80f to color.copy(alpha = alpha), 1f to color.copy(alpha = 0f))
                } else {
                    arrayOf(0f to color.copy(alpha = alpha), 0.45f to color.copy(alpha = alpha * 0.85f), 1f to color.copy(alpha = 0f))
                }
                drawCircle(
                    brush = Brush.radialGradient(colorStops = stops, center = Offset(cx, cy), radius = r),
                    radius = r,
                    center = Offset(cx, cy),
                )
            }
        }
    }
}

@Composable
private fun animatedPhases(): List<Float> {
    val transition = rememberInfiniteTransition(label = "ambient")
    return SLOTS.mapIndexed { i, s ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(s.durationMillis, easing = LinearEasing), RepeatMode.Restart),
            label = "slot$i",
        ).value
    }
}
