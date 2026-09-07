package com.personal.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One glossy 3D blob of the background. Positions and sizes are fractions of the canvas.
 * A blob is an ellipse shaded like a glass bead: light side, body, shadow side, specular dot.
 */
@Immutable
data class BlobSpec(
    val body: Color,
    val light: Color,
    val deep: Color,
    /** Horizontal radius as a fraction of the shorter canvas side. */
    val radius: Float,
    /** Vertical/horizontal ratio: 1 = circle, <1 = wide, >1 = tall. */
    val aspect: Float,
    /** Rest rotation in degrees; the blob slowly wobbles around it. */
    val rotation: Float,
    val center: Offset,
    val amplitude: Offset,
    val durationMillis: Int,
    val phase: Float,
    /** Overall opacity of the blob. */
    val alpha: Float,
)

/** Design tokens for the frosted surfaces and the blob background. */
@Immutable
data class GlassTokens(
    val isDark: Boolean,
    val base: Color,
    val cornerRadius: Dp,
    val fillTop: Color,
    val fillBottom: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val highlight: Color,
    val shadow: Color,
    val shadowElevation: Dp,
    /** Hairline between rows inside a card. */
    val divider: Color,
    /** The raised circle under the selected bottom-bar item, and its icon. */
    val navSelectedBackground: Color,
    val navSelectedForeground: Color,
    val navUnselected: Color,
    val blobs: List<BlobSpec>,
)

private data class Layout(
    val radius: Float, val aspect: Float, val rotation: Float,
    val center: Offset, val amplitude: Offset, val duration: Int, val phase: Float,
)

// Placement echoes the guide: a big periwinkle drop top-left, champagne top-right, a lilac
// drop mid-left, periwinkle bottom-right, champagne bottom-left. Mostly out of the way of cards.
private val layout = listOf(
    Layout(0.34f, 1.25f, -25f, Offset(0.08f, 0.16f), Offset(0.05f, 0.04f), 29_000, 0.00f),
    Layout(0.26f, 0.85f, 20f, Offset(0.92f, 0.10f), Offset(0.04f, 0.05f), 35_000, 0.30f),
    Layout(0.20f, 1.10f, 40f, Offset(0.15f, 0.52f), Offset(0.06f, 0.05f), 24_000, 0.55f),
    Layout(0.36f, 0.90f, -10f, Offset(0.95f, 0.70f), Offset(0.05f, 0.06f), 33_000, 0.15f),
    Layout(0.24f, 1.20f, 30f, Offset(0.12f, 0.94f), Offset(0.05f, 0.04f), 27_000, 0.75f),
)

private fun blobs(alpha: Float, dark: Boolean = false): List<BlobSpec> {
    val periwinkle = if (dark) Triple(AppColors.NightPeriwinkle, AppColors.NightPeriwinkleLight, AppColors.NightPeriwinkleDeep)
    else Triple(AppColors.Periwinkle, AppColors.PeriwinkleLight, AppColors.PeriwinkleDeep)
    val gold = if (dark) Triple(AppColors.NightGold, AppColors.NightGoldLight, AppColors.NightGoldDeep)
    else Triple(AppColors.Champagne, AppColors.ChampagneLight, AppColors.ChampagneDeep)
    val lilac = if (dark) Triple(AppColors.NightLilac, AppColors.NightLilacLight, AppColors.NightLilacDeep)
    else Triple(AppColors.Lilac, AppColors.LilacLight, AppColors.LilacDeep)
    val hues = listOf(periwinkle, gold, lilac, periwinkle, gold)
    return layout.mapIndexed { i, l ->
        val (body, light, deep) = hues[i]
        BlobSpec(body, light, deep, l.radius, l.aspect, l.rotation, l.center, l.amplitude, l.duration, l.phase, alpha)
    }
}

fun lightGlass(): GlassTokens = GlassTokens(
    isDark = false,
    base = AppColors.LightBase,
    cornerRadius = 16.dp,
    fillTop = Color.White.copy(alpha = 0.82f),
    fillBottom = Color.White.copy(alpha = 0.70f),
    borderTop = Color.White.copy(alpha = 1.0f),
    borderBottom = AppColors.Navy.copy(alpha = 0.08f),
    highlight = Color.White.copy(alpha = 0.55f),
    shadow = Color(0xFF2A3160).copy(alpha = 0.10f),
    shadowElevation = 10.dp,
    divider = AppColors.Navy.copy(alpha = 0.07f),
    navSelectedBackground = Color.White,
    navSelectedForeground = AppColors.Navy,
    navUnselected = AppColors.NavyMuted,
    blobs = blobs(alpha = 0.95f),
)

fun darkGlass(): GlassTokens = GlassTokens(
    isDark = true,
    base = AppColors.DarkBase,
    cornerRadius = 16.dp,
    // Frosted navy rather than clear glass: the light theme's 80 % white becomes 80 % deep navy.
    fillTop = Color(0xFF262C4C).copy(alpha = 0.82f),
    fillBottom = Color(0xFF1C2140).copy(alpha = 0.76f),
    borderTop = Color.White.copy(alpha = 0.30f),
    borderBottom = Color.White.copy(alpha = 0.05f),
    highlight = Color.White.copy(alpha = 0.10f),
    shadow = Color.Black.copy(alpha = 0.35f),
    shadowElevation = 12.dp,
    divider = Color.White.copy(alpha = 0.08f),
    navSelectedBackground = Color.White.copy(alpha = 0.92f),
    navSelectedForeground = AppColors.Navy,
    navUnselected = AppColors.SnowMuted,
    blobs = blobs(alpha = 0.55f, dark = true),
)

val LocalGlass = staticCompositionLocalOf { lightGlass() }
