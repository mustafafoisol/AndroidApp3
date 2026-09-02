package com.mustafafoisol.androidapp3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafafoisol.androidapp3.data.DocType
import com.mustafafoisol.androidapp3.data.Invoice
import com.mustafafoisol.androidapp3.data.LineItem
import com.mustafafoisol.androidapp3.data.formatMoney
import com.mustafafoisol.androidapp3.ui.theme.BorderLine
import com.mustafafoisol.androidapp3.ui.theme.Canvas
import com.mustafafoisol.androidapp3.ui.theme.Divider
import com.mustafafoisol.androidapp3.ui.theme.Faint
import com.mustafafoisol.androidapp3.ui.theme.Ink
import com.mustafafoisol.androidapp3.ui.theme.Label
import com.mustafafoisol.androidapp3.ui.theme.Muted
import com.mustafafoisol.androidapp3.ui.theme.Orange
import com.mustafafoisol.androidapp3.ui.theme.OrangeDeep
import com.mustafafoisol.androidapp3.ui.theme.OrangeMuted
import com.mustafafoisol.androidapp3.ui.theme.OrangeTint
import com.mustafafoisol.androidapp3.ui.theme.Surface
import com.mustafafoisol.androidapp3.ui.theme.TabIdleBorder

private val BodyInput = TextStyle(fontSize = 14.5.sp)
private val CompactInput = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
private val CardInput = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
private val SmallInput = TextStyle(fontSize = 14.sp)

@Composable
fun FormScreen(
    invoice: Invoice,
    onDocType: (DocType) -> Unit,
    onName: (String) -> Unit,
    onAddress: (String) -> Unit,
    onMobile: (String) -> Unit,
    onSerial: (String) -> Unit,
    onRpr: (String) -> Unit,
    onDate: (String) -> Unit,
    onItemDesc: (Int, String) -> Unit,
    onItemQty: (Int, String) -> Unit,
    onItemPrice: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCustSign: (String) -> Unit,
    onSellerSign: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        BrandBar()
        DocTypeTabs(invoice.docType, onDocType)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CustomerSection(invoice, onName, onAddress, onMobile)
            DocumentSection(invoice, onSerial, onDate, onRpr)
            ItemsSection(invoice, onItemDesc, onItemQty, onItemPrice, onAddItem, onRemoveItem)
            if (invoice.isMemo) TotalSection(invoice)
            SignaturesSection(invoice, onCustSign, onSellerSign)
        }

        GenerateBar(onGenerate)
    }
}

@Composable
private fun BrandBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).background(Orange, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("D", fontSize = 19.sp, fontWeight = FontWeight.Black, color = Surface)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DESTINATION TOOLS & ELECTRONICS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.1).sp,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "One stop solution for all kind of industrial goods",
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic,
                color = Muted,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLine))
}

@Composable
private fun DocTypeTabs(selected: DocType, onSelect: (DocType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DocType.entries.forEach { type ->
            val active = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (active) Orange else Surface,
                        RoundedCornerShape(percent = 50)
                    )
                    .border(
                        1.dp,
                        if (active) Orange else TabIdleBorder,
                        RoundedCornerShape(percent = 50)
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 11.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = if (active) Surface else Muted
                )
            }
        }
    }
}

@Composable
private fun CustomerSection(
    invoice: Invoice,
    onName: (String) -> Unit,
    onAddress: (String) -> Unit,
    onMobile: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("CUSTOMER")
        Column(modifier = Modifier.fillMaxWidth().formCard().padding(horizontal = 14.dp, vertical = 4.dp)) {
            LabelledRow("Name", divider = true) {
                PlainField(invoice.name, onName, "Customer name", BodyInput)
            }
            LabelledRow("Address", divider = true, alignTop = true) {
                PlainField(
                    value = invoice.address,
                    onValueChange = onAddress,
                    placeholder = "Area, city",
                    textStyle = BodyInput,
                    singleLine = false,
                    minLines = 2
                )
            }
            LabelledRow("Mobile", divider = false) {
                PlainField(invoice.mobile, onMobile, "+8801XXXXXXXXX", BodyInput, keyboardType = KeyboardType.Phone)
            }
        }
    }
}

@Composable
private fun LabelledRow(
    label: String,
    divider: Boolean,
    alignTop: Boolean = false,
    field: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = Muted,
            modifier = Modifier.width(66.dp).padding(top = if (alignTop) 5.dp else 0.dp)
        )
        Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) { field() }
    }
    if (divider) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Divider))
    }
}

