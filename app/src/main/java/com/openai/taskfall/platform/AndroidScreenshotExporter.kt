package com.openai.taskfall.platform

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.view.View
import com.openai.taskfall.domain.model.SessionSummary

class AndroidScreenshotExporter(
    private val context: Context,
    private val view: View,
) : ScreenshotExporter {
    override fun export(summary: SessionSummary): Result<Unit> = runCatching {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "taskfall-${summary.createdAtEpochMs}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Taskfall")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create screenshot destination")
        val output = checkNotNull(context.contentResolver.openOutputStream(uri))
        output.use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Screenshot encoding failed" }
        }
        bitmap.recycle()
    }
}
