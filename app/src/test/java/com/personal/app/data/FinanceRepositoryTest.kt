package com.personal.app.data

import com.personal.app.data.bank.MockBankProvider
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import com.personal.app.data.model.Source
import com.personal.app.data.repository.FinanceRepository
import com.personal.app.data.repository.SyncState
import com.personal.app.data.store.InMemoryFinanceStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceRepositoryTest {
    private val now = 1_757_203_200_000L + 15 * 3600_000L
    private var ids = 0
    private fun repo() = FinanceRepository(
        store = InMemoryFinanceStore(),
        providers = mapOf("mock" to MockBankProvider(clock = { now }, latencyMillis = 0)),
        clock = { now },
        newId = { "id-${ids++}" },
    )

    @Test
    fun manual_transaction_moves_manual_account_balance() = runTest {
        val r = repo()
        val acc = r.addManualAccount("Efectivo", AccountType.CASH, 100_00)
        r.addManualTransaction(acc.id, -35_50, Category.FOOD, "Mercadona")
        r.addManualTransaction(acc.id, 200_00, Category.SALARY, "Paga")
        val updated = r.data.value.accounts.first { it.id == acc.id }
        assertEquals(264_50L, updated.balanceMinor)
        assertEquals(2, r.data.value.transactions.size)
        assertTrue(r.data.value.transactions.all { it.source == Source.MANUAL })
    }

    @Test
    fun deleting_a_manual_transaction_restores_the_balance() = runTest {
        val r = repo()
        val acc = r.addManualAccount("Efectivo", AccountType.CASH, 100_00)
        val tx = r.addManualTransaction(acc.id, -40_00, Category.LEISURE, "Cine")
        r.deleteTransaction(tx.id)
        assertEquals(100_00L, r.data.value.accounts.first().balanceMinor)
        assertTrue(r.data.value.transactions.isEmpty())
    }

    @Test
    fun linking_imports_accounts_and_history_and_resync_is_idempotent() = runTest {
        val r = repo()
        r.linkBank("mock", "demo")
        val d1 = r.data.value
        assertEquals(1, d1.connections.size)
        assertEquals(2, d1.accounts.size)
        assertTrue(d1.accounts.all { it.source == Source.LINKED && it.institution == "Banco Demo" })
        assertTrue(d1.transactions.isNotEmpty())
        assertTrue(d1.transactions.all { it.id.startsWith("mock:") })
        assertEquals(SyncState.Idle, r.syncState.value)

        r.sync()
        val d2 = r.data.value
        assertEquals(d1.transactions.size, d2.transactions.size)
        assertEquals(d1.accounts.map { it.balanceMinor }, d2.accounts.map { it.balanceMinor })
    }

    @Test
    fun unlinking_removes_the_connection_its_accounts_and_transactions() = runTest {
        val r = repo()
        val manual = r.addManualAccount("Efectivo", AccountType.CASH, 10_00)
        val conn = r.linkBank("mock", "caja")
        r.unlinkBank(conn.id)
        val d = r.data.value
        assertTrue(d.connections.isEmpty())
        assertEquals(listOf(manual.id), d.accounts.map { it.id })
        assertTrue(d.transactions.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleting_a_linked_account_is_refused() = runTest {
        val r = repo()
        r.linkBank("mock", "demo")
        r.deleteAccount(r.data.value.accounts.first().id)
    }

    @Test
    fun sync_drops_transactions_of_accounts_the_bank_no_longer_returns() = runTest {
        val first = repo()
        first.linkBank("mock", "demo")
        val seeded = first.data.value
        val orphan = seeded.transactions.first().copy(id = "mock:ghost", accountId = "mock:gone")
        val r = FinanceRepository(
            store = InMemoryFinanceStore(seeded.copy(transactions = seeded.transactions + orphan)),
            providers = mapOf("mock" to MockBankProvider(clock = { now }, latencyMillis = 0)),
            clock = { now },
        )
        assertTrue(r.data.value.transactions.any { it.id == "mock:ghost" })
        r.sync()
        assertTrue(r.data.value.transactions.none { it.id == "mock:ghost" })
        assertEquals(seeded.transactions.size, r.data.value.transactions.size)
    }

    @Test
    fun captures_are_deduplicated_and_accepting_creates_a_captured_transaction() = runTest {
        val r = repo()
        val cash = r.addManualAccount("Efectivo", AccountType.CASH, 100_00)
        val c = com.personal.app.data.capture.BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", "Compra de 12,50 € en MERCADONA", now)
        r.addCaptured(c); r.addCaptured(c)
        assertEquals(1, r.data.value.inbox.size)
        val tx = r.acceptCaptured(c.id, cash.id, -12_50, Category.FOOD, "Mercadona")
        assertEquals(Source.CAPTURED, tx.source)
        assertEquals(87_50L, r.data.value.accounts.first().balanceMinor)
        assertEquals(com.personal.app.data.model.CaptureStatus.ACCEPTED, r.data.value.inbox.first().status)
        r.dismissCaptured(c.id)
        assertEquals(com.personal.app.data.model.CaptureStatus.DISMISSED, r.data.value.inbox.first().status)
    }
}