@Composable
private fun DocumentSection(
    invoice: Invoice,
    onSerial: (String) -> Unit,
    onDate: (String) -> Unit,
    onRpr: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("DOCUMENT")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CaptionedField("Serial", invoice.serial, onSerial, "DT&E1201", Modifier.weight(1f))
            CaptionedField("Date", invoice.date, onDate, "09.08.2026", Modifier.weight(1f))
        }
        CaptionedField("R/PR", invoice.rpr, onRpr, "Requisition or PR reference", Modifier.fillMaxWidth())
    }
}

@Composable
private fun CaptionedField(
    caption: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.formCard().padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(caption, fontSize = 10.5.sp, color = Label, letterSpacing = 0.3.sp)
        Box(modifier = Modifier.padding(top = 3.dp)) {
            PlainField(value, onValueChange, placeholder, CardInput)
        }
    }
}

@Composable
private fun ItemsSection(
    invoice: Invoice,
    onItemDesc: (Int, String) -> Unit,
    onItemQty: (Int, String) -> Unit,
    onItemPrice: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("ITEMS", Modifier.weight(1f))
            val count = invoice.items.size
            Text(
                text = if (count == 1) "1 row" else "$count rows",
                fontSize = 11.sp,
                color = Label
            )
        }

        invoice.items.forEachIndexed { index, item ->
            ItemCard(
                index = index,
                item = item,
                isMemo = invoice.isMemo,
                onDesc = { onItemDesc(index, it) },
                onQty = { onItemQty(index, it) },
                onPrice = { onItemPrice(index, it) },
                onRemove = { onRemoveItem(index) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dashedOutline()
                .clickable { onAddItem() }
                .padding(13.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("+", fontSize = 17.sp, color = Muted)
                Text("Add item", fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Muted)
            }
        }
    }
}

@Composable
private fun ItemCard(
    index: Int,
    item: LineItem,
    isMemo: Boolean,
    onDesc: (String) -> Unit,
    onQty: (String) -> Unit,
    onPrice: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .formCard()
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(22.dp).background(OrangeTint, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Orange)
            }
            Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                PlainField(item.desc, onDesc, "Item description", BodyInput)
            }
            Text(
                text = "×",
                fontSize = 20.sp,
                color = Faint,
                modifier = Modifier.clickable { onRemove() }.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniField(
                caption = "QUANTITY",
                value = item.qty,
                onValueChange = onQty,
                placeholder = "24 Pcs",
                modifier = Modifier.weight(1f)
            )
            if (isMemo) {
                MiniField(
                    caption = "UNIT PRICE",
                    value = item.price,
                    onValueChange = onPrice,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                // Line total is derived from quantity x unit price, so it is display-only.
                ComputedField(
                    caption = "TOTAL PRICE",
                    value = formatMoney(item.amount),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun MiniField(
    caption: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = modifier
            .background(Canvas, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(caption, fontSize = 9.5.sp, color = Label, letterSpacing = 0.4.sp)
        Box(modifier = Modifier.padding(top = 2.dp)) {
            PlainField(value, onValueChange, placeholder, CompactInput, keyboardType = keyboardType)
        }
    }
}

@Composable
private fun ComputedField(caption: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(OrangeTint, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(caption, fontSize = 9.5.sp, color = OrangeMuted, letterSpacing = 0.4.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OrangeDeep,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun TotalSection(invoice: Invoice) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("TOTAL")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("AUTO-CALCULATED", fontSize = 10.5.sp, color = Muted, letterSpacing = 0.6.sp)
                Text(
                    text = formatMoney(invoice.total),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Surface,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SignaturesSection(
    invoice: Invoice,
    onCustSign: (String) -> Unit,
    onSellerSign: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("SIGNATURES")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SignatureField("Customer", invoice.custSign, onCustSign, Modifier.weight(1f))
            SignatureField("Seller", invoice.sellerSign, onSellerSign, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SignatureField(
    caption: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.formCard().padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(caption, fontSize = 10.5.sp, color = Label)
        Box(modifier = Modifier.padding(top = 3.dp)) {
            PlainField(value, onValueChange, "Name", SmallInput)
        }
    }
}

@Composable
private fun GenerateBar(onGenerate: () -> Unit) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderLine))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Orange, RoundedCornerShape(percent = 50))
                    .clickable { onGenerate() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Generate PDF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                    color = Surface,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(0.dp))
    }
}
