package com.personal.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/** One animated blob of the background. All positions/sizes are fractions of the canvas. */
@Immutable
data class BlobSpec(
    val color: Color,
    /** Radius as a fraction of the shorter canvas side. */
    val radius: Float,
    /** Rest position of the centre, as fractions of width/height. */
    val center: Offset,
    /** How far the blob drifts from its rest position, as fractions of width/height. */
    val amplitude: Offset,
    /** One full drift cycle, in milliseconds. Long = slow and calm. */
    val durationMillis: Int,
    /** Phase offset in cycles (0..1) so blobs don't move in lockstep. */
    val phase: Float,
)

/** Design tokens for the liquid-glass surfaces and the blob background. */
@Immutable
data class GlassTokens(
    val isDark: Boolean,
    val base: Color,
    val fillTop: Color,
    val fillBottom: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val highlight: Color,
    val shadow: Color,
    val blobs: List<BlobSpec>,
)

private val blobLayout = listOf(
    // colour placeholder, radius, centre, amplitude, duration, phase
    BlobSpec(Color.Unspecified, 0.60f, Offset(0.15f, 0.18f), Offset(0.14f, 0.10f), 26_000, 0.00f),
    BlobSpec(Color.Unspecified, 0.52f, Offset(0.88f, 0.32f), Offset(0.10f, 0.16f), 31_000, 0.35f),
    BlobSpec(Color.Unspecified, 0.48f, Offset(0.28f, 0.82f), Offset(0.16f, 0.08f), 23_000, 0.62f),
    BlobSpec(Color.Unspecified, 0.42f, Offset(0.82f, 0.92f), Offset(0.10f, 0.10f), 37_000, 0.80f),
)

private fun blobs(colors: List<Color>, alpha: Float): List<BlobSpec> =
    blobLayout.mapIndexed { i, spec -> spec.copy(color = colors[i % colors.size].copy(alpha = alpha)) }

fun darkGlass(): GlassTokens = GlassTokens(
    isDark = true,
    base = AppColors.DarkBase,
    fillTop = Color.White.copy(alpha = 0.14f),
    fillBottom = Color.White.copy(alpha = 0.05f),
    borderTop = Color.White.copy(alpha = 0.55f),
    borderBottom = Color.White.copy(alpha = 0.08f),
    highlight = Color.White.copy(alpha = 0.18f),
    shadow = Color.Black.copy(alpha = 0.45f),
    blobs = blobs(listOf(AppColors.Indigo, AppColors.Violet, AppColors.Teal, AppColors.Rose), alpha = 0.55f),
)

fun lightGlass(): GlassTokens = GlassTokens(
    isDark = false,
    base = AppColors.LightBase,
    fillTop = Color.White.copy(alpha = 0.72f),
    fillBottom = Color.White.copy(alpha = 0.40f),
    borderTop = Color.White.copy(alpha = 0.95f),
    borderBottom = Color.White.copy(alpha = 0.30f),
    highlight = Color.White.copy(alpha = 0.60f),
    shadow = Color(0xFF3B4270).copy(alpha = 0.16f),
    blobs = blobs(listOf(AppColors.Indigo, AppColors.Violet, AppColors.Teal, AppColors.Rose), alpha = 0.38f),
)

val LocalGlass = staticCompositionLocalOf { darkGlass() }
