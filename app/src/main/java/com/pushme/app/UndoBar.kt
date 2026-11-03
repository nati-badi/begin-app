package com.pushme.app

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/** A small dark bar at the bottom ("Removed   UNDO") that disappears by itself after a few seconds. */
object UndoBar {
    private const val TAG = "undo_bar"
    private const val SHOW_MS = 4500L

    fun show(host: FrameLayout, look: Look, text: String, action: String, onUndo: () -> Unit) {
        dismissNow(host)

        val bar = LinearLayout(host.context)
        bar.tag = TAG
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.background = look.rounded(Color.parseColor("#F2202733"), 18)
        bar.elevation = look.dp(8).toFloat()
        bar.setPadding(look.dp(18), look.dp(4), look.dp(6), look.dp(4))

        val label = TextView(host.context)
        label.text = text
        label.textSize = 14f
        label.setTextColor(Color.parseColor("#F3F4F6"))
        bar.addView(label, LinearLayout.LayoutParams(0, WRAP, 1f))

        val button = TextView(host.context)
        button.text = if (Ui.isAmharic(host.context)) action else action.uppercase()
        button.textSize = 13f
        button.setTypeface(button.typeface, android.graphics.Typeface.BOLD)
        button.setTextColor(Color.parseColor("#9EC9FF"))
        button.setPadding(look.dp(14), look.dp(12), look.dp(14), look.dp(12))
        button.setOnClickListener {
            dismissNow(host)
            onUndo()
        }
        bar.addView(button)

        val params = FrameLayout.LayoutParams(MATCH, WRAP, Gravity.BOTTOM)
        params.setMargins(look.dp(22), 0, look.dp(22), look.dp(28))
        host.addView(bar, params)

        if (Motion.on(host.context)) {
            bar.alpha = 0f
            bar.translationY = look.dp(16).toFloat()
            bar.animate().alpha(1f).translationY(0f).setDuration(200).start()
        }
        host.postDelayed({ fadeOut(host, bar) }, SHOW_MS)
    }

    private fun dismissNow(host: FrameLayout) {
        for (i in host.childCount - 1 downTo 0) {
            if (host.getChildAt(i).tag == TAG) host.removeViewAt(i)
        }
    }

    private fun fadeOut(host: FrameLayout, bar: View) {
        if (bar.parent !== host) return
        if (Motion.on(host.context)) {
            bar.animate().alpha(0f).setDuration(200).withEndAction {
                if (bar.parent === host) host.removeView(bar)
            }.start()
        } else {
            host.removeView(bar)
        }
    }
}
