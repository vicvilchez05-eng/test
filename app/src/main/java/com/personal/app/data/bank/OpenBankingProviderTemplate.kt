package com.personal.app.data.bank

/**
 * TEMPLATE — how a real Open Banking aggregator plugs in. Not wired anywhere.
 *
 * Every aggregator (Plaid, Tink, GoCardless Bank Account Data, Salt Edge...) follows the same
 * shape, which is why [BankProvider] is enough of a seam:
 *
 *  1. **Link / consent.** The app asks the aggregator for a short-lived *link token*, opens the
 *     aggregator's hosted page or SDK where the user logs into the bank and grants consent, and
 *     gets back a *public token* that the app's backend exchanges for a long-lived *access token*.
 *     Store the access token encrypted (androidx.security `EncryptedSharedPreferences` or
 *     DataStore + Tink keyset). [BankConnection.externalId] holds a reference to it, never the
 *     token itself.
 *     · Plaid: POST /link/token/create → Link SDK → POST /item/public_token/exchange
 *     · Tink: OAuth2 authorization code flow with the Tink Link UI → POST /api/v1/oauth/token
 *
 *  2. **Accounts.** One call, mapped 1:1 to [ProviderAccount]. Balances usually come with it.
 *     · Plaid: POST /accounts/balance/get      · Tink: GET /data/v2/accounts
 *
 *  3. **Transactions.** Prefer the incremental endpoint when the aggregator has one: keep the
 *     cursor it returns instead of a timestamp.
 *     · Plaid: POST /transactions/sync (cursor)  · Tink: GET /data/v2/transactions?pageToken=
 *     Map the aggregator's category taxonomy to [com.personal.app.data.model.Category] here.
 *
 *  4. **Errors.** Wrap HTTP / auth failures in [BankProviderException]; on "consent expired"
 *     (PSD2 consents last 90 days in the EU) surface a re-link prompt rather than silently failing.
 *
 * Dependencies this would need: an HTTP client (Ktor or OkHttp) and JSON (already present). A
 * personal app should never embed the aggregator's *secret* key: the token exchange belongs on a
 * tiny backend or a serverless function, otherwise the key ships inside the APK.
 */
@Suppress("unused")
class OpenBankingProviderTemplate(
    override val id: String = "template",
    override val displayName: String = "Open Banking",
) : BankProvider {
    override suspend fun institutions(): List<Institution> = TODO("GET the aggregator's institution catalogue, filtered by country")
    override suspend fun link(institutionId: String): String = TODO("Create link token → open consent UI → exchange for access token → return its stored reference")
    override suspend fun fetchAccounts(connectionExternalId: String): List<ProviderAccount> = TODO("Load token by reference → GET accounts + balances → map to ProviderAccount")
    override suspend fun fetchTransactions(connectionExternalId: String, sinceTimestamp: Long?): List<ProviderTransaction> = TODO("Incremental sync with the stored cursor → map categories")
}
