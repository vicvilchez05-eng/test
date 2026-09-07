package com.personal.app.domain

import com.personal.app.data.model.Category
import com.personal.app.data.model.Transaction
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Period summaries for the Balance screen and the exports. Pure functions over transactions. */
object Reports {

    data class PeriodSummary(
        val start: LocalDate,
        val endInclusive: LocalDate,
        val incomeMinor: Long,
        val expensesMinor: Long,
        /** Expenses by category, largest first, positive minor units. */
        val byCategory: List<Pair<Category, Long>>,
        /** Expenses per calendar day across the whole period (zeros included), positive. */
        val perDayExpenses: List<Pair<LocalDate, Long>>,
        val transactionCount: Int,
    ) {
        val netMinor: Long get() = incomeMinor - expensesMinor
        val days: Int get() = (endInclusive.toEpochDay() - start.toEpochDay() + 1).toInt()
        val averageDailyExpenseMinor: Long get() = if (days == 0) 0 else expensesMinor / days
    }

    data class MonthPoint(val month: YearMonth, val incomeMinor: Long, val expensesMinor: Long) {
        val netMinor: Long get() = incomeMinor - expensesMinor
    }

    fun summarize(transactions: List<Transaction>, start: LocalDate, endInclusive: LocalDate, zone: ZoneId = ZoneId.systemDefault()): PeriodSummary {
        val from = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = endInclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val inRange = transactions.filter { it.timestamp in from until to }
        val expenses = inRange.filter { it.amountMinor < 0 }
        val byDay = expenses.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .mapValues { (_, txs) -> -txs.sumOf { it.amountMinor } }
        val days = generateSequence(start) { d -> d.plusDays(1).takeIf { !it.isAfter(endInclusive) } }.toList()
        return PeriodSummary(
            start = start,
            endInclusive = endInclusive,
            incomeMinor = inRange.filter { it.amountMinor > 0 }.sumOf { it.amountMinor },
            expensesMinor = -expenses.sumOf { it.amountMinor },
            byCategory = expenses.groupBy { it.category }.map { (c, txs) -> c to -txs.sumOf { it.amountMinor } }.sortedByDescending { it.second },
            perDayExpenses = days.map { it to (byDay[it] ?: 0L) },
            transactionCount = inRange.size,
        )
    }

    /** Monday to Sunday of the week containing [anchor]. */
    fun week(transactions: List<Transaction>, anchor: LocalDate, zone: ZoneId = ZoneId.systemDefault()): PeriodSummary {
        val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return summarize(transactions, monday, monday.plusDays(6), zone)
    }

    fun month(transactions: List<Transaction>, month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): PeriodSummary =
        summarize(transactions, month.atDay(1), month.atEndOfMonth(), zone)

    /** Income and expenses for the last [months] months ending at [endMonth], oldest first. */
    fun monthlySeries(transactions: List<Transaction>, endMonth: YearMonth, months: Int = 6, zone: ZoneId = ZoneId.systemDefault()): List<MonthPoint> =
        (months - 1 downTo 0).map { back ->
            val m = endMonth.minusMonths(back.toLong())
            val s = month(transactions, m, zone)
            MonthPoint(m, s.incomeMinor, s.expensesMinor)
        }
}
