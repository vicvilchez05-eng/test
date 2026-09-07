package com.personal.app.data

import com.personal.app.data.model.Category
import com.personal.app.data.model.Source
import com.personal.app.data.model.Transaction
import com.personal.app.domain.Reports
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

class ReportsTest {
    private fun at(d: LocalDate, hour: Int = 10) = d.atTime(hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
    private fun tx(amount: Long, cat: Category, d: LocalDate) = Transaction("t-$amount-$d", "a", amount, "EUR", cat, "x", at(d), Source.MANUAL)

    @Test
    fun week_runs_monday_to_sunday_and_fills_empty_days() {
        val wed = LocalDate.of(2025, 9, 3) // Wednesday
        val txs = listOf(tx(-10_00, Category.FOOD, LocalDate.of(2025, 9, 1)), tx(-20_00, Category.FOOD, wed), tx(-99_00, Category.FOOD, LocalDate.of(2025, 9, 8)))
        val w = Reports.week(txs, wed, ZoneOffset.UTC)
        assertEquals(LocalDate.of(2025, 9, 1), w.start)
        assertEquals(LocalDate.of(2025, 9, 7), w.endInclusive)
        assertEquals(7, w.perDayExpenses.size)
        assertEquals(30_00L, w.expensesMinor)
        assertEquals(listOf(10_00L, 0L, 20_00L, 0L, 0L, 0L, 0L), w.perDayExpenses.map { it.second })
        assertEquals(2, w.transactionCount)
    }

    @Test
    fun month_summary_and_series() {
        val sep = YearMonth.of(2025, 9); val aug = YearMonth.of(2025, 8)
        val txs = listOf(
            tx(-500_00, Category.HOUSING, sep.atDay(1)), tx(-100_00, Category.FOOD, sep.atDay(5)), tx(2_000_00, Category.SALARY, sep.atDay(28)),
            tx(-300_00, Category.FOOD, aug.atDay(10)), tx(1_500_00, Category.SALARY, aug.atDay(28)),
        )
        val m = Reports.month(txs, sep, ZoneOffset.UTC)
        assertEquals(600_00L, m.expensesMinor)
        assertEquals(2_000_00L, m.incomeMinor)
        assertEquals(1_400_00L, m.netMinor)
        assertEquals(Category.HOUSING to 500_00L, m.byCategory.first())
        assertEquals(30, m.days)
        val series = Reports.monthlySeries(txs, sep, 3, ZoneOffset.UTC)
        assertEquals(listOf(YearMonth.of(2025, 7), aug, sep), series.map { it.month })
        assertEquals(listOf(0L, 300_00L, 600_00L), series.map { it.expensesMinor })
    }
}
