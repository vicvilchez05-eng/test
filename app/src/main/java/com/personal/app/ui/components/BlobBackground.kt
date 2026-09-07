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
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.BlobSpec
import com.personal.app.ui.theme.LocalGlass
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Full-screen background: a solid base colour with a few large, soft, slowly drifting colour blobs.
 *
 * Each blob follows a Lissajous path (cos on X, sin(2t) on Y) so the motion is organic and loops
 * seamlessly. Blobs are drawn as radial gradients that fade to transparent, then the whole canvas
 * is blurred (API 31+, a no-op on older devices where the gradient alone is soft enough).
 *
 * @param animated set to false for screenshots/tests so every frame is identical.
 * @param blur     set to false where RenderEffect is unavailable or undesirable (e.g. Robolectric).
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
                .then(if (blur) Modifier.blur(56.dp, BlurredEdgeTreatment.Unbounded) else Modifier),
        ) {
            val minSide = min(size.width, size.height)
            blobs.forEachIndexed { i, blob ->
                val t = (progress[i] + blob.phase) * 2f * PI.toFloat()
                val cx = (blob.center.x + blob.amplitude.x * cos(t)) * size.width
                val cy = (blob.center.y + blob.amplitude.y * sin(2f * t)) * size.height
                val r = blob.radius * minSide * (1f + 0.06f * sin(t))
                val c = Offset(cx, cy)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blob.color, blob.color.copy(alpha = 0f)),
                        center = c,
                        radius = r,
                    ),
                    radius = r,
                    center = c,
                )
            }
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
