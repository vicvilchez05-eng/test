package com.personal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Blue,
    onPrimary = Color.White,
    secondary = AppColors.Green,
    onSecondary = Color.White,
    tertiary = AppColors.LilacDeep,
    background = AppColors.LightBase,
    onBackground = AppColors.Navy,
    surface = AppColors.LightBase,
    onSurface = AppColors.Navy,
    onSurfaceVariant = AppColors.NavyMuted,
    outline = AppColors.Navy.copy(alpha = 0.12f),
    error = AppColors.Red,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.BlueSoft,
    onPrimary = AppColors.Navy,
    secondary = AppColors.Green,
    onSecondary = Color.White,
    tertiary = AppColors.Lilac,
    background = AppColors.DarkBase,
    onBackground = AppColors.Snow,
    surface = AppColors.DarkBase,
    onSurface = AppColors.Snow,
    onSurfaceVariant = AppColors.SnowMuted,
    outline = Color.White.copy(alpha = 0.16f),
    error = AppColors.Red,
)

/**
 * App theme. Light is the primary look (the guide is light); dark is a faithful translation.
 * Dynamic (Material You) colour stays off: the blob palette is the brand.
 */
@Composable
fun PersonalAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val glass = if (darkTheme) darkGlass() else lightGlass()
    CompositionLocalProvider(LocalGlass provides glass) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
