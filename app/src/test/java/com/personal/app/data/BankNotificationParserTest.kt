package com.personal.app.data

import com.personal.app.data.capture.BankNotificationListener
import com.personal.app.data.capture.BankNotificationParser
import com.personal.app.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Formats are ASSUMED from BBVA España's usual wording; Vic will paste real ones through the
 * "try it with a text" box. What matters here is the behaviour: amount, sign, merchant, category.
 */
class BankNotificationParserTest {

    @Test
    fun card_purchase_is_an_expense_with_merchant_and_category() {
        val p = BankNotificationParser.parse("BBVA", "Compra de 12,50 € en MERCADONA S.A. con tu tarjeta *1234")!!
        assertEquals(-12_50L, p.amountMinor)
        assertEquals("Mercadona S.A.", p.merchant)
        assertEquals(Category.FOOD, p.category)
    }

    @Test
    fun thousands_separator_and_eur_suffix() {
        val p = BankNotificationParser.parse(null, "Pago con tarjeta de 1.234,56 EUR en IKEA")!!
        assertEquals(-1_234_56L, p.amountMinor)
        assertEquals(Category.SHOPPING, p.category)
        assertEquals("Ikea", p.merchant)
    }

    @Test
    fun bizum_received_is_income_and_transfer() {
        val p = BankNotificationParser.parse("BBVA", "Bizum recibido de ANA GARCIA por 20,00 €")!!
        assertEquals(20_00L, p.amountMinor)
        assertTrue(p.isIncome)
        assertEquals(Category.TRANSFER, p.category)
        assertEquals("Ana Garcia", p.merchant)
    }

    @Test
    fun salary_is_income() {
        val p = BankNotificationParser.parse("BBVA", "Ingreso de nómina por 1.850,00 € de EMPRESA SL")!!
        assertEquals(1_850_00L, p.amountMinor)
        assertEquals(Category.SALARY, p.category)
    }

    @Test
    fun direct_debit_is_a_bill() {
        val p = BankNotificationParser.parse("BBVA", "Recibo de IBERDROLA por 56,78 € cargado en tu cuenta")!!
        assertEquals(-56_78L, p.amountMinor)
        assertEquals(Category.BILLS, p.category)
        assertEquals("Iberdrola", p.merchant)
    }

    @Test
    fun euro_sign_before_the_number_and_whole_euros() {
        assertEquals(40_00L, BankNotificationParser.parseAmount("Retirada en cajero: € 40"))
        assertEquals(7_00L, BankNotificationParser.parseAmount("Compra de 7 euros en BAR PEPE"))
    }

    @Test
    fun text_without_amount_is_not_parsed_but_still_captured() {
        assertNull(BankNotificationParser.parse("BBVA", "Tu tarjeta ya está activa"))
        val c = BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", "Tu tarjeta ya está activa", 1_000L)
        assertNull(c.amountMinor)
        assertTrue(c.id.startsWith("cap:"))
    }

    @Test
    fun ids_are_stable_for_the_same_notification_and_differ_otherwise() {
        val a = BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", "Compra de 5 € en X", 60_000L)
        val b = BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", "Compra de 5 € en X", 60_500L) // same minute
        val c = BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", "Compra de 6 € en X", 60_000L)
        assertEquals(a.id, b.id)
        assertNotEquals(a.id, c.id)
    }

    @Test
    fun only_bank_packages_are_watched() {
        assertTrue(BankNotificationParser.isBankApp("com.bbva.bbvacontigo"))
        assertTrue(!BankNotificationParser.isBankApp("com.whatsapp"))
    }
}
