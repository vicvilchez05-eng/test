package com.personal.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Esforia's palette, key for key (esforia-app `src/contexts/palettes.ts`, theme "ritmo").
 * See docs/design/identidad-esforia.md. Only the accent family (moss*, financeGradient) would
 * change with a theme; greys, status and category colours are fixed meanings.
 */
@Immutable
data class Palette(
    val isDark: Boolean,
    /** Screen background, top → bottom (a flat colour is a one-element gradient). */
    val bg: List<Color>,
    val solidBg: Color,
    val surface: Color,
    val line: Color,
    val ink: Color,
    val inkSoft: Color,
    val muted: Color,
    val mutedLight: Color,
    val onAccent: Color,
    val moss: Color,
    val mossSoft: Color,
    val mossText: Color,
    val ember: Color,
    val emberSoft: Color,
    val emberText: Color,
    val blue: Color,
    val blueSoft: Color,
    val danger: Color,
    val track: Color,
    /** The hero gradient, 135°: near stop is the accent family, far stop leans blue. */
    val financeGradient: List<Color>,
    val statusGood: Color,
    val statusGoodSoft: Color,
    val statusMid: Color,
    val statusMidSoft: Color,
    val statusLow: Color,
    val statusLowSoft: Color,
    /** Frosted panel (onboarding / floating elements only). */
    val glass: Color,
    val glassBorder: Color,
    val glassShadow: Color,
    // ---- Semantic aliases (Esforia uses ember/danger for these; named for readability). ----
    val positive: Color = ember,
    val negative: Color = danger,
    /** Categorical chart colours, in order of use: accent, blue, green, soft accent. */
    val chart: List<Color> = listOf(moss, blue, ember, mossText),
) {
    /** `0 14px 30px ${moss}3D`: the hero card's shadow. */
    val heroShadow: Color get() = moss.copy(alpha = 0.24f)
}

val LightPalette = Palette(
    isDark = false,
    bg = listOf(Color(0xFFF8F7FD)),
    solidBg = Color(0xFFF8F7FD),
    surface = Color(0xFFFFFFFF),
    line = Color(0xFFE7E3F5),
    ink = Color(0xFF211C36),
    inkSoft = Color(0xFF665F87),
    muted = Color(0xFF9891B4),
    mutedLight = Color(0xFFC9C4E0),
    onAccent = Color(0xFFFFFFFF),
    moss = Color(0xFF6C5CE7),
    mossSoft = Color(0xFFEBE7FD),
    mossText = Color(0xFF4B3FBF),
    ember = Color(0xFF0FA968),
    emberSoft = Color(0xFFDEF7EC),
    emberText = Color(0xFF0B7A4C),
    blue = Color(0xFF2E90D6),
    blueSoft = Color(0xFFDFF0FC),
    danger = Color(0xFFD64545),
    track = Color(0xFFECE9F8),
    financeGradient = listOf(Color(0xFF8B5CF6), Color(0xFF7C6CF0), Color(0xFF5B7FF5)),
    statusGood = Color(0xFF0F9D63),
    statusGoodSoft = Color(0xFFE2F4EC),
    statusMid = Color(0xFFB8862F),
    statusMidSoft = Color(0xFFF7EFDF),
    statusLow = Color(0xFFBE6B60),
    statusLowSoft = Color(0xFFF7E8E5),
    glass = Color.White.copy(alpha = 0.55f),
    glassBorder = Color.White.copy(alpha = 0.80f),
    glassShadow = Color(0xFF4C3AA0).copy(alpha = 0.16f),
)

val DarkPalette = Palette(
    isDark = true,
    bg = listOf(Color(0xFF17122A), Color(0xFF0C0A16)),
    solidBg = Color(0xFF17122A),
    surface = Color(0xFF181229),
    line = Color.White.copy(alpha = 0.08f),
    ink = Color(0xFFF5F3FB),
    inkSoft = Color(0xFFA79FC7),
    muted = Color(0xFF7A7396),
    mutedLight = Color(0xFF4E4770),
    onAccent = Color(0xFFFFFFFF),
    moss = Color(0xFF8B7CF6),
    mossSoft = Color(0xFF8B7CF6).copy(alpha = 0.16f),
    mossText = Color(0xFFC9BFFF),
    ember = Color(0xFF34D399),
    emberSoft = Color(0xFF34D399).copy(alpha = 0.16f),
    emberText = Color(0xFF7EE8C4),
    blue = Color(0xFF7DD3FC),
    blueSoft = Color(0xFF7DD3FC).copy(alpha = 0.16f),
    danger = Color(0xFFF87171),
    track = Color.White.copy(alpha = 0.08f),
    financeGradient = listOf(Color(0xFFA78BFA), Color(0xFF8B7CF6), Color(0xFF6E93F7)),
    statusGood = Color(0xFF34D399),
    statusGoodSoft = Color(0xFF34D399).copy(alpha = 0.14f),
    statusMid = Color(0xFFE0B15C),
    statusMidSoft = Color(0xFFE0B15C).copy(alpha = 0.14f),
    statusLow = Color(0xFFE08C82),
    statusLowSoft = Color(0xFFE08C82).copy(alpha = 0.14f),
    glass = Color.White.copy(alpha = 0.07f),
    glassBorder = Color.White.copy(alpha = 0.14f),
    glassShadow = Color.Black.copy(alpha = 0.45f),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }
