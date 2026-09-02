package com.mustafafoisol.androidapp3.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafafoisol.androidapp3.pdf.PdfBuilder
import com.mustafafoisol.androidapp3.ui.theme.Faint
import com.mustafafoisol.androidapp3.ui.theme.Orange
import com.mustafafoisol.androidapp3.ui.theme.PreviewBackdrop
import com.mustafafoisol.androidapp3.ui.theme.PreviewChipBorder
import com.mustafafoisol.androidapp3.ui.theme.PreviewOutline
import com.mustafafoisol.androidapp3.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Preview raster width; A4 at roughly 150dpi, which stays sharp when scaled to fit. */
private const val PREVIEW_WIDTH_PX = 1240

@Composable
fun PreviewScreen(
    fileName: String,
    pdfBytes: ByteArray,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var page by remember(pdfBytes) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pdfBytes) {
        page = withContext(Dispatchers.IO) { renderFirstPage(context, pdfBytes) }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val saved = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(pdfBytes) }
        }.isSuccess
        Toast.makeText(
            context,
            if (saved) "Saved $fileName" else "Could not save the PDF",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(PreviewBackdrop)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "←",
                fontSize = 22.sp,
                color = Surface,
                modifier = Modifier.clickable { onBack() }.padding(horizontal = 2.dp)
            )
            Text(
                text = fileName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .border(1.dp, PreviewChipBorder, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text("A4", fontSize = 10.5.sp, color = Faint)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp)
        ) {
            val bitmap = page
            val sheet = Modifier
                .fillMaxWidth()
                .aspectRatio(PdfBuilder.PAGE_WIDTH_PT.toFloat() / PdfBuilder.PAGE_HEIGHT_PT)
                .background(Surface)
            if (bitmap == null) {
                Box(modifier = sheet)
            } else {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Document preview",
                    modifier = sheet,
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, PreviewOutline, RoundedCornerShape(percent = 50))
                    .clickable { onBack() }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Edit", fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = Surface)
            }
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .background(Orange, RoundedCornerShape(percent = 50))
                    .clickable { saveLauncher.launch(fileName) }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Save as PDF", fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = Surface)
            }
        }
    }
}

/**
 * Rasterises page one of [bytes] so the preview shows the exact file that gets saved,
 * rather than a second rendering of the same layout.
 */
private fun renderFirstPage(context: Context, bytes: ByteArray): Bitmap? = runCatching {
    val file = File(context.cacheDir, "preview.pdf")
    file.writeBytes(bytes)

    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            renderer.openPage(0).use { pdfPage ->
                val height = PREVIEW_WIDTH_PX * pdfPage.height / pdfPage.width
                val bitmap = Bitmap.createBitmap(PREVIEW_WIDTH_PX, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }
}.getOrNull()
