package com.personal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalGlass
import kotlin.math.pow

val GlassCornerRadius = 24.dp

/**
 * The base "liquid glass" surface: translucent gradient fill, 1dp gradient border that is
 * brightest at the top-left (light source), a soft sheen in the top-left corner and a soft
 * drop shadow that is clipped out of the surface itself so the glass stays translucent.
 *
 * Real backdrop blur is not applied: on Android a backdrop filter needs API 31 RenderEffect and
 * costs a full extra render pass. Because the blob background is already blurred, translucency
 * alone reads as glass.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GlassCornerRadius),
    shadowElevation: Dp = 16.dp,
    tint: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val interaction = remember { MutableInteractionSource() }
    val fill = if (tint.isSpecified) {
        // Tinted glass: colour must stay translucent, so the alpha is fixed rather than derived from the fill.
        if (glass.isDark) listOf(tint.copy(alpha = 0.36f), tint.copy(alpha = 0.16f))
        else listOf(tint.copy(alpha = 0.30f), tint.copy(alpha = 0.14f))
    } else {
        listOf(glass.fillTop, glass.fillBottom)
    }

    Box(
        modifier = modifier
            .glassShadow(shape, shadowElevation, glass.shadow)
            .clip(shape)
            .background(Brush.verticalGradient(fill))
            .drawBehind {
                // Sheen: light catching the top-left corner.
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(glass.highlight, Color.Transparent),
                        center = Offset(size.width * 0.18f, 0f),
                        radius = size.width * 0.6f,
                    ),
                )
                // Thin bright edge along the top.
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, glass.borderTop, Color.Transparent),
                    ),
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(glass.borderTop, glass.borderBottom),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
                shape = shape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = ripple(color = glass.highlight),
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        content = content,
    )
}

/** A glass surface laid out as a padded column. The everyday building block for content. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(GlassCornerRadius),
    contentPadding: Dp = 20.dp,
    tint: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(modifier = modifier, shape = shape, tint = tint, onClick = onClick) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Soft drop shadow drawn as stacked, progressively larger and fainter rounded rects.
 * The surface's own area is clipped out, so nothing darkens the translucent interior.
 * Works on every API level and in Robolectric (no RenderEffect / BlurMaskFilter needed).
 */
fun Modifier.glassShadow(shape: Shape, elevation: Dp, color: Color): Modifier =
    if (elevation <= 0.dp || color.alpha == 0f) this else drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        val cornerRadius = when (outline) {
            is Outline.Rounded -> outline.roundRect.topLeftCornerRadius.x
            else -> 0f
        }
        val cutout = Path().apply { addOutline(outline) }
        val elevationPx = elevation.toPx()
        val layers = 10
        clipPath(cutout, ClipOp.Difference) {
            for (i in 1..layers) {
                val t = i / layers.toFloat()
                val spread = elevationPx * t
                val alpha = color.alpha * (1f - t).pow(1.6f) * 0.55f
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(-spread, -spread + elevationPx * 0.5f),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(cornerRadius + spread),
                )
            }
        }
    }

private val Color.isSpecified: Boolean get() = this != Color.Unspecified
