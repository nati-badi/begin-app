package com.pushme.app

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import java.util.Calendar
import kotlin.math.abs

/**
 * A color theme. [start] and [end] are the two main colors (the discs in the background).
 * Light themes get a very pale background; dark themes give their own dark background.
 */
data class AppTheme(
    val key: String,
    val name: String,
    val start: Int,
    val end: Int,
    val dark: Boolean = false,
    private val bgA: Int? = null,
    private val bgB: Int? = null
) {
    val bgStart: Int get() = bgA ?: ColorUtils.blendARGB(start, Color.WHITE, 0.55f)
    val bgEnd: Int get() = bgB ?: ColorUtils.blendARGB(end, Color.WHITE, 0.55f)
}

/**
 * How see-through the glass is. Alphas are 0-255; [darkText] = use dark text on top
 * (only on light themes); [blurDp] = how strongly the card blurs what is behind it.
 */
data class GlassLevel(
    val name: String,
    val alphaTop: Int,
    val alphaBottom: Int,
    val darkText: Boolean,
    val blurDp: Int
)

object Themes {
    val all: List<AppTheme> = listOf(
        // Light, soft
        AppTheme("sky", "Sky", Color.parseColor("#9EC9FF"), Color.parseColor("#C9B8FF")),
        AppTheme("blush", "Blush", Color.parseColor("#FFC1D6"), Color.parseColor("#FFD9B8")),
        AppTheme("mint", "Mint", Color.parseColor("#A8E6CF"), Color.parseColor("#BFE3FF")),
        AppTheme("lilac", "Lilac", Color.parseColor("#CDB4F6"), Color.parseColor("#F4B8E4")),
        AppTheme("sand", "Sand", Color.parseColor("#FBE3A6"), Color.parseColor("#F7B9A0")),
        // Dark
        AppTheme("midnight", "Midnight", Color.parseColor("#5B63E6"), Color.parseColor("#8B5CF6"), true,
            Color.parseColor("#0B1020"), Color.parseColor("#1B1C3A")),
        AppTheme("graphite", "Graphite", Color.parseColor("#59606C"), Color.parseColor("#7C8594"), true,
            Color.parseColor("#0F1114"), Color.parseColor("#23262C")),
        AppTheme("ocean", "Ocean", Color.parseColor("#0E96A8"), Color.parseColor("#2F6FE0"), true,
            Color.parseColor("#04121C"), Color.parseColor("#0A2B3F")),
        AppTheme("ember", "Ember", Color.parseColor("#D1264F"), Color.parseColor("#E08A1E"), true,
            Color.parseColor("#18090D"), Color.parseColor("#2E1219")),
        // Northern lights: emerald and violet glowing over a deep night sky.
        AppTheme("aurora", "Aurora", Color.parseColor("#22C597"), Color.parseColor("#8A63F2"), true,
            Color.parseColor("#06130F"), Color.parseColor("#14122B"))
    )

    val glass: List<GlassLevel> = listOf(
        GlassLevel("Crystal", 0x24, 0x0F, false, 6),
        GlassLevel("Clear", 0x3D, 0x1F, false, 12),
        GlassLevel("Light", 0x66, 0x40, false, 18),
        GlassLevel("Frosted", 0xA8, 0x8F, true, 26),
        GlassLevel("Milky", 0xE6, 0xDB, true, 34)
    )

    fun current(c: Context): AppTheme {
        val index = if (Store.autoTheme(c)) {
            val night = (c.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            if (night) Store.darkIndex(c) else Store.lightIndex(c)
        } else {
            Store.themeIndex(c)
        }
        return all[index.coerceIn(0, all.lastIndex)]
    }

    /**
     * Moves a theme color a little toward peach in the morning (peak about 8 AM, up to 10%)
     * and toward soft blue at night (from 6 PM, up to 14%). Nothing happens when the switch is off.
     */
    fun shift(c: Context, color: Int): Int {
        if (!Store.timeTint(c)) return color
        val now = Calendar.getInstance()
        val h = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60f
        val warm = if (h in 5f..11f) 0.10f * (1f - abs(h - 8f) / 3f).coerceIn(0f, 1f) else 0f
        val cool = when {
            h >= 18f -> ((h - 18f) / 4f).coerceIn(0f, 1f) * 0.14f
            h < 4f -> 0.14f
            h < 6f -> (6f - h) / 2f * 0.14f
            else -> 0f
        }
        var out = color
        if (warm > 0f) out = ColorUtils.blendARGB(out, 0xFFFFB067.toInt(), warm)
        if (cool > 0f) out = ColorUtils.blendARGB(out, 0xFF5B7CFF.toInt(), cool)
        return out
    }

    fun level(c: Context): GlassLevel = glass[Store.glass(c).coerceIn(0, glass.lastIndex)]

    /**
     * Dark text on light themes from the middle glass level up (soft slate reads gentler than
     * white on pastel). Dark themes always use light text.
     */
    fun darkText(c: Context): Boolean = !current(c).dark && Store.glass(c) >= 2

    /** White glass tint for the card. On dark themes it is much weaker so the card stays dark. */
    fun tint(c: Context, alpha: Int): Int {
        val a = if (current(c).dark) (alpha * 0.4f).toInt() else alpha
        return Color.argb(a, 255, 255, 255)
    }
}
