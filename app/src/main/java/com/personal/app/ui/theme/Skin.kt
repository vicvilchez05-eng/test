package com.personal.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A skin is a complete visual identity: palette, typography, component variants and, where the
 * layouts differ, its own screens. Both are kept so Vic can compare them (HANDOFF D-022).
 *
 *  - [Esforia]  : Vic's Esforia identity (docs/design/identidad-esforia.md).
 *  - [NavyGold] : the navy / teal / gold design (docs/design/navy-gold-*.png).
 */
enum class Skin { Esforia, NavyGold }

/** Flip this to switch the whole app. Tests render both regardless. */
val DefaultSkin = Skin.NavyGold

val LocalSkin = staticCompositionLocalOf { Skin.Esforia }
