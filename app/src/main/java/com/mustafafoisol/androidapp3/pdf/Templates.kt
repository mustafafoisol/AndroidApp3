package com.mustafafoisol.androidapp3.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mustafafoisol.androidapp3.data.DocType

/**
 * Scanned letterhead artwork laid under the generated page.
 *
 * Drop the blank artwork into `res/drawable-nodpi` as `template_letterhead.png`. The
 * artwork carries only the header strip, watermark and brand footer, so one file serves
 * both document types; `template_challan.png` / `template_memo.png` override it per type
 * if they ever need to differ. With none present [PdfBuilder] draws its own letterhead.
 */
object Templates {

    /** Roughly A4 at 200dpi; enough for print without holding a huge scan in memory. */
    private const val TARGET_WIDTH = 1654

    private val cache = mutableMapOf<DocType, Bitmap?>()

    fun load(context: Context, docType: DocType): Bitmap? = cache.getOrPut(docType) {
        val perType = if (docType == DocType.MEMO) "template_memo" else "template_challan"
        val resId = resolve(context, perType) ?: resolve(context, "template_letterhead")
        if (resId == null) null else decodeScaled(context, resId)
    }

    private fun resolve(context: Context, name: String): Int? {
        @Suppress("DiscouragedApi")
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (resId == 0) null else resId
    }

    private fun decodeScaled(context: Context, resId: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)
        if (bounds.outWidth <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= TARGET_WIDTH) sample *= 2

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return runCatching {
            BitmapFactory.decodeResource(context.resources, resId, options)
        }.getOrNull()
    }
}
