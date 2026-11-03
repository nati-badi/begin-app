package com.pushme.app

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.core.content.res.ResourcesCompat

/** A font the user can pick. resId 0 = the phone's own font. */
data class FontOption(val label: String, val resId: Int)

object Fonts {
    // New fonts are added at the END, so a font you already chose keeps its place.
    val english: List<FontOption> = listOf(
        FontOption("System", 0),
        FontOption("Inter", R.font.en_inter),
        FontOption("Poppins", R.font.en_poppins),
        FontOption("DM Sans", R.font.en_dmsans),
        FontOption("Nunito", R.font.en_nunito),
        FontOption("Playfair", R.font.en_playfair),
        FontOption("Lora", R.font.en_lora),
        FontOption("Carlito", R.font.en_carlito),
        FontOption("Manrope", R.font.en_manrope),
        FontOption("Fraunces", R.font.en_fraunces),
        FontOption("Grotesk", R.font.en_spacegrotesk)
    )

    val amharic: List<FontOption> = listOf(
        FontOption("System", 0),
        FontOption("Noto Sans", R.font.am_noto_sans),
        FontOption("Noto Serif", R.font.am_noto_serif),
        FontOption("Abyssinica", R.font.am_abyssinica),
        FontOption("Lessan", R.font.am_lessan),
        FontOption("Sadiss", R.font.am_sadiss),
        FontOption("Abay", R.font.am_abay),
        FontOption("Sans Light", R.font.am_noto_sans_light),
        FontOption("Serif Light", R.font.am_noto_serif_light),
        FontOption("Sans Black", R.font.am_noto_sans_black),
        FontOption("FreeSerif", R.font.am_freeserif)
    )

    private val cache = HashMap<Int, Typeface>()

    fun load(c: Context, option: FontOption): Typeface {
        if (option.resId == 0) return Typeface.DEFAULT_BOLD
        return cache.getOrPut(option.resId) {
            ResourcesCompat.getFont(c, option.resId) ?: Typeface.DEFAULT_BOLD
        }
    }

    fun englishFace(c: Context): Typeface =
        load(c, english[Store.enFont(c).coerceIn(0, english.lastIndex)])

    fun amharicFace(c: Context): Typeface =
        load(c, amharic[Store.amFont(c).coerceIn(0, amharic.lastIndex)])

    private fun isEthiopic(ch: Char): Boolean {
        val code = ch.code
        return code in 0x1200..0x139F || code in 0x2D80..0x2DDF || code in 0xAB00..0xAB2F
    }

    /**
     * Gives Amharic letters the chosen Amharic font and everything else the chosen
     * English font. Spaces and numbers follow the letters before them.
     */
    fun styled(c: Context, text: CharSequence): SpannableString {
        val out = SpannableString(text)
        val en = englishFace(c)
        val am = amharicFace(c)
        val n = text.length
        var i = 0
        var amharicRun = false
        while (i < n) {
            val start = i
            amharicRun = if (isEthiopic(text[i])) true
            else if (text[i].isWhitespace() || text[i].isDigit()) amharicRun
            else false
            i++
            while (i < n) {
                val ch = text[i]
                val sameRun = if (isEthiopic(ch)) amharicRun
                else if (ch.isWhitespace() || ch.isDigit()) true
                else !amharicRun
                if (!sameRun) break
                i++
            }
            out.setSpan(FontSpan(if (amharicRun) am else en), start, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return out
    }
}

/** Changes the font of one piece of text. */
class FontSpan(private val face: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = face
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = face
    }
}
