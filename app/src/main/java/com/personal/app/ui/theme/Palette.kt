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
    // ---- Skin extras. Defaults reproduce Esforia; NavyGold overrides them. ----
    /** Gradient of the top band behind the page title; empty = no band. */
    val headerBand: List<Color> = emptyList(),
    /** Hero card fill and the ink used on it. */
    val heroGradient: List<Color> = financeGradient,
    val heroInk: Color = onAccent,
    val heroInkSoft: Color = onAccent.copy(alpha = 0.85f),
    /** When set, the hero figure is painted with this gradient instead of [heroInk]. */
    val heroValueGradient: List<Color>? = null,
    /** Gradients for accent cards (account cards), cycled in order. */
    val cardGradients: List<List<Color>> = listOf(financeGradient),
    val navBackground: Color = surface,
    val gold: Color = moss,
    val positive: Color = ember,
    val negative: Color = danger,
    /** Categorical chart colours, in order of use. */
    val chart: List<Color> = listOf(moss, blue, ember, mossSoft),
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


// ---------------------------------------------------------------------------------------------
// NavyGold skin (docs/design/navy-gold-*.png): warm ivory or deep navy ground, petrol band,
// gold as the accent, account cards in navy → teal / bronze / gold gradients.
// Esforia keys are reused with their generic meaning: moss = primary accent, ember = positive,
// blue = secondary accent, mossSoft = soft tile behind icons.
// ---------------------------------------------------------------------------------------------

private val NavyInk = Color(0xFF1B2F3B)
private val Navy = Color(0xFF1E4C5C)
private val Teal = Color(0xFF2E6E7E)
private val Gold = Color(0xFFC9A85B)

val NavyGoldLightPalette = Palette(
    isDark = false,
    bg = listOf(Color(0xFFF3EFE4)),
    solidBg = Color(0xFFF3EFE4),
    surface = Color(0xFFFFFFFF),
    line = Color(0xFFE4DDCC),
    ink = NavyInk,
    inkSoft = Color(0xFF5A6B75),
    muted = Color(0xFF8A9AA3),
    mutedLight = Color(0xFFC5CDD2),
    onAccent = Color(0xFFFFFFFF),
    moss = Navy,
    mossSoft = Color(0xFFE9E3D4),
    mossText = Navy,
    ember = Color(0xFF3F9A6B),
    emberSoft = Color(0xFFDDEFE3),
    emberText = Color(0xFF2F7D55),
    blue = Teal,
    blueSoft = Color(0xFFD8E8EC),
    danger = Color(0xFFD9534F),
    track = Color(0xFFE4DDCC),
    financeGradient = listOf(Navy, Teal, Gold),
    statusGood = Color(0xFF3F9A6B), statusGoodSoft = Color(0xFFDDEFE3),
    statusMid = Color(0xFFB8862F), statusMidSoft = Color(0xFFF3E9CF),
    statusLow = Color(0xFFBE6B60), statusLowSoft = Color(0xFFF5E4E1),
    glass = Color.White.copy(alpha = 0.72f),
    glassBorder = Color.White.copy(alpha = 0.9f),
    glassShadow = Color(0xFF1B2F3B).copy(alpha = 0.14f),
    headerBand = listOf(Color(0xFF235A68), Color(0xFF163C4A)),
    heroGradient = listOf(Color(0xFFFAF6EC), Color(0xFFEDE0BA), Color(0xFFD9C48C)),
    heroInk = NavyInk,
    heroInkSoft = Color(0xFF3C4E5A),
    heroValueGradient = null,
    cardGradients = listOf(
        listOf(Color(0xFF1E4C5C), Color(0xFF2E6E7E), Color(0xFFC9A85B)),
        listOf(Color(0xFF1E4C5C), Color(0xFF3A5A72), Color(0xFF7A93AD)),
        listOf(Color(0xFF1E4C5C), Color(0xFF8A7A4E), Color(0xFFC9A85B)),
        listOf(Color(0xFF2F6E5C), Color(0xFF6FA07A), Color(0xFFC9A85B)),
    ),
    navBackground = Color(0xFFF3EFE4),
    gold = Gold,
    positive = Color(0xFF3F9A6B),
    negative = Color(0xFFD9534F),
    chart = listOf(Navy, Gold, Color(0xFFD9C9A0), Teal),
)

val NavyGoldDarkPalette = Palette(
    isDark = true,
    bg = listOf(Color(0xFF16222B), Color(0xFF0E171E)),
    solidBg = Color(0xFF16222B),
    surface = Color(0xFF1C2A35),
    line = Color.White.copy(alpha = 0.08f),
    ink = Color(0xFFF2EFE6),
    inkSoft = Color(0xFFB5BEC5),
    muted = Color(0xFF7F8B94),
    mutedLight = Color(0xFF4B5860),
    onAccent = Color(0xFFF2EFE6),
    moss = Color(0xFF243645),
    mossSoft = Color(0xFF2A3A47),
    mossText = Color(0xFFEAD7A4),
    ember = Color(0xFF4CB07E),
    emberSoft = Color(0xFF4CB07E).copy(alpha = 0.16f),
    emberText = Color(0xFF7ED3A5),
    blue = Color(0xFF3C8494),
    blueSoft = Color(0xFF3C8494).copy(alpha = 0.18f),
    danger = Color(0xFFE06060),
    track = Color.White.copy(alpha = 0.10f),
    financeGradient = listOf(Color(0xFF1B3C48), Color(0xFF245C66), Color(0xFF3C8494)),
    statusGood = Color(0xFF4CB07E), statusGoodSoft = Color(0xFF4CB07E).copy(alpha = 0.14f),
    statusMid = Color(0xFFE0B15C), statusMidSoft = Color(0xFFE0B15C).copy(alpha = 0.14f),
    statusLow = Color(0xFFE08C82), statusLowSoft = Color(0xFFE08C82).copy(alpha = 0.14f),
    glass = Color.White.copy(alpha = 0.06f),
    glassBorder = Color.White.copy(alpha = 0.12f),
    glassShadow = Color.Black.copy(alpha = 0.45f),
    headerBand = listOf(Color(0xFF1B3F49), Color(0xFF12242C)),
    heroGradient = listOf(Color(0xFF223A48), Color(0xFF182B37)),
    heroInk = Color(0xFFF2EFE6),
    heroInkSoft = Color(0xFFD5DADF),
    heroValueGradient = listOf(Color(0xFFF3E4B6), Color(0xFFD4B36A)),
    cardGradients = listOf(
        listOf(Color(0xFF1B3C48), Color(0xFF245C66), Color(0xFF3C8494)),
        listOf(Color(0xFF1B2F3B), Color(0xFF5A4A38), Color(0xFF8B6B45)),
        listOf(Color(0xFF1B2F3B), Color(0xFF6B5A36), Color(0xFFB8975A)),
        listOf(Color(0xFF1B3C3A), Color(0xFF2F6E5C), Color(0xFF8AA35B)),
    ),
    navBackground = Color(0xFF0F1A21),
    gold = Color(0xFFD4B36A),
    positive = Color(0xFF4CB07E),
    negative = Color(0xFFE06060),
    chart = listOf(Color(0xFF2B5568), Color(0xFFC9A85B), Color(0xFFD9C9A0), Color(0xFF1F3A4D)),
)
