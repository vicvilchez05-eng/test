package com.personal.app.data

import com.personal.app.data.bank.MockBankProvider
import com.personal.app.data.model.Category
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockBankProviderTest {
    private val now = 1_757_203_200_000L // 2025-09-07T00:00:00Z
    private val provider = MockBankProvider(clock = { now + 15 * 3600_000L }, latencyMillis = 0)

    @Test
    fun history_is_deterministic() = runTest {
        val conn = provider.link("demo")
        val a = provider.fetchTransactions(conn, null)
        val b = provider.fetchTransactions(conn, null)
        assertEquals(a, b)
        assertTrue(a.isNotEmpty())
        assertTrue(a.any { it.category == Category.SALARY && it.amountMinor > 0 })
        assertTrue(a.any { it.category == Category.HOUSING && it.amountMinor < 0 })
    }

    @Test
    fun since_filter_returns_only_newer_items() = runTest {
        val conn = provider.link("demo")
        val all = provider.fetchTransactions(conn, null)
        val cutoff = now - 7 * 24 * 3600_000L
        val recent = provider.fetchTransactions(conn, cutoff)
        assertTrue(recent.all { it.timestamp >= cutoff })
        assertEquals(all.filter { it.timestamp >= cutoff }, recent)
    }

    @Test
    fun balances_equal_opening_plus_movements() = runTest {
        val conn = provider.link("demo")
        val accounts = provider.fetchAccounts(conn)
        val movements = provider.fetchTransactions(conn, null).sumOf { it.amountMinor }
        val checking = accounts.first { it.externalId == "demo-cc" }
        assertEquals(3_450_00L + movements, checking.balanceMinor)
        assertEquals(12_800_00L, accounts.first { it.externalId == "demo-sv" }.balanceMinor)
    }
}
