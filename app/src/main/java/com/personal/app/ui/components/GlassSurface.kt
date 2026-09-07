package com.personal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette
import kotlin.math.pow

/**
 * Esforia's `GlassPanel`: `glass` fill, 1px `glassBorder`, `glassShadow`. Used sparingly — in
 * Esforia only the onboarding and a handful of floating elements; here, the floating tab bar.
 * No backdrop blur (Esforia pays it on four elements; the blobs behind are already blurred).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    fill: Color = LocalPalette.current.glass,
    border: Color = LocalPalette.current.glassBorder,
    shadow: Color = LocalPalette.current.glassShadow,
    shadowElevation: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .softShadow(shape, shadowElevation, shadow, offsetY = shadowElevation / 2)
            .clip(shape)
            .background(fill)
            .border(1.dp, border, shape),
        content = content,
    )
}

/**
 * `box-shadow: 0 Y B color` drawn as stacked, progressively larger and fainter rounded rects,
 * with the surface's own area clipped out so a translucent surface stays clean.
 * Works on every API level and in Robolectric.
 */
fun Modifier.softShadow(shape: Shape, blur: Dp, color: Color, offsetY: Dp = blur / 2): Modifier =
    if (blur <= 0.dp || color.alpha == 0f) this else drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        val cornerRadius = when (outline) {
            is Outline.Rounded -> outline.roundRect.topLeftCornerRadius.x
            else -> 0f
        }
        val cutout = Path().apply { addOutline(outline) }
        val blurPx = blur.toPx()
        val dy = offsetY.toPx()
        val layers = 10
        clipPath(cutout, ClipOp.Difference) {
            for (i in 1..layers) {
                val t = i / layers.toFloat()
                val spread = blurPx * t
                val alpha = color.alpha * (1f - t).pow(1.6f) * 0.55f
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(-spread, -spread + dy),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(cornerRadius + spread),
                )
            }
        }
    }
