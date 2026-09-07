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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Full-screen background: a pale ground with a few large glossy "glass bead" blobs that drift
 * very slowly, like the guide's 3D drops.
 *
 * Each blob is an ellipse shaded in four passes: body gradient lit from the top-left (light →
 * body → deep at the rim), a soft rim fade, a broad sheen and a small specular dot. Motion is a
 * Lissajous drift plus a gentle wobble of rotation and squash so the drop feels liquid.
 * A light canvas blur (API 31+) takes the digital edge off; older devices skip it.
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
                .then(if (blur) Modifier.blur(3.dp, BlurredEdgeTreatment.Unbounded) else Modifier),
        ) {
            val minSide = min(size.width, size.height)
            blobs.forEachIndexed { i, blob ->
                val t = (progress[i] + blob.phase) * 2f * PI.toFloat()
                val cx = (blob.center.x + blob.amplitude.x * cos(t)) * size.width
                val cy = (blob.center.y + blob.amplitude.y * sin(2f * t)) * size.height
                val rx = blob.radius * minSide * (1f + 0.04f * sin(t))
                val ry = rx * blob.aspect * (1f - 0.04f * sin(t))
                val rot = blob.rotation + 6f * sin(t * 0.5f)
                drawBead(blob, Offset(cx, cy), rx, ry, rot)
            }
        }
    }
}

private fun DrawScope.drawBead(blob: BlobSpec, c: Offset, rx: Float, ry: Float, rotation: Float) {
    val a = blob.alpha
    rotate(degrees = rotation, pivot = c) {
        // Draw everything as a circle of radius rx, squashed vertically to the aspect ratio.
        scale(scaleX = 1f, scaleY = ry / rx, pivot = c) {
            // 1. Body: lit from the top-left, deepening towards the lower-right rim.
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to blob.light.copy(alpha = a),
                        0.30f to blob.body.copy(alpha = a),
                        0.78f to blob.deep.copy(alpha = a),
                        0.97f to blob.deep.copy(alpha = a),
                        1.00f to blob.deep.copy(alpha = 0f),
                    ),
                    center = c + Offset(-rx * 0.38f, -rx * 0.42f),
                    radius = rx * 1.55f,
                ),
                radius = rx,
                center = c,
            )
            // 2. Refraction band: a lighter crescent near the lower-right edge, like thick glass.
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.66f to Color.Transparent,
                        0.84f to blob.light.copy(alpha = 0.45f * a),
                        0.96f to Color.Transparent,
                    ),
                    center = c + Offset(rx * 0.10f, rx * 0.12f),
                    radius = rx,
                ),
                radius = rx,
                center = c,
            )
            // 3. Broad sheen across the upper-left.
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f * a), Color.White.copy(alpha = 0f)),
                    center = c + Offset(-rx * 0.30f, -rx * 0.45f),
                    radius = rx * 0.75f,
                ),
                topLeft = c - Offset(rx, rx),
                size = Size(rx * 2, rx * 2),
            )
            // 4. Specular dot.
            val dotR = rx * 0.16f
            val dotC = c + Offset(-rx * 0.42f, -rx * 0.52f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.95f * a), Color.White.copy(alpha = 0f)),
                    center = dotC,
                    radius = dotR,
                ),
                radius = dotR,
                center = dotC,
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
