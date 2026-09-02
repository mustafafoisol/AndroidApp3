package com.mustafafoisol.androidapp3.data

enum class DocType(val title: String) {
    CHALLAN("CHALLAN"),
    MEMO("MEMO")
}

data class LineItem(
    val desc: String = "",
    val qty: String = "",
    val price: String = ""
) {
    /** Quantity may be typed freely ("24 Pcs"); only the leading number drives the maths. */
    val qtyValue: Double get() = parseNumber(qty)
    val priceValue: Double get() = parseNumber(price)

    /** Line total is always derived — never typed. */
    val amount: Double get() = qtyValue * priceValue

    val hasContent: Boolean get() = desc.isNotBlank() || qty.isNotBlank()
}

data class Invoice(
    val docType: DocType = DocType.CHALLAN,
    val name: String = "",
    val address: String = "",
    val mobile: String = "",
    val serial: String = "",
    val rpr: String = "",
    val date: String = "",
    val items: List<LineItem> = listOf(LineItem()),
    val custSign: String = "",
    val sellerSign: String = ""
) {
    val isMemo: Boolean get() = docType == DocType.MEMO

    /** Grand total is the sum of the derived line totals. */
    val total: Double get() = items.sumOf { it.amount }

    val filledItems: List<LineItem> get() = items.filter { it.hasContent }

    val fileName: String
        get() {
            val base = if (isMemo) "Memo" else "Challan"
            return if (serial.isBlank()) "$base.pdf" else "$base $serial.pdf"
        }
}

/**
 * Reads the first number out of free-form text, ignoring thousands separators and any
 * trailing unit ("24 Pcs" -> 24, "1,200" -> 1200, "" -> 0).
 */
fun parseNumber(raw: String): Double {
    val digits = StringBuilder()
    var seenDigit = false
    var seenDot = false
    for (c in raw) {
        when {
            c.isDigit() -> {
                digits.append(c)
                seenDigit = true
            }
            c == ',' -> Unit
            c == '.' && seenDigit && !seenDot -> {
                digits.append(c)
                seenDot = true
            }
            seenDigit -> return digits.toString().toDoubleOrNull() ?: 0.0
            else -> Unit
        }
    }
    return digits.toString().toDoubleOrNull() ?: 0.0
}

/** Grouped, trailing-zero-free money formatting: 3024.0 -> "3,024", 528.5 -> "528.5". */
fun formatMoney(value: Double): String {
    if (value == 0.0) return "0"
    val rounded = Math.round(value * 100.0) / 100.0
    val whole = rounded.toLong()
    val grouped = StringBuilder()
    val digits = Math.abs(whole).toString()
    for ((index, c) in digits.withIndex()) {
        if (index > 0 && (digits.length - index) % 3 == 0) grouped.append(',')
        grouped.append(c)
    }
    val sign = if (rounded < 0) "-" else ""
    val fraction = Math.abs(rounded - whole)
    if (fraction < 0.005) return sign + grouped
    val cents = Math.round(fraction * 100.0).toInt()
    val decimals = if (cents % 10 == 0) (cents / 10).toString() else cents.toString().padStart(2, '0')
    return sign + grouped + "." + decimals
}
