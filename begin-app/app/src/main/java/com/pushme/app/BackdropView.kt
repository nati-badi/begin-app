package com.pushme.app

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * Draws the soft colored background art (gradient, two discs, a ring).
 * One copy fills the screen sharply. A second copy sits inside the glass card, is
 * shifted so it lines up with the first, and is blurred. That is what makes the card
 * look like frosted glass. In "plain" mode only the gradient is drawn (used by Settings,
 * where text must stay easy to read).
 */
class BackdropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var theme: AppTheme = Themes.all[0]
    private var plain = false

    // Size of the whole screen area, and where this view sits inside it.
    private var refW = 0f
    private var refH = 0f
    private var offX = 0f
    private var offY = 0f

    fun setArt(t: AppTheme) {
        theme = t
        invalidate()
    }

    fun setPlain(value: Boolean) {
        plain = value
        invalidate()
    }

    fun setFrame(screenW: Float, screenH: Float, x: Float, y: Float) {
        refW = screenW
        refH = screenH
        offX = x
        offY = y
        invalidate()
    }

    // The same Paint is used for every step below. Each step starts from a clean Paint,
    // because a leftover see-through color used to make the background fade after the
    // first redraw (the gradient picked up the transparency of the ring drawn last time).
    private fun fill(color: Int) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
    }

    private fun outline(color: Int, width: Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        val w = if (refW > 0f) refW else width.toFloat()
        val h = if (refH > 0f) refH else height.toFloat()
        if (w <= 0f || h <= 0f) return

        canvas.save()
        canvas.translate(-offX, -offY)

        // Base gradient.
        paint.reset()
        paint.isAntiAlias = true
        paint.shader = LinearGradient(
            0f, 0f, w, h,
            Themes.shift(context, theme.bgStart), Themes.shift(context, theme.bgEnd),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        if (!plain) {
            // Slow drift: every shape floats a little, like light moving through water.
            // The phase comes from the clock, so the sharp and the blurred copy stay in step.
            val drifting = Motion.on(context) && Store.drift(context)
            val phase = if (drifting) (SystemClock.uptimeMillis() % 36000L) / 36000f * TWO_PI else 0f
            val reach = if (drifting) w * 0.025f else 0f
            fun dx(speed: Int, shift: Float) = reach * sin(phase * speed + shift)
            fun dy(speed: Int, shift: Float) = reach * cos(phase * speed + shift)

            fill(Themes.shift(context, theme.start))
            canvas.drawCircle(w * 0.88f + dx(1, 0f), h * 0.22f + dy(1, 0f), w * 0.30f, paint)

            fill(Themes.shift(context, theme.end))
            canvas.drawCircle(w * 0.10f + dx(1, 2f), h * 0.60f + dy(2, 1f), w * 0.34f, paint)

            fill(Color.argb(if (theme.dark) 70 else 200, 255, 255, 255))
            canvas.drawCircle(w * 0.30f + dx(2, 3f), h * 0.14f + dy(1, 4f), w * 0.11f, paint)

            outline(Color.argb(if (theme.dark) 90 else 204, 255, 255, 255), w * 0.07f)
            canvas.drawCircle(w * 0.74f + dx(2, 5f), h * 0.74f + dy(1, 2f), w * 0.26f, paint)

            if (drifting) postInvalidateDelayed(40)
        }

        canvas.restore()
    }
}

private const val TWO_PI = 6.2831855f
