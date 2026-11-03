package com.pushme.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/** A soft pool of light that blooms where you touch the glass card, then fades. Never takes touches itself. */
class TouchGlowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cx = 0f
    private var cy = 0f
    private var progress = 1f
    private var animator: ValueAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    fun pulse(x: Float, y: Float) {
        if (!Motion.on(context)) return
        cx = x
        cy = y
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
        val radius = resources.displayMetrics.density * (70f + 110f * progress)
        val alpha = ((1f - progress) * 70f).toInt().coerceIn(0, 255)
        paint.reset()
        paint.isAntiAlias = true
        paint.shader = RadialGradient(
            cx, cy, radius,
            Color.argb(alpha, 255, 255, 255), Color.argb(0, 255, 255, 255),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
    }
}
