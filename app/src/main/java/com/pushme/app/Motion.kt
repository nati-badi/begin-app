package com.pushme.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.animation.ValueAnimator
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import android.view.ViewGroup
import android.widget.ImageView

/** All the soft motion in one place, so one switch in Settings turns every bit of it off. */
object Motion {
    fun on(c: Context): Boolean = Store.animations(c)

    /** Fades views in, one after another, each sliding up a little. */
    fun enter(c: Context, views: List<View>, startDelay: Long = 0, step: Long = 50) {
        if (!on(c)) return
        val lift = c.resources.displayMetrics.density * 14f
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = lift
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelay + index * step)
                .setDuration(320)
                .start()
        }
    }

    /** One word whose see-through-ness can change while the text stays still (no re-layout). */
    private class WordAlpha(private val base: Int) : CharacterStyle(), UpdateAppearance {
        var alpha = 0f
        override fun updateDrawState(tp: TextPaint) {
            tp.color = ColorUtils.setAlphaComponent(base, (Color.alpha(base) * alpha).toInt())
        }
    }

    /**
     * Shows [text] in [view] with the words easing in one after another (about a second in all).
     * With animations off, the text just appears. A new call stops the previous reveal.
     */
    fun revealWords(c: Context, view: TextView, text: CharSequence) {
        (view.getTag(R.id.message) as? ValueAnimator)?.cancel()
        if (!on(c) || text.isBlank()) {
            view.text = text
            return
        }
        val spannable = SpannableString(text)
        val words = ArrayList<WordAlpha>()
        val s = spannable.toString()
        var i = 0
        while (i < s.length) {
            while (i < s.length && s[i].isWhitespace()) i++
            val start = i
            while (i < s.length && !s[i].isWhitespace()) i++
            if (i > start) {
                val word = WordAlpha(view.currentTextColor)
                spannable.setSpan(word, start, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                words.add(word)
            }
        }
        view.text = spannable
        val count = words.size
        val overlap = 3f
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = (260L + 80L * count).coerceAtMost(1100L)
        animator.addUpdateListener {
            val position = (it.animatedValue as Float) * (count + overlap)
            words.forEachIndexed { index, word -> word.alpha = ((position - index) / overlap).coerceIn(0f, 1f) }
            view.invalidate()
        }
        view.setTag(R.id.message, animator)
        animator.start()
    }

    /** A little pop, like a button that was just pressed. */
    fun pop(c: Context, view: View) {
        if (!on(c)) return
        view.animate().scaleX(1.25f).scaleY(1.25f).setDuration(90).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()
    }

    /**
     * Runs [change] (for example a new color theme) and fades from the old look to the new one,
     * instead of snapping. Works by laying a snapshot of the old screen on top and fading it out.
     */
    fun crossfade(activity: Activity, change: () -> Unit) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        if (!on(activity) || content == null || content.width <= 0 || content.height <= 0) {
            change()
            return
        }
        val snapshot = try {
            Bitmap.createBitmap(content.width, content.height, Bitmap.Config.ARGB_8888).also {
                content.draw(Canvas(it))
            }
        } catch (e: Exception) {
            null
        }
        if (snapshot == null) {
            change()
            return
        }
        val overlay = ImageView(activity)
        overlay.setImageBitmap(snapshot)
        content.addView(overlay, ViewGroup.LayoutParams(MATCH, MATCH))
        change()
        overlay.animate().alpha(0f).setDuration(320).withEndAction { content.removeView(overlay) }.start()
    }

    /** Opens another screen; with animations off the screen simply appears. */
    @Suppress("DEPRECATION")
    fun open(from: Activity, intent: Intent) {
        from.startActivity(intent)
        if (!on(from)) from.overridePendingTransition(0, 0)
    }
}
