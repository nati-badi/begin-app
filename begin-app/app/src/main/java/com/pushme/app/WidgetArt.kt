package com.pushme.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.ColorUtils
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Paints the whole widget as a picture. Home-screen widgets cannot use custom fonts or
 * blur the wallpaper, so we draw our own "glass": soft colored blobs under a tinted,
 * lightly shining panel, then the message on top in the chosen fonts.
 * It works at any widget size, from a thin strip to a big square.
 */
object WidgetArt {

    // Largest text size (sp) for Small / Medium / Large.
    private val MAX_SP = floatArrayOf(20f, 30f, 42f)

    fun render(context: Context, widthDp: Int, heightDp: Int, dim: Boolean = true): Bitmap {
        val density = context.resources.displayMetrics.density
        var w = widthDp * density
        var h = heightDp * density

        // Keep the picture a sensible size (widgets have a memory limit).
        val maxPixels = 1_000_000f
        val shrink = min(1f, sqrt(maxPixels / (w * h)))
        w *= shrink
        h *= shrink
        val s = density * shrink // pixels per dp

        val bitmap = Bitmap.createBitmap(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(0f, 0f, w, h)
        val compact = heightDp < 100
        val radius = min(28f * s, min(w, h) / 3f)

        val theme = Themes.current(context)
        val level = Themes.level(context)

        // Light themes are softened (blended toward white) so the widget is never harsh.
        val startNow = Themes.shift(context, theme.start)
        val endNow = Themes.shift(context, theme.end)
        val colorA = if (theme.dark) startNow else ColorUtils.blendARGB(startNow, Color.WHITE, 0.30f)
        val colorB = if (theme.dark) endNow else ColorUtils.blendARGB(endNow, Color.WHITE, 0.30f)

        canvas.save()
        val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Soft colored base. More frosted = more solid.
        val baseTop = (level.alphaTop * 1.5f).toInt().coerceAtMost(255)
        val baseBottom = (level.alphaBottom * 1.5f).toInt().coerceAtMost(255)
        paint.shader = LinearGradient(
            0f, 0f, w, h,
            ColorUtils.setAlphaComponent(colorA, baseTop),
            ColorUtils.setAlphaComponent(colorB, baseBottom),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, paint)

        // 2. Two soft glowing blobs, like colors seen through frosted glass.
        val blobAlpha = (if (theme.dark) 90 + level.alphaTop / 3 else 55 + level.alphaTop / 4).coerceAtMost(170)
        // Drifting glass: a widget cannot animate live, so the two glows sit somewhere new on
        // every refresh (a full lap takes 3 hours), which reads as slow movement over time.
        val phase = if (Store.drift(context)) {
            ((System.currentTimeMillis() / 60000L) % 180L) / 180f * 6.2831855f
        } else {
            0f
        }
        val moveX = if (Store.drift(context)) w * 0.10f else 0f
        val moveY = if (Store.drift(context)) h * 0.12f else 0f
        drawBlob(
            canvas, paint, w * 0.85f + moveX * sin(phase), h * 0.15f + moveY * cos(phase),
            min(w, h) * 0.9f, ColorUtils.setAlphaComponent(colorA, blobAlpha)
        )
        drawBlob(
            canvas, paint, w * 0.12f - moveX * sin(phase + 1.3f), h * 0.90f - moveY * cos(phase + 2.1f),
            min(w, h) * 1.0f, ColorUtils.setAlphaComponent(colorB, blobAlpha)
        )

        // 3. A light (or dark) veil, then a soft shine along the top.
        paint.shader = null
        val veil = if (theme.dark) 0 else 255
        paint.color = Color.argb(level.alphaTop / 3, veil, veil, veil)
        canvas.drawRect(rect, paint)
        // Back to a fully solid paint, otherwise the shine below inherits the veil's transparency.
        paint.color = Color.WHITE
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.6f,
            Color.argb(if (theme.dark) 0x26 else 0x30, 255, 255, 255), Color.argb(0, 255, 255, 255),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, paint)
        paint.shader = null
        canvas.restore()

        // 4. A very thin, soft edge (not a hard border).
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * s
        paint.color = Color.argb(if (theme.dark) 0x55 else 0x40, 255, 255, 255)
        val inset = 0.5f * s
        canvas.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            radius - inset, radius - inset, paint
        )

