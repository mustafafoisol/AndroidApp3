package com.mustafafoisol.androidapp3.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class InvoiceViewModel : ViewModel() {

    var invoice by mutableStateOf(Invoice())
        private set

    fun setDocType(type: DocType) {
        invoice = invoice.copy(docType = type)
    }

    fun setName(v: String) { invoice = invoice.copy(name = v) }
    fun setAddress(v: String) { invoice = invoice.copy(address = v) }
    fun setMobile(v: String) { invoice = invoice.copy(mobile = v) }
    fun setSerial(v: String) { invoice = invoice.copy(serial = v) }
    fun setRpr(v: String) { invoice = invoice.copy(rpr = v) }
    fun setDate(v: String) { invoice = invoice.copy(date = v) }
    fun setCustSign(v: String) { invoice = invoice.copy(custSign = v) }
    fun setSellerSign(v: String) { invoice = invoice.copy(sellerSign = v) }

    fun setItemDesc(index: Int, v: String) = updateItem(index) { it.copy(desc = v) }
    fun setItemQty(index: Int, v: String) = updateItem(index) { it.copy(qty = v) }
    fun setItemPrice(index: Int, v: String) = updateItem(index) { it.copy(price = v) }

    fun addItem() {
        invoice = invoice.copy(items = invoice.items + LineItem())
    }

    /** The form always keeps at least one row, matching the design. */
    fun removeItem(index: Int) {
        if (invoice.items.size <= 1) return
        invoice = invoice.copy(items = invoice.items.filterIndexed { i, _ -> i != index })
    }

    private fun updateItem(index: Int, transform: (LineItem) -> LineItem) {
        val items = invoice.items.toMutableList()
        if (index !in items.indices) return
        items[index] = transform(items[index])
        invoice = invoice.copy(items = items)
    }
}
