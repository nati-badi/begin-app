package com.pushme.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat

const val MATCH = -1
const val WRAP = -2

/**
 * Colors and small view-building helpers shared by every screen, so the whole app looks
 * like one thing. Make a new Look whenever a screen (re)appears, so theme changes show up.
 */
class Look(val context: Context) {
    val theme: AppTheme = Themes.current(context)
    val dark: Boolean = theme.dark
    private val lines: Int = Store.lines(context)

    /** "Bigger tap targets" setting. */
    val big: Boolean = Store.bigTargets(context)

    /** Side of the round icon buttons (back, settings, heart, share) in dp. */
    val iconDp: Int = if (big) 48 else 40

    val ink: Int = Color.parseColor(if (dark) "#F3F4F6" else "#1F2937")
    val muted: Int = Color.parseColor(if (dark) "#AEB5C2" else "#6B7280")
    val rose: Int = Color.parseColor("#F43F5E")

    // Selected things are slightly raised: a light-to-dark gradient instead of a flat fill.
    val selTop: Int = if (dark) Color.WHITE else Color.parseColor("#374151")
    val selBottom: Int = if (dark) Color.parseColor("#D5DAE3") else Color.parseColor("#111827")
    val selText: Int = if (dark) Color.parseColor("#111827") else Color.WHITE

    val softFill: Int = if (dark) Color.argb(0x24, 255, 255, 255) else Color.argb(0x66, 255, 255, 255)
    val quietFill: Int = if (dark) Color.argb(0x1A, 255, 255, 255) else Color.argb(0x12, 0x1F, 0x29, 0x37)
    val panelFill: Int = if (dark) Color.argb(0x14, 255, 255, 255) else Color.argb(0xA6, 255, 255, 255)

    /** Thin line color used for borders and dividers. Transparent when lines are switched off. */
    val hairline: Int = when (lines) {
        0 -> Color.TRANSPARENT
        2 -> if (dark) Color.argb(0x59, 255, 255, 255) else Color.argb(0x40, 0x1F, 0x29, 0x37)
        else -> if (dark) Color.argb(0x24, 255, 255, 255) else Color.argb(0x1F, 0x1F, 0x29, 0x37)
    }

    /** Light edge of the glass card. */
    val edge: Int = when (lines) {
        0 -> Color.TRANSPARENT
        2 -> Color.argb(0x99, 255, 255, 255)
        else -> Color.argb(0x59, 255, 255, 255)
    }

    // ---------- sizes ----------

    fun dp(value: Int): Int = (value * context.resources.displayMetrics.density + 0.5f).toInt()

    /** Layout params for a LinearLayout child. Sizes are pixels or MATCH/WRAP; margins are dp. */
    fun lp(
        width: Int = MATCH,
        height: Int = WRAP,
        top: Int = 0,
        bottom: Int = 0,
        start: Int = 0,
        end: Int = 0
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(width, height).apply { setMargins(dp(start), dp(top), dp(end), dp(bottom)) }

    // ---------- window ----------

    /** Colors the status bar, navigation bar and window to match the theme. */
    fun applyWindow(activity: Activity) {
        val top = Themes.shift(context, theme.bgStart)
        val bottom = Themes.shift(context, theme.bgEnd)
        activity.window.setBackgroundDrawable(ColorDrawable(top))
        activity.window.statusBarColor = top
        activity.window.navigationBarColor = bottom
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    // ---------- shapes ----------

    fun rounded(fill: Int, radiusDp: Int, stroke: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
        }

    /** A shape with a soft top-to-bottom gradient, so it looks raised instead of flat. */
    fun raised(top: Int, bottom: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom)).apply {
            cornerRadius = dp(radiusDp).toFloat()
            if (hairline != Color.TRANSPARENT) setStroke(dp(1), Color.argb(0x40, 255, 255, 255))
        }

    fun icon(resId: Int, sizeDp: Int, tint: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, resId)!!.mutate()
        drawable.setBounds(0, 0, dp(sizeDp), dp(sizeDp))
        drawable.setTint(tint)
        return drawable
    }

    // ---------- buttons ----------

    /** Squeezes a view slightly while it is pressed. Clicks still work. */
    @SuppressLint("ClickableViewAccessibility")
    fun pressable(view: View) {
        view.setOnTouchListener { v, event ->
            if (!Store.animations(context)) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }
    }

    /** The main button: raised, with a soft colored glow underneath and a press effect. */
    fun stylePrimary(button: TextView, radiusDp: Int = 31) {
        button.background = raised(selTop, selBottom, radiusDp)
        button.setTextColor(selText)
        button.elevation = dp(8).toFloat()
        if (Build.VERSION.SDK_INT >= 28) {
            button.outlineSpotShadowColor = theme.start
            button.outlineAmbientShadowColor = theme.end
        }
        pressable(button)
    }

    /** Round glass button holding one icon (settings, back, heart ...). */
    fun styleIconButton(button: ImageView, tint: Int = ink) {
        button.background = rounded(softFill, iconDp / 2, hairline)
        button.imageTintList = ColorStateList.valueOf(tint)
        pressable(button)
    }

    // ---------- chips ----------

    fun chip(label: CharSequence): TextView = TextView(context).apply {
        text = label
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER
        if (big) setPadding(dp(20), dp(14), dp(20), dp(14)) else setPadding(dp(16), dp(10), dp(16), dp(10))
    }

    fun styleChip(chip: TextView, selected: Boolean, quiet: Boolean = false, radiusDp: Int = 100) {
        chip.background = if (selected) {
            raised(selTop, selBottom, radiusDp)
        } else {
            rounded(if (quiet) quietFill else softFill, radiusDp, hairline)
        }
        chip.setTextColor(if (selected) selText else ink)
    }

    /**
     * A sideways-scrolling row of chips. [selected] is highlighted. Pass [out] to get the chips
     * back (to restyle them without rebuilding the row).
     */
    fun chipsRow(
        labels: List<CharSequence>,
        selected: Int,
        quiet: Boolean = true,
        radiusDp: Int = 100,
        out: MutableList<TextView>? = null,
        onPick: (Int) -> Unit
    ): HorizontalScrollView {
        val scroller = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        labels.forEachIndexed { index, label ->
            val chip = chip(label)
            styleChip(chip, index == selected, quiet, radiusDp)
            chip.setOnClickListener {
                Haptics.tick(context)
                onPick(index)
            }
            row.addView(chip, lp(WRAP, WRAP, end = 8))
            out?.add(chip)
        }
        scroller.addView(row, FrameLayout.LayoutParams(WRAP, WRAP))
        return scroller
    }

    // ---------- panels and dividers ----------

    /** A rounded container with a faint border, used to group rows. */
    fun panel(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(panelFill, 20, hairline)
    }

    /** A thin line between two rows (invisible when lines are off). */
    fun divider(): View = View(context).apply { setBackgroundColor(hairline) }

    fun dividerParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(MATCH, maxOf(1, dp(1)))
}
