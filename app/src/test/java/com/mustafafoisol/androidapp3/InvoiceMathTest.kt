package com.mustafafoisol.androidapp3

import com.mustafafoisol.androidapp3.data.DocType
import com.mustafafoisol.androidapp3.data.Invoice
import com.mustafafoisol.androidapp3.data.LineItem
import com.mustafafoisol.androidapp3.data.formatMoney
import com.mustafafoisol.androidapp3.data.parseNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceMathTest {

    @Test
    fun `quantity text keeps only its leading number`() {
        assertEquals(24.0, parseNumber("24 Pcs"), 0.0)
        assertEquals(12.0, parseNumber("12"), 0.0)
        assertEquals(1200.0, parseNumber("1,200"), 0.0)
        assertEquals(2.5, parseNumber("2.5 kg"), 0.0)
        assertEquals(0.0, parseNumber(""), 0.0)
        assertEquals(0.0, parseNumber("Pcs"), 0.0)
    }

    @Test
    fun `money is grouped and drops empty decimals`() {
        assertEquals("3,024", formatMoney(3024.0))
        assertEquals("528", formatMoney(528.0))
        assertEquals("0", formatMoney(0.0))
        assertEquals("1,234,567", formatMoney(1234567.0))
        assertEquals("12.5", formatMoney(12.5))
    }

    @Test
    fun `line total is quantity times unit price`() {
        val item = LineItem(desc = "Pneumatic Air socket 10mm", qty = "24 Pcs", price = "22")
        assertEquals(528.0, item.amount, 0.0)
    }

    /** Mirrors the memo in /ui, which totals 3,024. */
    @Test
    fun `grand total sums every line`() {
        val invoice = Invoice(
            docType = DocType.MEMO,
            items = listOf(
                LineItem("Pneumatic Air socket 10mm", "24 Pcs", "22"),
                LineItem("Pneumatic Air socket 8mm", "24 Pcs", "20"),
                LineItem("Pneumatic Air socket 6mm", "24 Pcs", "18"),
                LineItem("Pneumatic Air socket 8mm x 6mm", "12 Pcs", "28"),
                LineItem("Pneumatic Air elbow 8mm x 1/4", "24 Pcs", "28"),
                LineItem("Pneumatic Air nipple 6mm x 1/4", "24 Pcs", "24")
            )
        )
        assertEquals(3024.0, invoice.total, 0.0)
        assertEquals("3,024", formatMoney(invoice.total))
    }

    @Test
    fun `blank rows are left out of the printed table`() {
        val invoice = Invoice(items = listOf(LineItem("Socket", "2"), LineItem()))
        assertEquals(1, invoice.filledItems.size)
    }

    @Test
    fun `file name follows document type and serial`() {
        assertEquals("Challan.pdf", Invoice().fileName)
        assertEquals(
            "Memo DT&E1201.pdf",
            Invoice(docType = DocType.MEMO, serial = "DT&E1201").fileName
        )
    }
}
