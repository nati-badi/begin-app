package com.pushme.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.sin

/** A tiny burst of dots that pops out of a point, used when you save a message. Never takes touches. */
class BurstView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cx = 0f
    private var cy = 0f
    private var color = 0
    private var progress = 1f
    private var animator: ValueAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    fun fire(x: Float, y: Float, tint: Int) {
        if (!Motion.on(context)) return
        cx = x
        cy = y
        color = tint
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (progress >= 1f) return
        val d = resources.displayMetrics.density
        val dots = 10
        val alpha = ((1f - progress) * 255f).toInt().coerceIn(0, 255)
        paint.reset()
        paint.isAntiAlias = true
        paint.color = (color and 0x00FFFFFF) or (alpha shl 24)
        for (i in 0 until dots) {
            val angle = i * (6.2831855f / dots)
            val distance = d * (14f + 34f * progress)
            val radius = d * (3.2f * (1f - progress) + 0.8f)
            canvas.drawCircle(cx + cos(angle) * distance, cy + sin(angle) * distance, radius, paint)
        }
    }
}
