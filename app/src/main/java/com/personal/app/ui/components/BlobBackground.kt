package com.personal.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.BlobSpec
import com.personal.app.ui.theme.LocalGlass
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Full-screen background: a pale ground with a few large, heavily blurred colour glows that
 * drift and breathe very slowly. Same recipe as Esforia's ambient layer (`.ambient-blob`:
 * `filter: blur(46px)`, 26–32 s drift loops, opacity pulsing between ~0.45 and ~0.8).
 *
 * Each blob is a soft radial gradient that fades to fully transparent well inside its own
 * radius, so it has no edge even without a blur. On API 31+ the whole canvas is additionally
 * blurred, which smooths the last trace of the gradient rings; older devices skip that and
 * still look right. Cards sit on top as plain translucent white, which reads as frosted glass
 * because what shows through is already out of focus.
 *
 * @param animated false for screenshots/tests so every frame is identical.
 * @param blur     false where RenderEffect is unavailable (Robolectric).
 */
@Composable
fun BlobBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    blur: Boolean = true,
    blobs: List<BlobSpec> = LocalGlass.current.blobs,
) {
    val base = LocalGlass.current.base
    val progress: List<Float> = if (animated) animatedProgress(blobs) else blobs.map { 0f }

    Box(modifier.fillMaxSize().background(base)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blur) Modifier.blur(36.dp, BlurredEdgeTreatment.Unbounded) else Modifier),
        ) {
            val minSide = min(size.width, size.height)
            blobs.forEachIndexed { i, blob ->
                val t = (progress[i] + blob.phase) * 2f * PI.toFloat()
                val cx = (blob.center.x + blob.amplitude.x * cos(t)) * size.width
                val cy = (blob.center.y + blob.amplitude.y * sin(2f * t)) * size.height
                val rx = blob.radius * minSide * (1f + 0.07f * sin(t))
                val ry = rx * blob.aspect * (1f - 0.05f * sin(t))
                val rot = blob.rotation + 8f * sin(t * 0.5f)
                // Breathe: opacity swings ±18 % around the spec value over the same cycle.
                val breathe = 1f + 0.18f * sin(t + 1.3f)
                drawGlow(blob, Offset(cx, cy), rx, ry, rot, breathe)
            }
        }
    }
}

private fun DrawScope.drawGlow(blob: BlobSpec, c: Offset, rx: Float, ry: Float, rotation: Float, breathe: Float) {
    val a = (blob.alpha * breathe).coerceIn(0f, 1f)
    rotate(degrees = rotation, pivot = c) {
        scale(scaleX = 1f, scaleY = ry / rx, pivot = c) {
            // Main glow: light core, body colour, then a long fade to nothing.
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to blob.light.copy(alpha = a),
                        0.28f to blob.body.copy(alpha = a),
                        0.60f to blob.deep.copy(alpha = a * 0.55f),
                        1.00f to blob.deep.copy(alpha = 0f),
                    ),
                    center = c,
                    radius = rx,
                ),
                radius = rx,
                center = c,
            )
            // A hint of volume that survives the blur: a lighter cloud off to the upper-left.
            val hc = c + Offset(-rx * 0.22f, -rx * 0.26f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blob.light.copy(alpha = a * 0.55f), blob.light.copy(alpha = 0f)),
                    center = hc,
                    radius = rx * 0.55f,
                ),
                radius = rx * 0.55f,
                center = hc,
            )
        }
    }
}

@Composable
private fun animatedProgress(blobs: List<BlobSpec>): List<Float> {
    val transition = rememberInfiniteTransition(label = "blobs")
    return blobs.mapIndexed { i, blob ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = blob.durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "blob$i",
        ).value
    }
}
