package com.mustafafoisol.androidapp3.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mustafafoisol.androidapp3.data.DocType

/**
 * Scanned letterhead artwork laid under the generated page.
 *
 * Drop the blank artwork into `res/drawable-nodpi` as `template_challan.png` and
 * `template_memo.png`. Either may be absent, in which case [PdfBuilder] falls back to
 * drawing the letterhead and brand strip itself.
 */
object Templates {

    /** Roughly A4 at 200dpi; enough for print without holding a huge scan in memory. */
    private const val TARGET_WIDTH = 1654

    private val cache = mutableMapOf<DocType, Bitmap?>()

    fun load(context: Context, docType: DocType): Bitmap? = cache.getOrPut(docType) {
        val name = if (docType == DocType.MEMO) "template_memo" else "template_challan"
        @Suppress("DiscouragedApi")
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (resId == 0) null else decodeScaled(context, resId)
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
