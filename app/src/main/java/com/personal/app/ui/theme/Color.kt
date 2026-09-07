package com.personal.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette taken from Vic's visual guide (docs/design/guia-visual-vic-2026-09-07.png):
 * pale lavender ground, periwinkle / champagne / lilac glass blobs, navy text, white frosted cards.
 */
object AppColors {
    // Ground
    val LightBase = Color(0xFFE2E5F6)
    val DarkBase = Color(0xFF11152A)

    // Text
    val Navy = Color(0xFF1B2140)
    val NavyMuted = Color(0xFF6B7194)
    val Snow = Color(0xFFEEF0FA)
    val SnowMuted = Color(0xFF9DA3C2)

    // Accents
    val Blue = Color(0xFF3E5BE8)
    val BlueSoft = Color(0xFF8FA4F0)
    val Green = Color(0xFF2E9E5B)
    val GreenSoft = Color(0xFFD7F0DF)
    val Red = Color(0xFFD9484F)

    // Blob hues: (body, light side, shadow side)
    val Periwinkle = Color(0xFF9DB0F2)
    val PeriwinkleLight = Color(0xFFD6DEFA)
    val PeriwinkleDeep = Color(0xFF6F86DC)
    val Champagne = Color(0xFFE9D8B4)
    val ChampagneLight = Color(0xFFF8F0DD)
    val ChampagneDeep = Color(0xFFCDB588)
    val Lilac = Color(0xFFC7BDF0)
    val LilacLight = Color(0xFFE8E2FA)
    val LilacDeep = Color(0xFFA093DC)

    // Dark-theme blob hues: the same three drops, deeper and more saturated so they don't go muddy on navy.
    val NightPeriwinkle = Color(0xFF5A70D8)
    val NightPeriwinkleLight = Color(0xFFA9B8F5)
    val NightPeriwinkleDeep = Color(0xFF2E3F9A)
    val NightGold = Color(0xFFC9A961)
    val NightGoldLight = Color(0xFFF0DFA8)
    val NightGoldDeep = Color(0xFF8A6E2E)
    val NightLilac = Color(0xFF8E7BD6)
    val NightLilacLight = Color(0xFFCBBFF5)
    val NightLilacDeep = Color(0xFF5A4AA6)
}