        drawTexts(context, canvas, w, h, s, compact)
        if (!compact && Store.gestureMode(context) != 0) drawGestureHints(context, canvas, w, h, s)

        // Late at night the whole widget gets a little darker (only where it has color, the corners stay clear).
        if (dim && Store.nightDim(context)) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour >= 22 || hour < 6) {
                val night = Paint()
                night.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
                night.color = Color.argb(if (theme.dark) 40 else 64, 0, 0, 0)
                canvas.drawRect(rect, night)
            }
        }
        return bitmap
    }

    private fun drawBlob(canvas: Canvas, paint: Paint, cx: Float, cy: Float, r: Float, color: Int) {
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, r, color, ColorUtils.setAlphaComponent(color, 0), Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, r, paint)
        paint.shader = null
    }

    private fun layoutAt(
        text: CharSequence,
        paint: TextPaint,
        sizePx: Float,
        width: Int,
        maxLines: Int
    ): StaticLayout {
        paint.textSize = sizePx
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
            .setLineSpacing(0f, 1.1f)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
    }

    /** Two faint chevrons, top and bottom centre: tap there to go forward / back through your Saved or Mine list. */
    private fun drawGestureHints(context: Context, canvas: Canvas, w: Float, h: Float, s: Float) {
        val dark = Themes.darkText(context)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 2f * s
        stroke.strokeCap = Paint.Cap.ROUND
        stroke.strokeJoin = Paint.Join.ROUND
        stroke.color = if (dark) Color.argb(0x70, 0x1F, 0x29, 0x37) else Color.argb(0x88, 255, 255, 255)
        val cx = w / 2f
        val r = 6f * s
        val topY = 17f * s
        canvas.drawPath(Path().apply {
            moveTo(cx - r, topY + r / 2f); lineTo(cx, topY - r / 2f); lineTo(cx + r, topY + r / 2f)
        }, stroke)
        val bottomY = h - 13f * s
        canvas.drawPath(Path().apply {
            moveTo(cx - r, bottomY - r / 2f); lineTo(cx, bottomY + r / 2f); lineTo(cx + r, bottomY - r / 2f)
        }, stroke)
    }

    private fun drawTexts(context: Context, canvas: Canvas, w: Float, h: Float, s: Float, compact: Boolean) {
        val dark = Themes.darkText(context)
        val main = if (dark) 0xFF263244.toInt() else 0xFFFFFFFF.toInt()
        val soft = if (dark) 0xFF566070.toInt() else 0xCCFFFFFF.toInt()
        val text = Store.current(context)
        val shown = Library.tidy(text)

        // Small caption in the top-left, e.g. "TINY STEP". Hidden on very short widgets.
        if (!compact) {
            val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = if (Ui.isAmharic(context)) Fonts.amharicFace(context) else Fonts.englishFace(context)
                textSize = 11f * s
                letterSpacing = if (Ui.isAmharic(context)) 0f else 0.12f
                color = soft
            }
            canvas.drawText(Ui.caption(context, text), 22f * s, 30f * s, label)
        }

        // The message, centered, as large as will fit.
        val padX = (if (compact) 14f else 22f) * s
        val top = (if (compact) 8f else 44f) * s
        val bottom = (if (compact) 8f else 22f) * s
        val availW = (w - 2 * padX).toInt().coerceAtLeast(1)
        val availH = (h - top - bottom).coerceAtLeast(1f)

        val styled = Fonts.styled(context, shown)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = main
            if (!dark) setShadowLayer(3f * s, 0f, 1f * s, 0x30000000)
        }

        val maxSp = MAX_SP[Store.widgetText(context).coerceIn(0, MAX_SP.lastIndex)]
        val minSp = if (compact) 8f else 12f
        var sizeSp = maxSp
        var layout = layoutAt(styled, paint, sizeSp * s, availW, Int.MAX_VALUE)
        while (layout.height > availH && sizeSp > minSp) {
            sizeSp -= 1f
            layout = layoutAt(styled, paint, sizeSp * s, availW, Int.MAX_VALUE)
        }
        if (layout.height > availH) {
            // Still too tall at the smallest size: show fewer lines and end with "...".
            val lines = (availH / (paint.fontSpacing * 1.1f)).toInt().coerceAtLeast(1)
            layout = layoutAt(styled, paint, sizeSp * s, availW, lines)
        }

        canvas.save()
        canvas.translate(padX, top + (availH - layout.height) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }
}
