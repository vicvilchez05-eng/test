package com.personal.app.data.repository

import com.personal.app.data.bank.BankProvider
import com.personal.app.data.bank.BankProviderException
import com.personal.app.data.model.Account
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.BankConnection
import com.personal.app.data.model.Category
import com.personal.app.data.model.FinanceData
import com.personal.app.data.model.Source
import com.personal.app.data.model.Transaction
import com.personal.app.data.store.FinanceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Error(val message: String) : SyncState
}

/**
 * The only place that writes to the ledger. Two inputs (a [BankProvider] or the user's hand),
 * one dataset out. Ids are stable: linked records are "<provider>:<externalId>", so re-syncing
 * upserts instead of duplicating.
 */
class FinanceRepository(
    private val store: FinanceStore,
    private val providers: Map<String, BankProvider>,
    /** "Now" for every write and every "this month"; injectable so tests and screenshots are stable. */
    val clock: () -> Long = { System.currentTimeMillis() },
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    val data: StateFlow<FinanceData> = store.data
    val accounts: Flow<List<Account>> = store.data.map { d -> d.accounts.sortedBy { it.createdAt } }
    val transactions: Flow<List<Transaction>> = store.data.map { d -> d.transactions.sortedByDescending { it.timestamp } }
    val connections: Flow<List<BankConnection>> = store.data.map { it.connections }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun provider(id: String): BankProvider = providers[id] ?: throw BankProviderException("No provider '$id'")
    val availableProviders: List<BankProvider> get() = providers.values.toList()

    // ---- Manual path ----

    suspend fun addManualAccount(name: String, type: AccountType, initialBalanceMinor: Long, currency: String = "EUR"): Account {
        val account = Account(
            id = newId(), name = name.trim(), type = type, balanceMinor = initialBalanceMinor, currency = currency,
            source = Source.MANUAL, createdAt = clock(),
        )
        store.update { it.copy(accounts = it.accounts + account) }
        return account
    }

    /**
     * Adds a hand-entered movement. Manual accounts keep a running balance, so it moves; linked
     * accounts get their balance from the bank, so a manual entry there is a note on top of it.
     */
    suspend fun addManualTransaction(
        accountId: String,
        amountMinor: Long,
        category: Category,
        description: String,
        timestamp: Long = clock(),
        note: String? = null,
    ): Transaction {
        require(amountMinor != 0L) { "Amount must not be zero" }
        val tx = Transaction(
            id = newId(), accountId = accountId, amountMinor = amountMinor, category = category,
            description = description.trim(), timestamp = timestamp, source = Source.MANUAL, note = note?.takeIf { it.isNotBlank() },
        )
        store.update { d ->
            val accounts = d.accounts.map { a ->
                if (a.id == accountId && a.source == Source.MANUAL) a.copy(balanceMinor = a.balanceMinor + amountMinor) else a
            }
            d.copy(accounts = accounts, transactions = d.transactions + tx)
        }
        return tx
    }

    suspend fun deleteTransaction(id: String) {
        store.update { d ->
            val tx = d.transactions.firstOrNull { it.id == id } ?: return@update d
            val accounts = d.accounts.map { a ->
                if (a.id == tx.accountId && a.source == Source.MANUAL && tx.source == Source.MANUAL) a.copy(balanceMinor = a.balanceMinor - tx.amountMinor) else a
            }
            d.copy(accounts = accounts, transactions = d.transactions.filterNot { it.id == id })
        }
    }

    suspend fun deleteAccount(id: String) {
        store.update { d -> d.copy(accounts = d.accounts.filterNot { it.id == id }, transactions = d.transactions.filterNot { it.accountId == id }) }
    }

    // ---- Linked path ----

    /** Runs the provider's consent flow, stores the connection, imports its accounts and syncs. */
    suspend fun linkBank(providerId: String, institutionId: String): BankConnection {
        val provider = provider(providerId)
        val institution = provider.institutions().firstOrNull { it.id == institutionId }
            ?: throw BankProviderException("Unknown institution $institutionId")
        val externalId = provider.link(institutionId)
        val connection = BankConnection(
            id = "$providerId:$externalId", providerId = providerId, institutionId = institutionId,
            institutionName = institution.name, externalId = externalId, linkedAt = clock(),
        )
        store.update { d -> d.copy(connections = d.connections.filterNot { it.id == connection.id } + connection) }
        sync(connection.id)
        return connection
    }

    suspend fun unlinkBank(connectionId: String) {
        val conn = data.value.connections.firstOrNull { it.id == connectionId } ?: return
        runCatching { provider(conn.providerId).unlink(conn.externalId) }
        store.update { d ->
            val gone = d.accounts.filter { it.connectionId == connectionId }.map { it.id }.toSet()
            d.copy(
                connections = d.connections.filterNot { it.id == connectionId },
                accounts = d.accounts.filterNot { it.id in gone },
                transactions = d.transactions.filterNot { it.accountId in gone },
            )
        }
    }

    /** Refreshes one connection or all of them. Never throws: failures land in [syncState]. */
    suspend fun sync(connectionId: String? = null) {
        val targets = data.value.connections.filter { connectionId == null || it.id == connectionId }
        if (targets.isEmpty()) return
        _syncState.value = SyncState.Syncing
        try {
            targets.forEach { syncConnection(it) }
            _syncState.value = SyncState.Idle
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    private suspend fun syncConnection(conn: BankConnection) {
        val provider = provider(conn.providerId)
        val now = clock()
        val remoteAccounts = provider.fetchAccounts(conn.externalId)
        // Overlap the window by a day so a late-posted transaction is not missed; ids make it idempotent.
        val since = conn.lastSyncAt?.let { it - 24 * 3600_000L }
        val remoteTx = provider.fetchTransactions(conn.externalId, since)

        store.update { d ->
            val existingById = d.accounts.associateBy { it.id }
            val accounts = remoteAccounts.map { ra ->
                val id = "${conn.providerId}:${ra.externalId}"
                val existing = existingById[id]
                Account(
                    id = id, name = existing?.name ?: ra.name, type = ra.type, balanceMinor = ra.balanceMinor, currency = ra.currency,
                    source = Source.LINKED, institution = conn.institutionName, connectionId = conn.id,
                    createdAt = existing?.createdAt ?: now, lastSyncedAt = now,
                )
            }
            val accountIds = accounts.map { it.id }.toSet()
            val untouched = d.accounts.filterNot { it.connectionId == conn.id }
            val incoming = remoteTx.map { rt ->
                Transaction(
                    id = "${conn.providerId}:${rt.externalId}", accountId = "${conn.providerId}:${rt.accountExternalId}",
                    amountMinor = rt.amountMinor, currency = rt.currency, category = rt.category, description = rt.description,
                    timestamp = rt.timestamp, source = Source.LINKED, pending = rt.pending,
                )
            }.filter { it.accountId in accountIds }
            val incomingIds = incoming.map { it.id }.toSet()
            val kept = d.transactions.filterNot { it.id in incomingIds }
            d.copy(
                accounts = untouched + accounts,
                transactions = kept + incoming,
                connections = d.connections.map { if (it.id == conn.id) it.copy(lastSyncAt = now) else it },
            )
        }
    }
}
