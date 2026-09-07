package com.personal.app.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.personal.app.R
import com.personal.app.data.model.Account
import com.personal.app.data.model.FinanceData
import com.personal.app.data.model.Money
import com.personal.app.data.model.Transaction
import com.personal.app.domain.Reports
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Turns the ledger into files people can keep: a CSV of every movement and a PDF report with the
 * week's and month's summaries, the category breakdown and the movements of the month.
 * Files land in `cacheDir/exports` and are handed out through the app's FileProvider, so
 * nothing needs storage permissions.
 */
class ExportManager(
    private val context: Context,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    private val dir: File get() = File(context.cacheDir, "exports").apply { mkdirs() }
    private fun stamp(): String = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(Instant.ofEpochMilli(clock()).atZone(zone))

    fun exportCsv(data: FinanceData): File {
        val file = File(dir, "movimientos-${stamp()}.csv")
        file.writeText(buildCsv(data.transactions, data.accounts.associateBy { it.id }, zone), Charsets.UTF_8)
        return file
    }

    fun exportPdf(data: FinanceData, currency: String, ownerName: String, locale: Locale = Locale.getDefault()): File {
        val file = File(dir, "informe-${stamp()}.pdf")
        val today = Instant.ofEpochMilli(clock()).atZone(zone).toLocalDate()
        val month = Reports.month(data.transactions, java.time.YearMonth.from(today), zone)
        val week = Reports.week(data.transactions, today, zone)
        val monthTx = data.transactions
            .filter { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate().let { d -> !d.isBefore(month.start) && !d.isAfter(month.endInclusive) } }
            .sortedByDescending { it.timestamp }
        PdfReport(context, currency, locale, zone).render(file, ownerName, today, data.accounts, month, week, monthTx)
        return file
    }

    /** A chooser intent for [file]; the caller starts it from an Activity context. */
    fun shareIntent(file: File, mime: String, title: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    companion object {
        /** RFC 4180: comma separated, quotes doubled, every text field quoted. Amounts as decimals with a dot. */
        fun buildCsv(transactions: List<Transaction>, accountsById: Map<String, Account>, zone: ZoneId): String {
            val sb = StringBuilder()
            sb.append("date,time,account,description,category,amount,currency,source,note\r\n")
            transactions.sortedBy { it.timestamp }.forEach { t ->
                val at = Instant.ofEpochMilli(t.timestamp).atZone(zone)
                val amount = java.math.BigDecimal.valueOf(t.amountMinor).movePointLeft(2).toPlainString()
                listOf(
                    at.toLocalDate().toString(),
                    at.toLocalTime().withSecond(0).withNano(0).toString(),
                    q(accountsById[t.accountId]?.name ?: t.accountId),
                    q(t.description),
                    t.category.name,
                    amount,
                    t.currency,
                    t.source.name,
                    q(t.note ?: ""),
                ).joinTo(sb, ",")
                sb.append("\r\n")
            }
            return sb.toString()
        }

        private fun q(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
    }
}

/** A4 portrait, drawn with android.graphics — no PDF library. */
private class PdfReport(private val context: Context, private val currency: String, private val locale: Locale, private val zone: ZoneId) {
    private val pageW = 595; private val pageH = 842; private val margin = 44f
    private val ink = Color.rgb(0x21, 0x1C, 0x36); private val inkSoft = Color.rgb(0x66, 0x5F, 0x87)
    private val moss = Color.rgb(0x6C, 0x5C, 0xE7); private val line = Color.rgb(0xE7, 0xE3, 0xF5); private val mossSoft = Color.rgb(0xEB, 0xE7, 0xFD)
    private val ember = Color.rgb(0x0B, 0x7A, 0x4C); private val danger = Color.rgb(0xD6, 0x45, 0x45)

    private fun paint(size: Float, color: Int, bold: Boolean = false, mono: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; this.color = color
        typeface = Typeface.create(if (mono) Typeface.MONOSPACE else Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
    private fun money(minor: Long) = Money.format(minor, currency, locale)
    private fun s(id: Int, vararg args: Any) = context.getString(id, *args)
    private val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

    fun render(file: File, owner: String, today: LocalDate, accounts: List<Account>, month: Reports.PeriodSummary, week: Reports.PeriodSummary, monthTx: List<Transaction>) {
        val doc = PdfDocument()
        var pageNo = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f

        fun newPage(): Canvas {
            page?.let { doc.finishPage(it) }
            pageNo++
            val p = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
            page = p
            val c = p.canvas
            canvas = c
            y = margin
            c.drawText(s(R.string.report_title), margin, y + 10, paint(10f, inkSoft))
            c.drawText(s(R.string.report_page, pageNo), pageW - margin - 40f, pageH - 24f, paint(9f, inkSoft))
            y += 26f
            return c
        }
        fun ensure(height: Float): Canvas = if (canvas == null || y + height > pageH - margin) newPage() else canvas!!

        // ---- Title block ----
        var c = newPage()
        c.drawText(s(R.string.report_title), margin, y + 22, paint(22f, ink, bold = true)); y += 30
        val ownerLine = if (owner.isBlank()) dateFmt.format(today) else "$owner · ${dateFmt.format(today)}"
        c.drawText(ownerLine, margin, y + 12, paint(11f, inkSoft)); y += 28

        // ---- KPI boxes: month and week ----
        fun kpiBox(x: Float, w: Float, title: String, sum: Reports.PeriodSummary) {
            val boxH = 92f
            val bg = Paint().apply { color = mossSoft }
            c.drawRoundRect(x, y, x + w, y + boxH, 12f, 12f, bg)
            c.drawText(title.uppercase(locale), x + 14, y + 20, paint(9f, moss, bold = true))
            c.drawText("${dateFmt.format(sum.start)} – ${dateFmt.format(sum.endInclusive)}", x + 14, y + 34, paint(8.5f, inkSoft))
            val col = (w - 28) / 3
            listOf(
                Triple(s(R.string.stat_income), money(sum.incomeMinor), ember),
                Triple(s(R.string.stat_expenses), money(sum.expensesMinor), danger),
                Triple(s(R.string.stat_net), money(sum.netMinor), if (sum.netMinor >= 0) ink else danger),
            ).forEachIndexed { i, (label, value, color) ->
                c.drawText(label.uppercase(locale), x + 14 + i * col, y + 56, paint(7.5f, inkSoft))
                c.drawText(value, x + 14 + i * col, y + 74, paint(11f, color, bold = true, mono = true))
            }
        }
        val half = (pageW - 2 * margin - 12) / 2
        kpiBox(margin, half, s(R.string.section_monthly_summary), month)
        kpiBox(margin + half + 12, half, s(R.string.section_weekly_summary), week)
        y += 92f + 24f

        // ---- Accounts ----
        c = ensure(40f)
        c.drawText(s(R.string.section_by_account), margin, y + 14, paint(13f, ink, bold = true)); y += 26
        accounts.forEach { a ->
            c = ensure(18f)
            c.drawText(a.name + (a.institution?.let { " · $it" } ?: ""), margin, y + 12, paint(10f, ink))
            drawRight(c, money(a.balanceMinor), pageW - margin, y + 12, paint(10f, if (a.balanceMinor < 0) danger else ink, mono = true))
            y += 18
        }
        c = ensure(20f)
        c.drawLine(margin, y + 6, pageW - margin, y + 6, Paint().apply { color = line })
        c.drawText(s(R.string.hero_net_worth), margin, y + 22, paint(10f, ink, bold = true))
        drawRight(c, money(accounts.sumOf { it.balanceMinor }), pageW - margin, y + 22, paint(10f, ink, bold = true, mono = true))
        y += 40

        // ---- Categories of the month ----
        c = ensure(40f)
        c.drawText(s(R.string.report_categories_month), margin, y + 14, paint(13f, ink, bold = true)); y += 26
        val total = month.expensesMinor.coerceAtLeast(1)
        month.byCategory.forEach { (cat, amount) ->
            c = ensure(20f)
            val label = context.getString(com.personal.app.ui.components.labelResOf(cat))
            c.drawText(label, margin, y + 12, paint(10f, ink))
            val barX = margin + 150f; val barW = pageW - margin - 90f - barX
            c.drawRoundRect(barX, y + 4, barX + barW, y + 12, 4f, 4f, Paint().apply { color = mossSoft })
            c.drawRoundRect(barX, y + 4, barX + barW * amount / total, y + 12, 4f, 4f, Paint().apply { color = moss })
            drawRight(c, money(amount), pageW - margin, y + 12, paint(10f, ink, mono = true))
            y += 20
        }
        if (month.byCategory.isEmpty()) { c.drawText(s(R.string.no_expenses_period), margin, y + 12, paint(10f, inkSoft)); y += 20 }
        y += 16

        // ---- Transactions of the month ----
        c = ensure(40f)
        c.drawText(s(R.string.report_transactions_month, monthTx.size), margin, y + 14, paint(13f, ink, bold = true)); y += 24
        fun header(cv: Canvas) {
            cv.drawText(s(R.string.field_date), margin, y + 10, paint(8f, inkSoft, bold = true))
            cv.drawText(s(R.string.field_description), margin + 70, y + 10, paint(8f, inkSoft, bold = true))
            cv.drawText(s(R.string.field_category), margin + 260, y + 10, paint(8f, inkSoft, bold = true))
            drawRight(cv, s(R.string.report_amount), pageW - margin, y + 10, paint(8f, inkSoft, bold = true))
            y += 16
            cv.drawLine(margin, y, pageW - margin, y, Paint().apply { color = line }); y += 4
        }
        header(c)
        val shortDate = DateTimeFormatter.ofPattern("dd MMM", locale)
        monthTx.forEach { t ->
            val before = y
            c = ensure(16f)
            if (y != before + 0f && y == margin + 26f) header(c) // continued on a new page
            val at = Instant.ofEpochMilli(t.timestamp).atZone(zone)
            c.drawText(shortDate.format(at), margin, y + 11, paint(9f, inkSoft))
            c.drawText(ellipsize(t.description, 38), margin + 70, y + 11, paint(9f, ink))
            c.drawText(context.getString(com.personal.app.ui.components.labelResOf(t.category)), margin + 260, y + 11, paint(9f, inkSoft))
            drawRight(c, money(t.amountMinor), pageW - margin, y + 11, paint(9f, if (t.amountMinor < 0) ink else ember, mono = true))
            y += 16
        }

        page?.let { doc.finishPage(it) }
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    private fun drawRight(c: Canvas, text: String, right: Float, y: Float, p: Paint) = c.drawText(text, right - p.measureText(text), y, p)
    private fun ellipsize(t: String, max: Int) = if (t.length <= max) t else t.take(max - 1) + "…"
}
