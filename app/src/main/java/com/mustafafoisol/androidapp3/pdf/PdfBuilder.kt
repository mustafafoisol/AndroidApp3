package com.mustafafoisol.androidapp3.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.mustafafoisol.androidapp3.data.Invoice
import com.mustafafoisol.androidapp3.data.LineItem
import com.mustafafoisol.androidapp3.data.formatMoney
import java.io.ByteArrayOutputStream

/**
 * Draws the A4 document from the design in /ui.
 *
 * The design sheet is laid out at 794x1123 CSS px (A4 at 96dpi). PDF user space is
 * points (A4 = 595x842), so the canvas is scaled once up front and every measurement
 * below stays in the design's own pixel units.
 */
object PdfBuilder {

    const val PAGE_WIDTH_PT = 595
    const val PAGE_HEIGHT_PT = 842

    private const val SHEET_W = 794f
    private const val SHEET_H = 1123f
    private const val SCALE = PAGE_WIDTH_PT / SHEET_W

    private const val PAD_L = 40f
    private const val PAD_T = 34f
    private const val PAD_B = 26f
    private const val CONTENT_L = PAD_L
    private const val CONTENT_R = SHEET_W - PAD_L
    private const val CONTENT_W = CONTENT_R - CONTENT_L

    /** The table always prints this many body rows, blank ones included. */
    private const val MIN_ROWS = 14

    private const val ORANGE = 0xFFE4610F.toInt()
    private const val INK = 0xFF17140F.toInt()
    private const val INK_SOFT = 0xFF4A443B.toInt()
    private const val BRAND_GREY = 0xFF6B6459.toInt()

    private val BRANDS = listOf("BOSCH", "DeWALT", "INGCO", "HARDEN", "TOPTUL", "YATO")

