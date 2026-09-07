package com.personal.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.personal.app.data.model.Money

/** What the UI needs to print an amount: the display currency and whether to hide it. */
data class MoneyDisplay(val currency: String = "EUR", val privacy: Boolean = false)

val LocalMoneyDisplay = compositionLocalOf { MoneyDisplay() }

/** Formats [amountMinor] in [currency] (or the display currency), or dots in privacy mode. */
@Composable
fun money(amountMinor: Long, currency: String? = null, signed: Boolean = false): String {
    val d = LocalMoneyDisplay.current
    if (d.privacy) return "••••"
    return Money.format(amountMinor, currency ?: d.currency, signed = signed)
}
