package com.personal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * App theme = Esforia's palette exposed both as [LocalPalette] (the real design tokens) and as a
 * Material colour scheme (so Material components pick sensible colours). Dynamic colour is off:
 * the accent family is the brand.
 */
@Composable
fun PersonalAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    skin: Skin = DefaultSkin,
    content: @Composable () -> Unit,
) {
    val p = when (skin) {
        Skin.Esforia -> if (darkTheme) DarkPalette else LightPalette
        Skin.NavyGold -> if (darkTheme) NavyGoldDarkPalette else NavyGoldLightPalette
    }
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = p.moss, onPrimary = p.onAccent, primaryContainer = p.mossSoft, onPrimaryContainer = p.mossText,
            secondary = p.ember, onSecondary = p.onAccent, tertiary = p.blue,
            background = p.solidBg, onBackground = p.ink, surface = p.surface, onSurface = p.ink,
            surfaceVariant = p.surface, onSurfaceVariant = p.inkSoft, outline = p.line, outlineVariant = p.line,
            error = p.danger,
        )
    } else {
        lightColorScheme(
            primary = p.moss, onPrimary = p.onAccent, primaryContainer = p.mossSoft, onPrimaryContainer = p.mossText,
            secondary = p.ember, onSecondary = p.onAccent, tertiary = p.blue,
            background = p.solidBg, onBackground = p.ink, surface = p.surface, onSurface = p.ink,
            surfaceVariant = p.surface, onSurfaceVariant = p.inkSoft, outline = p.line, outlineVariant = p.line,
            error = p.danger,
        )
    }
    CompositionLocalProvider(LocalPalette provides p, LocalSkin provides skin) {
        MaterialTheme(colorScheme = scheme, typography = typographyFor(skin), content = content)
    }
}