    fun render(invoice: Invoice): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_PT, PAGE_HEIGHT_PT, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)
        canvas.save()
        canvas.scale(SCALE, SCALE)
        drawSheet(canvas, invoice)
        canvas.restore()

        document.finishPage(page)

        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    private fun drawSheet(canvas: Canvas, invoice: Invoice) {
        val headerBottom = drawHeader(canvas)
        val infoBottom = drawInfo(canvas, invoice, headerBottom)
        val titleBottom = drawTitle(canvas, invoice, infoBottom)
        drawTable(canvas, invoice, titleBottom)
        val brandTop = drawBrandFooter(canvas)
        drawSignatures(canvas, invoice, brandTop)
    }

    // ---- header -------------------------------------------------------------

    private fun drawHeader(canvas: Canvas): Float {
        val logoW = 74f
        val logoH = 88f
        val logoRect = RectF(CONTENT_L, PAD_T, CONTENT_L + logoW, PAD_T + logoH)

        // Softly rounded on the left, heavily rounded on the right, forming the D mark.
        val logoPath = Path().apply {
            addRoundRect(
                logoRect,
                floatArrayOf(6f, 6f, 40f, 40f, 40f, 40f, 6f, 6f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(logoPath, fill(ORANGE))

        val logoLetter = text(46f, Typeface.DEFAULT_BOLD, Color.WHITE, Paint.Align.CENTER)
        canvas.drawText(
            "D",
            logoRect.centerX(),
            logoRect.centerY() - (logoLetter.descent() + logoLetter.ascent()) / 2f,
            logoLetter
        )

        val blockL = CONTENT_L + logoW + 18f
        val centerX = (blockL + CONTENT_R) / 2f

        val titlePaint = text(29f, Typeface.DEFAULT_BOLD, INK, Paint.Align.CENTER)
        val taglinePaint = text(15f, italic(), INK, Paint.Align.CENTER)
        val contactPaint = text(10f, Typeface.DEFAULT, INK_SOFT, Paint.Align.CENTER)

        val blockH =
            lineHeight(titlePaint) + 4f + lineHeight(taglinePaint) + 6f + lineHeight(contactPaint)
        var y = PAD_T + (logoH - blockH) / 2f

        y = drawTop(canvas, "DESTINATION TOOLS & ELECTRONICS", centerX, y, titlePaint) + 4f
        y = drawTop(canvas, "One stop solution for all kind of industrial goods", centerX, y, taglinePaint) + 6f
        drawTop(canvas, CONTACT_LINE, centerX, y, contactPaint)

        val ruleY = PAD_T + logoH + 12f
        canvas.drawLine(CONTENT_L, ruleY, CONTENT_R, ruleY, stroke(ORANGE, 2f))
        return ruleY
    }

    private const val CONTACT_LINE =
        "+8801943735093  ·  destinationtoolsandelectronics@gmail.com  ·  36, Madanpaul Lane, Nawabpur Dhaka 1100"

    // ---- name / serial block ------------------------------------------------

    private fun drawInfo(canvas: Canvas, invoice: Invoice, top: Float): Float {
        val labelPaint = text(12.5f, Typeface.DEFAULT_BOLD, INK)
        val valuePaint = text(12.5f, Typeface.DEFAULT, INK)
        val lineSlot = 12.5f * 2.1f

        val left = listOf(
            "Name: " to invoice.name,
            "Address: " to invoice.address,
            "Mobile: " to invoice.mobile
        )
        val right = listOf(
            "Serial: " to invoice.serial,
            "R/PR: " to invoice.rpr,
            "Date: " to invoice.date
        )

        val rightWidth =
            right.maxOf { labelPaint.measureText(it.first) + valuePaint.measureText(it.second) }
        val rightX = CONTENT_R - 4f - rightWidth

        val blockTop = top + 22f
        drawLabelledLines(canvas, left, CONTENT_L + 4f, blockTop, lineSlot, labelPaint, valuePaint)
        drawLabelledLines(canvas, right, rightX, blockTop, lineSlot, labelPaint, valuePaint)

        return blockTop + lineSlot * left.size
    }

    private fun drawLabelledLines(
        canvas: Canvas,
        lines: List<Pair<String, String>>,
        x: Float,
        top: Float,
        slot: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        lines.forEachIndexed { index, entry ->
            val slotTop = top + slot * index
            val baseline = slotTop + (slot - lineHeight(labelPaint)) / 2f - labelPaint.ascent()
            canvas.drawText(entry.first, x, baseline, labelPaint)
            canvas.drawText(entry.second, x + labelPaint.measureText(entry.first), baseline, valuePaint)
        }
    }

    private fun drawTitle(canvas: Canvas, invoice: Invoice, top: Float): Float {
        val paint = text(22f, Typeface.DEFAULT_BOLD, ORANGE, Paint.Align.CENTER)
        val bottom = drawTop(canvas, invoice.docType.title, SHEET_W / 2f, top + 14f, paint)
        return bottom + 16f
    }

    // ---- items table --------------------------------------------------------

    private fun drawTable(canvas: Canvas, invoice: Invoice, top: Float) {
        val columns = if (invoice.isMemo) {
            floatArrayOf(68f, CONTENT_W - 68f - 104f - 96f - 104f, 104f, 96f, 104f)
        } else {
            floatArrayOf(78f, CONTENT_W - 78f - 158f, 158f)
        }
        val headers = if (invoice.isMemo) {
            listOf("S/N", "Item Description", "Quantity", "Unit Price", "Total Price")
        } else {
            listOf("S/N", "Item Description", "Quantity")
        }

        val edges = FloatArray(columns.size + 1)
        edges[0] = CONTENT_L
        for (i in columns.indices) edges[i + 1] = edges[i] + columns[i]

        val headerH = 29f
        val rowH = 26.2f
        val footerH = if (invoice.isMemo) 32.2f else 20f

        val headerBottom = top + headerH
        val bodyBottom = headerBottom + rowH * MIN_ROWS
        val tableBottom = bodyBottom + footerH

        val headerPaint = text(12.5f, Typeface.DEFAULT_BOLD, INK, Paint.Align.CENTER)
        headers.forEachIndexed { i, label ->
            val cx = (edges[i] + edges[i + 1]) / 2f
            canvas.drawText(label, cx, top + 7f - headerPaint.ascent(), headerPaint)
        }

        drawBodyRows(canvas, invoice, edges, headerBottom, rowH)

        if (invoice.isMemo) {
            val totalPaint = text(13.5f, Typeface.DEFAULT_BOLD, INK, Paint.Align.CENTER)
            val baseline = bodyBottom + 8f - totalPaint.ascent()
            canvas.drawText("Total", (edges[3] + edges[4]) / 2f, baseline, totalPaint)
            canvas.drawText(formatMoney(invoice.total), (edges[4] + edges[5]) / 2f, baseline, totalPaint)
        }

        val border = stroke(INK, 1f)
        canvas.drawRect(CONTENT_L, top, CONTENT_R, tableBottom, border)
        canvas.drawLine(CONTENT_L, headerBottom, CONTENT_R, headerBottom, border)
        canvas.drawLine(CONTENT_L, bodyBottom, CONTENT_R, bodyBottom, border)
        for (i in 1 until edges.size - 1) {
            canvas.drawLine(edges[i], top, edges[i], tableBottom, border)
        }
    }

    private fun drawBodyRows(
        canvas: Canvas,
        invoice: Invoice,
        edges: FloatArray,
        top: Float,
        rowH: Float
    ) {
        val centered = text(13.5f, Typeface.SERIF, INK, Paint.Align.CENTER)
        val leading = text(13.5f, Typeface.SERIF, INK)

        val rows = invoice.filledItems
        for (index in 0 until MIN_ROWS) {
            val item: LineItem = rows.getOrNull(index) ?: continue
            val baseline = top + rowH * index + 5f - centered.ascent()

            canvas.drawText("${index + 1}.", (edges[0] + edges[1]) / 2f, baseline, centered)
            canvas.drawText(item.desc, edges[1] + 12f, baseline, leading)
            canvas.drawText(item.qty, (edges[2] + edges[3]) / 2f, baseline, centered)

            if (invoice.isMemo) {
                canvas.drawText(item.price, (edges[3] + edges[4]) / 2f, baseline, centered)
                canvas.drawText(formatMoney(item.amount), (edges[4] + edges[5]) / 2f, baseline, centered)
            }
        }
    }

    // ---- signatures and brand strip ----------------------------------------

    private fun drawBrandFooter(canvas: Canvas): Float {
        val paint = text(13f, Typeface.DEFAULT_BOLD, BRAND_GREY)
        val widths = BRANDS.map { paint.measureText(it) }
        val textTop = SHEET_H - PAD_B - lineHeight(paint)
        val ruleY = textTop - 10f

        canvas.drawLine(CONTENT_L, ruleY, CONTENT_R, ruleY, stroke(ORANGE, 2f))

        val gap = (CONTENT_W - widths.sum()) / (BRANDS.size - 1)
        var x = CONTENT_L
        BRANDS.forEachIndexed { index, brand ->
            drawTop(canvas, brand, x, textTop, paint)
            x += widths[index] + gap
        }
        return ruleY
    }

    private fun drawSignatures(canvas: Canvas, invoice: Invoice, brandTop: Float) {
        val valuePaint = text(12.5f, Typeface.DEFAULT, INK)
        val rulePaint = text(12.5f, Typeface.MONOSPACE, INK)
        val labelPaint = text(12.5f, Typeface.DEFAULT_BOLD, INK)

        val dashes = "-".repeat(35)
        val valueSlot = 18f
        val blockH = valueSlot + lineHeight(rulePaint) + lineHeight(labelPaint)
        val top = brandTop - 10f - blockH

        drawSignature(
            canvas, invoice.custSign, dashes, "Customer Signature",
            CONTENT_L + 4f, top, valueSlot, valuePaint, rulePaint, labelPaint, Paint.Align.LEFT
        )
        drawSignature(
            canvas, invoice.sellerSign, dashes, "Seller Signature",
            CONTENT_R - 4f, top, valueSlot, valuePaint, rulePaint, labelPaint, Paint.Align.RIGHT
        )
    }

    private fun drawSignature(
        canvas: Canvas,
        value: String,
        dashes: String,
        label: String,
        x: Float,
        top: Float,
        valueSlot: Float,
        valuePaint: Paint,
        rulePaint: Paint,
        labelPaint: Paint,
        align: Paint.Align
    ) {
        valuePaint.textAlign = align
        rulePaint.textAlign = align
        labelPaint.textAlign = align

        drawTop(canvas, value, x, top, valuePaint)
        var y = top + valueSlot
        y = drawTop(canvas, dashes, x, y, rulePaint)
        drawTop(canvas, label, x, y, labelPaint)
    }

    // ---- paint helpers ------------------------------------------------------

    private fun italic() = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
    }

    private fun text(
        size: Float,
        face: Typeface,
        color: Int,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = face
        textAlign = align
    }

    private fun lineHeight(paint: Paint) = paint.descent() - paint.ascent()

    /** Draws [value] with its top edge at [top] and returns the bottom edge. */
    private fun drawTop(canvas: Canvas, value: String, x: Float, top: Float, paint: Paint): Float {
        if (value.isNotEmpty()) canvas.drawText(value, x, top - paint.ascent(), paint)
        return top + lineHeight(paint)
    }
}
