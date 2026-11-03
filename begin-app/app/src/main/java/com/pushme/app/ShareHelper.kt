package com.pushme.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import java.io.File
import java.io.FileOutputStream

/** Turns the message on the card into a square picture and opens the phone's share sheet. */
object ShareHelper {

    fun shareCard(activity: Activity) {
        try {
            val size = 1080
            val s = size.toFloat()
            val theme = Themes.current(activity)

            val picture = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(picture)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Background: the theme's own colors, with two soft discs.
            val top = if (theme.dark) theme.bgStart else ColorUtils.blendARGB(theme.start, 0xFFFFFFFF.toInt(), 0.15f)
            val bottom = if (theme.dark) theme.bgEnd else ColorUtils.blendARGB(theme.end, 0xFFFFFFFF.toInt(), 0.15f)
            paint.shader = LinearGradient(0f, 0f, s, s, top, bottom, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, s, s, paint)
            paint.shader = null
            paint.color = ColorUtils.setAlphaComponent(theme.start, 150)
            canvas.drawCircle(s * 0.86f, s * 0.16f, s * 0.26f, paint)
            paint.color = ColorUtils.setAlphaComponent(theme.end, 150)
            canvas.drawCircle(s * 0.10f, s * 0.90f, s * 0.32f, paint)

            // The glass card on top, drawn by the same code as the widget.
            val card = WidgetArt.render(activity, 340, 340, dim = false)
            paint.isFilterBitmap = true
            paint.color = 0xFFFFFFFF.toInt()
            val pad = 90f
            canvas.drawBitmap(card, null, RectF(pad, pad, s - pad, s - pad), paint)

            val folder = File(activity.cacheDir, "share")
            folder.mkdirs()
            val file = File(folder, "begin-message.png")
            FileOutputStream(file).use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) }

            val uri = FileProvider.getUriForFile(activity, activity.packageName + ".files", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(send, Ui.shareTitle(activity)))
        } catch (e: Exception) {
            Toast.makeText(activity, Ui.shareFailed(activity), Toast.LENGTH_SHORT).show()
        }
    }
}
