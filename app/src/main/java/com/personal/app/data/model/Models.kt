package com.personal.app.data.model

import kotlinx.serialization.Serializable

/*
 * Domain models. Money is stored as minor units (cents) in a Long plus an ISO-4217 code, never
 * as Double. Timestamps are epoch millis (UTC); the UI converts with java.time.
 * Everything is @Serializable because the store is a JSON file (HANDOFF D-025).
 */

@Serializable
enum class AccountType { CHECKING, SAVINGS, CREDIT, CASH, INVESTMENT }

/** Where a record came from: a bank connection or the user's own hand. */
@Serializable
enum class Source { LINKED, MANUAL }

@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceMinor: Long,
    val currency: String = "EUR",
    val source: Source,
    /** Bank or institution name for linked accounts; null for manual ones. */
    val institution: String? = null,
    /** The [BankConnection] this account belongs to, for linked accounts. */
    val connectionId: String? = null,
    val createdAt: Long,
    val lastSyncedAt: Long? = null,
)

/** Spending / income categories. Order is the chart order; icons and labels live in the UI. */
@Serializable
enum class Category(val isIncome: Boolean = false) {
    HOUSING, FOOD, TRANSPORT, LEISURE, SHOPPING, HEALTH, BILLS, SUBSCRIPTIONS, OTHER,
    SALARY(isIncome = true), TRANSFER(isIncome = true), OTHER_INCOME(isIncome = true),
}

@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    /** Negative = money out, positive = money in. */
    val amountMinor: Long,
    val currency: String = "EUR",
    val category: Category,
    val description: String,
    val timestamp: Long,
    val source: Source,
    val note: String? = null,
    val pending: Boolean = false,
) {
    val isExpense: Boolean get() = amountMinor < 0
}

/** A live link to an institution through a [com.personal.app.data.bank.BankProvider]. */
@Serializable
data class BankConnection(
    val id: String,
    val providerId: String,
    val institutionId: String,
    val institutionName: String,
    /** Opaque handle the provider gave us at link time (a token id, never the token itself). */
    val externalId: String,
    val linkedAt: Long,
    val lastSyncAt: Long? = null,
)

/** The whole dataset, as persisted. [version] is for future migrations. */
@Serializable
data class FinanceData(
    val version: Int = 1,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val connections: List<BankConnection> = emptyList(),
)
