package com.personal.app.data.capture

import com.personal.app.data.model.Category
import java.util.Locale

/** What the parser understood from one notification. [amountMinor] is signed (expense < 0). */
data class ParsedNotification(
    val amountMinor: Long,
    val merchant: String?,
    val category: Category,
    val isIncome: Boolean,
)

/**
 * Turns the text of a Spanish bank push notification into an amount, a merchant and a category
 * guess. Tuned for BBVA España's wording ("Compra de 12,50 € en MERCADONA", "Bizum recibido de
 * ANA por 20,00 €", "Recibo de IBERDROLA 56,78 €", "Ingreso de nómina 1.500,00 €") but written
 * defensively: any text with a euro amount is parsed, the sign comes from keywords, and the
 * merchant is best effort. Unknown formats still land in the inbox with the raw text, so nothing
 * is lost — the user completes what the parser could not read.
 */
object BankNotificationParser {

    /** Package → display name of the bank apps we listen to. Anything else is ignored. */
    val bankApps: Map<String, String> = mapOf(
        "com.bbva.bbvacontigo" to "BBVA",       // BBVA España
        "com.bbva.netcash" to "BBVA Net Cash",  // BBVA empresas
    )

    fun isBankApp(packageName: String): Boolean =
        packageName in bankApps || packageName.lowercase(Locale.ROOT).contains("bbva")

    // 1.234,56 € · 1234,56 EUR · 12 € · € 12,50 · 12.50 EUR
    private val amountAfter = Regex("""(\d{1,3}(?:[.\s]\d{3})+|\d+)(?:[,.](\d{1,2}))?\s?(?:€|EUR\b|euros?\b)""", RegexOption.IGNORE_CASE)
    private val amountBefore = Regex("""(?:€|EUR)\s?(\d{1,3}(?:[.\s]\d{3})+|\d+)(?:[,.](\d{1,2}))?""", RegexOption.IGNORE_CASE)

    private val incomeWords = Regex("""\b(ingreso|abono|abonado|n[oó]mina|recibid[oa]|has recibido|te ha enviado|devoluci[oó]n|reembolso|transferencia recibida)\b""", RegexOption.IGNORE_CASE)
    private val expenseWords = Regex("""\b(compra|pago|pagado|cargo|recibo|retirada|reintegro|adeudo|domiciliaci[oó]n|enviado|has enviado|transferencia enviada|suscripci[oó]n)\b""", RegexOption.IGNORE_CASE)

    // "en MERCADONA S.A.", "en Amazon", "de ANA GARCIA por", "a JUAN por"
    // Stops at a connector word, a comma/semicolon, or a full stop that ends a sentence (not the dot in "S.A.").
    private val merchantEn = Regex("""\ben\s+([A-Za-zÁÉÍÓÚÑáéíóúñ0-9][A-Za-zÁÉÍÓÚÑáéíóúñ0-9 .&'*\-]{1,40}?)(?=\s+(?:con|el|la|por|desde|a las|de|mediante)\b|[,;]|\.\s(?![a-záéíóúñ])|\.$|$)""")
    private val merchantDePor = Regex("""\b(?:de|a)\s+([A-ZÁÉÍÓÚÑ][A-Za-zÁÉÍÓÚÑáéíóúñ .\-]{1,40}?)\s+por\b""")
    private val merchantRecibo = Regex("""\brecibo(?:\s+domiciliado)?\s+(?:de\s+)?([A-Za-zÁÉÍÓÚÑáéíóúñ0-9][A-Za-zÁÉÍÓÚÑáéíóúñ0-9 .&'\-]{1,40}?)(?=\s+(?:por|de)\s+\d|[,.;:]|$)""", RegexOption.IGNORE_CASE)

