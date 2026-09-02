package com.mustafafoisol.androidapp3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mustafafoisol.androidapp3.data.InvoiceViewModel
import com.mustafafoisol.androidapp3.pdf.PdfBuilder
import com.mustafafoisol.androidapp3.pdf.Templates
import com.mustafafoisol.androidapp3.ui.screens.FormScreen
import com.mustafafoisol.androidapp3.ui.screens.PreviewScreen
import com.mustafafoisol.androidapp3.ui.theme.AndroidApp3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidApp3Theme {
                InvoiceApp()
            }
        }
    }
}

@Composable
private fun InvoiceApp(viewModel: InvoiceViewModel = viewModel()) {
    val context = LocalContext.current
    val invoice = viewModel.invoice
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }

    val root = Modifier.fillMaxSize().safeDrawingPadding().imePadding()
    val bytes = pdfBytes

    if (bytes == null) {
        Box(modifier = root) {
            FormScreen(
                invoice = invoice,
                onDocType = viewModel::setDocType,
                onName = viewModel::setName,
                onAddress = viewModel::setAddress,
                onMobile = viewModel::setMobile,
                onSerial = viewModel::setSerial,
                onRpr = viewModel::setRpr,
                onDate = viewModel::setDate,
                onItemDesc = viewModel::setItemDesc,
                onItemQty = viewModel::setItemQty,
                onItemPrice = viewModel::setItemPrice,
                onAddItem = viewModel::addItem,
                onRemoveItem = viewModel::removeItem,
                onCustSign = viewModel::setCustSign,
                onSellerSign = viewModel::setSellerSign,
                onGenerate = {
                    val background = Templates.load(context, invoice.docType)
                    pdfBytes = PdfBuilder.render(invoice, background)
                }
            )
        }
    } else {
        BackHandler { pdfBytes = null }
        Box(modifier = root) {
            PreviewScreen(
                fileName = invoice.fileName,
                pdfBytes = bytes,
                onBack = { pdfBytes = null }
            )
        }
    }
}
