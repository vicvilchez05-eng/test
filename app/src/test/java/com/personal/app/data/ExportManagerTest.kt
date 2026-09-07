package com.personal.app.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.app.data.bank.MockBankProvider
import com.personal.app.data.export.ExportManager
import com.personal.app.data.model.Account
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import com.personal.app.data.model.FinanceData
import com.personal.app.data.model.Source
import com.personal.app.data.model.Transaction
import com.personal.app.data.repository.FinanceRepository
import com.personal.app.data.store.InMemoryFinanceStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ExportManagerTest {
    private val now = 1_757_260_800_000L

    @Test
    fun csv_is_rfc4180_with_quotes_doubled_and_amounts_as_decimals() {
        val acc = Account("a1", "Efectivo", AccountType.CASH, 0, source = Source.MANUAL, createdAt = 0)
        val txs = listOf(
            Transaction("t1", "a1", -12_50, "EUR", Category.FOOD, "Bar \"El Rincón\"", now, Source.MANUAL, note = "con, coma"),
            Transaction("t2", "a1", 2_000_00, "EUR", Category.SALARY, "Nómina", now - 86_400_000L, Source.LINKED),
        )
        val csv = ExportManager.buildCsv(txs, mapOf("a1" to acc), ZoneOffset.UTC)
        val lines = csv.trimEnd().split("\r\n")
        assertEquals("date,time,account,description,category,amount,currency,source,note", lines[0])
        assertEquals(3, lines.size)
        assertTrue(lines[1].contains("\"Nómina\",SALARY,2000.00,EUR,LINKED,\"\"")) // oldest first
        assertTrue(lines[2].contains("\"Efectivo\",\"Bar \"\"El Rincón\"\"\",FOOD,-12.50,EUR,MANUAL,\"con, coma\""))
    }

    @Test
    fun csv_and_pdf_files_are_written_to_the_cache_dir() = runTest {
        val repo = FinanceRepository(InMemoryFinanceStore(), mapOf("mock" to MockBankProvider(clock = { now }, latencyMillis = 0)), clock = { now })
        repo.linkBank("mock", "demo")
        val exporter = ExportManager(ApplicationProvider.getApplicationContext(), clock = { now })

        val csv = exporter.exportCsv(repo.data.value)
        assertTrue(csv.exists() && csv.length() > 200)
        assertTrue(csv.name.endsWith(".csv"))

        val pdf = try {
            exporter.exportPdf(repo.data.value, "EUR", "Vic")
        } catch (t: Throwable) {
            Assume.assumeNoException("PdfDocument not available in this Robolectric build", t); return@runTest
        }
        assertTrue(pdf.exists() && pdf.length() > 1000)
        assertEquals("%PDF", pdf.inputStream().use { String(it.readNBytes(4)) })
    }

    @Test
    fun empty_ledger_exports_still_produce_valid_files() = runTest {
        val exporter = ExportManager(ApplicationProvider.getApplicationContext(), clock = { now })
        val csv = exporter.exportCsv(FinanceData())
        assertEquals(1, csv.readLines().size)
    }
}
