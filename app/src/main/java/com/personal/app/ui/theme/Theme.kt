package com.personal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.IndigoLight,
    onPrimary = AppColors.DarkBase,
    secondary = AppColors.Teal,
    onSecondary = AppColors.DarkBase,
    tertiary = AppColors.Violet,
    background = AppColors.DarkBase,
    onBackground = AppColors.DarkText,
    surface = AppColors.DarkBase,
    onSurface = AppColors.DarkText,
    onSurfaceVariant = AppColors.DarkText.copy(alpha = 0.70f),
    outline = Color.White.copy(alpha = 0.20f),
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Indigo,
    onPrimary = Color.White,
    secondary = Color(0xFF0E9FB5),
    onSecondary = Color.White,
    tertiary = Color(0xFF8B3FD6),
    background = AppColors.LightBase,
    onBackground = AppColors.LightText,
    surface = AppColors.LightBase,
    onSurface = AppColors.LightText,
    onSurfaceVariant = AppColors.LightText.copy(alpha = 0.65f),
    outline = AppColors.LightText.copy(alpha = 0.15f),
)

/**
 * App theme. Dynamic (Material You) colour is intentionally off: the glass look depends on the
 * fixed blob palette, and wallpaper-derived colours would clash with it.
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
