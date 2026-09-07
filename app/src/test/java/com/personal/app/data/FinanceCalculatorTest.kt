package com.personal.app.data

import com.personal.app.data.model.Account
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import com.personal.app.data.model.Money
import com.personal.app.data.model.Source
import com.personal.app.data.model.Transaction
import com.personal.app.domain.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

class FinanceCalculatorTest {
    private val sep = YearMonth.of(2025, 9)
    private fun at(day: Int) = sep.atDay(day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private fun tx(amount: Long, cat: Category, day: Int) =
        Transaction("t$amount$day", "a", amount, "EUR", cat, "x", at(day), Source.MANUAL)

    @Test
    fun month_totals_and_category_shares() {
        val txs = listOf(tx(-800_00, Category.HOUSING, 1), tx(-200_00, Category.FOOD, 3), tx(2_000_00, Category.SALARY, 28), tx(-50_00, Category.FOOD, 40 - 31))
        val totals = FinanceCalculator.monthTotals(txs, sep, ZoneOffset.UTC)
        assertEquals(2_000_00L, totals.incomeMinor)
        assertEquals(1_050_00L, totals.expensesMinor)
        assertEquals(950_00L, totals.netMinor)
        val shares = FinanceCalculator.expenseShares(txs, sep, ZoneOffset.UTC)
        assertEquals(Category.HOUSING, shares[0].first)
        assertEquals(800f / 1050f, shares[0].second, 0.001f)
    }

    @Test
    fun total_balance_counts_credit_as_negative_and_assets_exclude_it() {
        val accounts = listOf(
            Account("1", "cc", AccountType.CHECKING, 1_000_00, source = Source.MANUAL, createdAt = 0),
            Account("2", "cr", AccountType.CREDIT, -300_00, source = Source.MANUAL, createdAt = 0),
        )
        assertEquals(700_00L, FinanceCalculator.totalBalance(accounts))
        assertEquals(1_000_00L, FinanceCalculator.assets(accounts))
    }

    @Test
    fun month_over_month_is_null_without_history() {
        assertNull(FinanceCalculator.monthOverMonthPercent(emptyList(), emptyList(), sep, ZoneOffset.UTC))
    }

    @Test
    fun money_formats_and_parses_locale_styles() {
        assertEquals("1.234,56 €", Money.format(123_456, "EUR", Locale("es", "ES")).replace('\u00A0', ' '))
        assertEquals(123_456L, Money.parseToMinor("1.234,56"))
        assertEquals(123_456L, Money.parseToMinor("1,234.56"))
        assertEquals(123_456L, Money.parseToMinor("1234.56"))
        assertEquals(-1_200L, Money.parseToMinor("-12"))
        assertNull(Money.parseToMinor("abc"))
    }
}
