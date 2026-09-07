package com.personal.app.domain

import com.personal.app.data.model.Account
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import com.personal.app.data.model.Transaction
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Pure money maths. The only place totals are computed (mirrors Esforia's `monthlyFinance`). */
object FinanceCalculator {

    /** Net worth: every account's balance, credit cards counted as they are (negative). */
    fun totalBalance(accounts: List<Account>): Long = accounts.sumOf { it.balanceMinor }

    /** Assets only (what you own), excluding credit lines. */
    fun assets(accounts: List<Account>): Long = accounts.filter { it.type != AccountType.CREDIT }.sumOf { it.balanceMinor }

    data class MonthTotals(val incomeMinor: Long, val expensesMinor: Long) {
        val netMinor: Long get() = incomeMinor - expensesMinor
    }

    fun monthTotals(transactions: List<Transaction>, month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): MonthTotals {
        val inMonth = transactions.filter { it.monthAt(zone) == month }
        return MonthTotals(
            incomeMinor = inMonth.filter { it.amountMinor > 0 }.sumOf { it.amountMinor },
            expensesMinor = -inMonth.filter { it.amountMinor < 0 }.sumOf { it.amountMinor },
        )
    }

    /** Expenses of a month grouped by category, largest first, as positive minor units. */
    fun expensesByCategory(transactions: List<Transaction>, month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): List<Pair<Category, Long>> =
        transactions
            .filter { it.amountMinor < 0 && it.monthAt(zone) == month }
            .groupBy { it.category }
            .map { (cat, txs) -> cat to -txs.sumOf { it.amountMinor } }
            .sortedByDescending { it.second }

    /** Same thing as fractions of the month's total expenses (0..1), for charts. */
    fun expenseShares(transactions: List<Transaction>, month: YearMonth, zone: ZoneId = ZoneId.systemDefault(), top: Int = 4): List<Pair<Category, Float>> {
        val byCat = expensesByCategory(transactions, month, zone)
        val total = byCat.sumOf { it.second }.toFloat()
        if (total <= 0f) return emptyList()
        return byCat.take(top).map { (cat, amount) -> cat to amount / total }
    }

    /** Change of net worth versus the previous month's closing, as a percentage, or null if unknown. */
    fun monthOverMonthPercent(accounts: List<Account>, transactions: List<Transaction>, month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): Double? {
        val now = totalBalance(accounts)
        val thisMonthNet = monthTotals(transactions, month, zone).netMinor
        val before = now - thisMonthNet
        if (before == 0L) return null
        return thisMonthNet * 100.0 / kotlin.math.abs(before)
    }

    fun recent(transactions: List<Transaction>, limit: Int = 5): List<Transaction> =
        transactions.sortedByDescending { it.timestamp }.take(limit)

    private fun Transaction.monthAt(zone: ZoneId): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(timestamp).atZone(zone))
}