    private val categoryHints: List<Pair<Regex, Category>> = listOf(
        Regex("""mercadona|carrefour|lidl|aldi|\bdia\b|alcampo|eroski|consum|supermerc|hipercor|froiz""", RegexOption.IGNORE_CASE) to Category.FOOD,
        Regex("""restaurant|\bbar\b|caf[eé]|starbucks|mcdonald|burger|kfc|telepizza|domino|glovo|just eat|uber eats""", RegexOption.IGNORE_CASE) to Category.FOOD,
        Regex("""repsol|cepsa|\bbp\b|galp|shell|gasolin|renfe|metro|\bemt\b|uber|cabify|bolt|parking|autopista|peaje|iryo|ouigo|alsa""", RegexOption.IGNORE_CASE) to Category.TRANSPORT,
        Regex("""netflix|spotify|hbo|disney|prime video|apple\.com|google \*|youtube|dazn|filmin|icloud|dropbox|chatgpt|openai""", RegexOption.IGNORE_CASE) to Category.SUBSCRIPTIONS,
        Regex("""iberdrola|endesa|naturgy|repsol luz|holaluz|vodafone|movistar|orange|digi|yoigo|masmovil|pepephone|canal de isabel|aguas|\bemasesa|\bemaya|seguro|mapfre|mutua|comunidad de propietarios""", RegexOption.IGNORE_CASE) to Category.BILLS,
        Regex("""farmacia|cl[ií]nica|dentista|hospital|sanitas|adeslas|[oó]ptica""", RegexOption.IGNORE_CASE) to Category.HEALTH,
        Regex("""amazon|zara|mango|primark|el corte ingl|ikea|leroy|decathlon|mediamarkt|pccomponentes|aliexpress|shein|fnac""", RegexOption.IGNORE_CASE) to Category.SHOPPING,
        Regex("""cine|yelmo|cinesa|gym|gimnasio|basic.?fit|teatro|concierto|steam|playstation|nintendo|padel|spotify""", RegexOption.IGNORE_CASE) to Category.LEISURE,
        Regex("""alquiler|hipoteca|arrendam""", RegexOption.IGNORE_CASE) to Category.HOUSING,
    )

    fun parse(title: String?, text: String?): ParsedNotification? {
        val full = listOfNotNull(title, text).joinToString(". ").replace(' ', ' ').trim()
        if (full.isEmpty()) return null
        val amount = parseAmount(full) ?: return null

        val isIncome = when {
            incomeWords.containsMatchIn(full) && !expenseWords.containsMatchIn(full) -> true
            incomeWords.containsMatchIn(full) && Regex("""bizum recibido|has recibido|transferencia recibida|ingreso|n[oó]mina|abono""", RegexOption.IGNORE_CASE).containsMatchIn(full) -> true
            else -> false
        }
        val merchant = parseMerchant(full)
        val category = guessCategory(full, merchant, isIncome)
        return ParsedNotification(if (isIncome) amount else -amount, merchant, category, isIncome)
    }

    fun parseAmount(text: String): Long? {
        val m = amountAfter.find(text) ?: amountBefore.find(text) ?: return null
        val whole = m.groupValues[1].replace(".", "").replace(" ", "")
        val cents = m.groupValues[2].padEnd(2, '0').take(2)
        return runCatching { whole.toLong() * 100 + (if (cents.isEmpty()) 0 else cents.toLong()) }.getOrNull()
    }

    fun parseMerchant(text: String): String? {
        val candidates = listOf(merchantRecibo, merchantDePor, merchantEn).mapNotNull { it.find(text)?.groupValues?.get(1) }
        val raw = candidates.firstOrNull() ?: return null
        var cleaned = raw.trim().trimEnd(',', ';', ':').replace(Regex("""\s+"""), " ")
        // Drop a sentence-ending dot, but keep the one that belongs to an abbreviation ("S.A.").
        if (cleaned.endsWith(".") && cleaned.substringAfterLast(' ').replace(".", "").length > 2) cleaned = cleaned.dropLast(1)
        return cleaned
            .takeIf { it.length >= 2 && !it.matches(Regex("""\d[\d.,]*""")) }
            ?.let { prettify(it) }
    }

    /** "MERCADONA S.A." → "Mercadona S.A."; acronyms (dotted or ≤3 letters) stay upper; mixed-case names are left alone. */
    private fun prettify(name: String): String {
        if (name != name.uppercase(Locale.ROOT)) return name
        return name.split(" ").joinToString(" ") { w ->
            if (w.contains('.') || w in companyAcronyms) w else w.lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
        }
    }

    private val companyAcronyms = setOf("SL", "SA", "SLU", "SAU", "SCP", "SC", "CB", "SLL", "SAL", "AIE", "UTE")

    fun guessCategory(text: String, merchant: String?, isIncome: Boolean): Category {
        if (isIncome) {
            return when {
                Regex("""n[oó]mina|salario|sueldo""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Category.SALARY
                Regex("""bizum|transferencia""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Category.TRANSFER
                else -> Category.OTHER_INCOME
            }
        }
        val haystack = listOfNotNull(merchant, text).joinToString(" ")
        categoryHints.firstOrNull { (rx, _) -> rx.containsMatchIn(haystack) }?.let { return it.second }
        return when {
            Regex("""bizum|transferencia""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Category.TRANSFER
            Regex("""recibo|domicili""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Category.BILLS
            Regex("""retirada|reintegro|cajero""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Category.OTHER
            else -> Category.OTHER
        }
    }
}
