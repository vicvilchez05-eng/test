package com.personal.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.personal.app.R

/*
 * Esforia's three voices (all OFL, bundled in res/font, licences in docs/design/):
 *   Sora    = headings ("voice")      Manrope = body      IBM Plex Mono = money and percentages
 * Sora and Manrope ship as variable fonts; each weight is the same file with a wght axis setting.
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun variable(res: Int, weight: FontWeight) =
    Font(res, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val Sora = FontFamily(
    variable(R.font.sora, FontWeight.Normal),
    variable(R.font.sora, FontWeight.Medium),
    variable(R.font.sora, FontWeight.SemiBold),
    variable(R.font.sora, FontWeight.Bold),
)

val Manrope = FontFamily(
    variable(R.font.manrope, FontWeight.Normal),
    variable(R.font.manrope, FontWeight.Medium),
    variable(R.font.manrope, FontWeight.SemiBold),
    variable(R.font.manrope, FontWeight.Bold),
)

val PlexMono = FontFamily(
    Font(R.font.plexmono_medium, FontWeight.Medium),
    Font(R.font.plexmono_semibold, FontWeight.SemiBold),
)

/** Material slots mapped onto Esforia's sizes (docs/design/identidad-esforia.md §2). */
val AppTypography = Typography(
    // Hero figure: Sora 32 / 600
    displaySmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp),
    // Page title: Sora 26 / 500
    headlineMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 25.sp, lineHeight = 31.sp),
    // Section label: Sora 19 / 500
    headlineSmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 24.sp),
    // Section header with "view all": Sora 18 / 600
    titleLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 23.sp),
    // Card title: Sora 15 / 400 ; hero caption Sora 17 / 600 is titleMedium.copy(...)
    titleMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, lineHeight = 19.sp),
    // Row label 14.5 · row value / greeting 13 · hints / group label 11.5
    bodyLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 14.5.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, lineHeight = 16.sp),
    // Hero uppercase label 11 / 600 / tracking .8
    labelLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp),
    // Stat / group label 10–11.5 / tracking .3
    labelMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.3.sp),
    // Nav label 9.5
    labelSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 9.5.sp, lineHeight = 12.sp),
)

/** Money and percentages: IBM Plex Mono. */
val MonoText = TextStyle(fontFamily = PlexMono, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, lineHeight = 19.sp)
