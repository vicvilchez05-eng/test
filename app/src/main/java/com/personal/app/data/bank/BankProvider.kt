package com.personal.app.data.bank

import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category

/*
 * The Open Banking seam. The app talks to banks only through [BankProvider]; today the only
 * implementation is [MockBankProvider] (a local sandbox), and [OpenBankingProviderTemplate]
 * shows where a real aggregator (Plaid, Tink, GoCardless...) plugs in.
 */

data class Institution(val id: String, val name: String, val country: String)

data class ProviderAccount(
    val externalId: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balanceMinor: Long,
)

data class ProviderTransaction(
    val externalId: String,
    val accountExternalId: String,
    val amountMinor: Long,
    val currency: String,
    val description: String,
    val timestamp: Long,
    val category: Category,
    val pending: Boolean = false,
)

class BankProviderException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface BankProvider {
    /** Stable id, part of every record id this provider produces ("mock", "plaid"...). */
    val id: String
    val displayName: String

    suspend fun institutions(): List<Institution>

    /**
     * Run the consent / login flow for [institutionId] and return an opaque connection handle.
     * A real provider returns here after the user has authenticated in a web view or SDK.
     */
    suspend fun link(institutionId: String): String

    suspend fun fetchAccounts(connectionExternalId: String): List<ProviderAccount>

    /** Transactions at or after [sinceTimestamp] (null = everything the bank exposes). */
    suspend fun fetchTransactions(connectionExternalId: String, sinceTimestamp: Long?): List<ProviderTransaction>

    suspend fun unlink(connectionExternalId: String) {}
}
