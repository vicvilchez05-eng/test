package com.personal.app.data.bank

import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

/**
 * A local Open Banking sandbox. Deterministic: the same institution always yields the same
 * accounts and the same transaction history, generated day by day from a seed, so repeated
 * syncs are idempotent and "new since last sync" falls out naturally as the calendar advances.
 *
 * @param clock  epoch millis for "now"; injectable so tests and screenshots are stable.
 * @param latencyMillis simulated network delay, 0 in tests.
 */
class MockBankProvider(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val latencyMillis: Long = 600,
    private val historyDays: Int = 90,
) : BankProvider {
    override val id = "mock"
    override val displayName = "Sandbox"

    private val banks = listOf(
        Institution("demo", "Banco Demo", "ES"),
        Institution("caja", "Caja Sandbox", "ES"),
        Institution("neo", "Neo Bank Test", "ES"),
    )

    override suspend fun institutions(): List<Institution> {
        delay(latencyMillis / 2)
        return banks
    }

    override suspend fun link(institutionId: String): String {
        delay(latencyMillis)
        banks.firstOrNull { it.id == institutionId } ?: throw BankProviderException("Unknown institution: $institutionId")
        return "mock-$institutionId"
    }

    private fun institutionOf(connection: String): Institution =
        banks.firstOrNull { "mock-${it.id}" == connection } ?: throw BankProviderException("Unknown connection: $connection")

    override suspend fun fetchAccounts(connectionExternalId: String): List<ProviderAccount> {
        delay(latencyMillis)
        val inst = institutionOf(connectionExternalId)
        val opening = openingBalances(inst)
        // Balance = opening balance + everything that has happened up to now.
        val movements = generate(inst, null).groupBy { it.accountExternalId }.mapValues { (_, txs) -> txs.sumOf { it.amountMinor } }
        return accountsOf(inst).map { acc ->
            acc.copy(balanceMinor = (opening[acc.externalId] ?: 0L) + (movements[acc.externalId] ?: 0L))
        }
    }

    override suspend fun fetchTransactions(connectionExternalId: String, sinceTimestamp: Long?): List<ProviderTransaction> {
        delay(latencyMillis)
        return generate(institutionOf(connectionExternalId), sinceTimestamp)
    }

    private fun accountsOf(inst: Institution): List<ProviderAccount> = when (inst.id) {
        "demo" -> listOf(
            ProviderAccount("demo-cc", "Cuenta Corriente", AccountType.CHECKING, "EUR", 0),
            ProviderAccount("demo-sv", "Cuenta Ahorro", AccountType.SAVINGS, "EUR", 0),
        )
        "caja" -> listOf(
            ProviderAccount("caja-cc", "Cuenta Nómina", AccountType.CHECKING, "EUR", 0),
            ProviderAccount("caja-cr", "Tarjeta Crédito", AccountType.CREDIT, "EUR", 0),
        )
        else -> listOf(ProviderAccount("neo-cc", "Neo Account", AccountType.CHECKING, "EUR", 0))
    }

    private fun openingBalances(inst: Institution): Map<String, Long> = when (inst.id) {
        "demo" -> mapOf("demo-cc" to 3_450_00L, "demo-sv" to 12_800_00L)
        "caja" -> mapOf("caja-cc" to 1_180_00L, "caja-cr" to -640_00L)
        else -> mapOf("neo-cc" to 830_00L)
    }

    private data class Merchant(val name: String, val category: Category, val minMinor: Long, val maxMinor: Long)

    private val merchants = listOf(
        Merchant("Mercadona", Category.FOOD, 18_00, 96_00),
        Merchant("Carrefour", Category.FOOD, 25_00, 140_00),
        Merchant("Starbucks", Category.FOOD, 3_50, 9_80),
        Merchant("Repsol", Category.TRANSPORT, 30_00, 75_00),
        Merchant("Renfe", Category.TRANSPORT, 6_00, 48_00),
        Merchant("Netflix", Category.SUBSCRIPTIONS, 15_99, 15_99),
        Merchant("Spotify", Category.SUBSCRIPTIONS, 10_99, 10_99),
        Merchant("Cine Yelmo", Category.LEISURE, 9_50, 32_00),
        Merchant("Amazon", Category.SHOPPING, 12_00, 180_00),
        Merchant("Zara", Category.SHOPPING, 19_95, 89_90),
        Merchant("Farmacia", Category.HEALTH, 4_20, 38_00),
        Merchant("Iberdrola", Category.BILLS, 48_00, 96_00),
        Merchant("Vodafone", Category.BILLS, 34_90, 34_90),
    )

    /**
     * Day-by-day deterministic history. Each day is seeded from (institution, day index), so the
     * ids and amounts never change between calls; the rent and the salary land on fixed days.
     */
    private fun generate(inst: Institution, since: Long?): List<ProviderTransaction> {
        val now = clock()
        val today = Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC).toLocalDate()
        val mainAccount = accountsOf(inst).first().externalId
        val out = mutableListOf<ProviderTransaction>()
        for (back in historyDays downTo 0) {
            val day: LocalDate = today.minusDays(back.toLong())
            val dayStart = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            if (dayStart + 12 * 3600_000L > now) continue // today's transactions appear from noon
            val rnd = Random((inst.id.hashCode() * 31L + day.toEpochDay()).toInt())
            if (day.dayOfMonth == 1) {
                out += tx(inst, mainAccount, day, 0, -(850_00L), "Alquiler", Category.HOUSING, dayStart + 8 * 3600_000L)
            }
            if (day.dayOfMonth == 28) {
                out += tx(inst, mainAccount, day, 1, 2_150_00L, "Nómina", Category.SALARY, dayStart + 7 * 3600_000L)
            }
            val count = rnd.nextInt(0, 4)
            repeat(count) { i ->
                val m = merchants[rnd.nextInt(merchants.size)]
                val amount = -rnd.nextLong(m.minMinor, m.maxMinor + 1)
                val at = dayStart + (9 + rnd.nextInt(12)) * 3600_000L + rnd.nextInt(60) * 60_000L
                out += tx(inst, mainAccount, day, 10 + i, amount, m.name, m.category, at)
            }
        }
        return out.filter { since == null || it.timestamp >= since }.sortedByDescending { it.timestamp }
    }

    private fun tx(inst: Institution, account: String, day: LocalDate, n: Int, amount: Long, desc: String, cat: Category, at: Long) =
        ProviderTransaction(
            externalId = "${inst.id}-${day}-$n",
            accountExternalId = account,
            amountMinor = amount,
            currency = "EUR",
            description = desc,
            timestamp = at,
            category = cat,
        )
}
