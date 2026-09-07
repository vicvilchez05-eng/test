package com.personal.app.data.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Money helpers. Minor units in, formatted text out; parsing tolerates "1.234,56" and "1234.56". */
object Money {
    fun format(amountMinor: Long, currency: String = "EUR", locale: Locale = Locale.getDefault(), signed: Boolean = false): String {
        val cur = runCatching { Currency.getInstance(currency) }.getOrDefault(Currency.getInstance("EUR"))
        val nf = NumberFormat.getCurrencyInstance(locale).apply { this.currency = cur }
        val text = nf.format(BigDecimal.valueOf(amountMinor).movePointLeft(cur.defaultFractionDigits))
        return if (signed && amountMinor > 0) "+$text" else text
    }

    /** "1.234,56" / "1234.56" / "1,234.56" / "-12" → minor units, or null if unparseable. */
    fun parseToMinor(text: String, currency: String = "EUR"): Long? {
        val cleaned = text.trim().replace(" ", "").replace("€", "").replace("$", "")
        if (cleaned.isEmpty()) return null
        val normalized = when {
            cleaned.count { it == ',' } == 1 && cleaned.count { it == '.' } == 0 -> cleaned.replace(',', '.')
            cleaned.count { it == ',' } == 1 && cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.') -> cleaned.replace(".", "").replace(',', '.')
            else -> cleaned.replace(",", "")
        }
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }.getOrDefault(2)
        return runCatching { BigDecimal(normalized).setScale(digits, RoundingMode.HALF_UP).movePointRight(digits).longValueExact() }.getOrNull()
    }
}
