package com.pushme.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Everything beyond the built-in list: the user's own messages and saved messages,
 * and the one place that picks the next message.
 */
object Library {
    private const val KEY_CUSTOM = "custom_json"
    private const val KEY_FAVS = "favorites"

    /** One clean line: line breaks and double spaces become single spaces, so wrapping is even. */
    fun tidy(text: String): String = text.replace(Regex("\\s+"), " ").trim()

    // ---------- your own messages ----------

    fun customs(c: Context): List<Msg> {
        val raw = Store.prefs(c).getString(KEY_CUSTOM, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val text = o.optString("t")
                if (text.isBlank()) {
                    null
                } else {
                    Msg(
                        runCatching { Kind.valueOf(o.optString("k")) }.getOrDefault(Kind.AFFIRM),
                        runCatching { Lang.valueOf(o.optString("l")) }.getOrDefault(Lang.EN),
                        text
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustoms(c: Context, list: List<Msg>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("t", it.text).put("k", it.kind.name).put("l", it.lang.name))
        }
        Store.prefs(c).edit().putString(KEY_CUSTOM, arr.toString()).apply()
    }

    fun addCustom(c: Context, text: String, kind: Kind) {
        val clean = tidy(text)
        if (clean.isEmpty()) return
        saveCustoms(c, customs(c) + Msg(kind, detectLang(clean), clean))
    }

    fun updateCustom(c: Context, index: Int, text: String, kind: Kind) {
        val clean = tidy(text)
        val list = customs(c).toMutableList()
        if (clean.isEmpty() || index !in list.indices) return
        list[index] = Msg(kind, detectLang(clean), clean)
        saveCustoms(c, list)
    }

    fun deleteCustom(c: Context, index: Int) {
        val list = customs(c).toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        saveCustoms(c, list)
    }

    /** Puts a deleted message back where it was (used by "Undo"). */
    fun restoreCustom(c: Context, index: Int, msg: Msg) {
        val list = customs(c).toMutableList()
        list.add(index.coerceIn(0, list.size), msg)
        saveCustoms(c, list)
    }

    /** Amharic letters only = AM, Latin letters only = EN, both = MIX. */
    fun detectLang(text: String): Lang {
        val hasAmharic = text.any { it.code in 0x1200..0x139F }
        val hasLatin = text.any { it in 'A'..'Z' || it in 'a'..'z' }
        return when {
            hasAmharic && hasLatin -> Lang.MIX
            hasAmharic -> Lang.AM
            else -> Lang.EN
        }
    }

    // ---------- saved ----------

    fun savedTexts(c: Context): Set<String> =
        Store.prefs(c).getStringSet(KEY_FAVS, emptySet())?.toSet() ?: emptySet()

    fun isSaved(c: Context, text: String): Boolean = text in savedTexts(c)

    fun toggleSaved(c: Context, text: String) {
        val set = savedTexts(c).toMutableSet()
        if (!set.add(text)) set.remove(text)
        Store.prefs(c).edit().putStringSet(KEY_FAVS, set).apply()
    }

    // ---------- backup ----------

    /** Your own messages and saved messages as one small JSON text, for a backup file. */
    fun exportJson(c: Context): String {
        val own = JSONArray()
        customs(c).forEach { own.put(JSONObject().put("t", it.text).put("k", it.kind.name)) }
        val saved = JSONArray()
        savedTexts(c).forEach { saved.put(it) }
        return JSONObject().put("app", "Begin").put("version", 1).put("own", own).put("saved", saved).toString(2)
    }

    /** Adds what a backup holds (nothing is removed or duplicated). Returns how many were added, or -1 if the file is not a backup. */
    fun importJson(c: Context, raw: String): Int {
        return try {
            val root = JSONObject(raw)
            if (root.optString("app") != "Begin") return -1
            var added = 0

            val have = customs(c).map { it.text }.toMutableSet()
            val list = customs(c).toMutableList()
            val own = root.optJSONArray("own") ?: JSONArray()
            for (i in 0 until own.length()) {
                val o = own.optJSONObject(i) ?: continue
                val text = tidy(o.optString("t"))
                if (text.isEmpty() || !have.add(text)) continue
                val kind = runCatching { Kind.valueOf(o.optString("k")) }.getOrDefault(Kind.AFFIRM)
                list.add(Msg(kind, detectLang(text), text))
                added++
            }
            if (added > 0) saveCustoms(c, list)

            val savedNow = savedTexts(c).toMutableSet()
            val saved = root.optJSONArray("saved") ?: JSONArray()
            var savedAdded = 0
            for (i in 0 until saved.length()) {
                val text = saved.optString(i)
                if (text.isNotEmpty() && savedNow.add(text)) savedAdded++
            }
            if (savedAdded > 0) Store.prefs(c).edit().putStringSet(KEY_FAVS, savedNow).apply()
            added + savedAdded
        } catch (e: Exception) {
            -1
        }
    }

    // ---------- picking ----------

    fun pool(c: Context): List<Msg> = Messages.all + customs(c)

    fun kindOf(c: Context, text: String): Kind? = pool(c).firstOrNull { it.text == text }?.kind

    /** Every message you saved with the heart (in the order of the full list). */
    fun savedMessages(c: Context): List<Msg> {
        val favs = savedTexts(c)
        return pool(c).filter { it.text in favs }
    }

    /**
     * Picks a random message that fits the filters, never the same as [avoid].
     * Your own messages show up twice as often, saved messages three times as often.
     * In "Saved" mode only saved messages are used (in your chosen language if there are
     * any, otherwise in any language).
     */
    fun pick(c: Context, kind: Kind?, lang: Lang?, avoid: String?, useSavedFilter: Boolean = true): String {
        val favs = savedTexts(c)
        val own = customs(c).map { it.text }.toSet()

        // A line you wrote that has both English and Amharic letters fits either language.
        val langOk = { m: Msg -> lang == null || m.lang == lang || m.lang == Lang.MIX }
        var pool = pool(c).filter { (kind == null || it.kind == kind) && langOk(it) }
        if (useSavedFilter && Store.source(c) == 1) {
            val saved = savedMessages(c)
            val sameLanguage = saved.filter(langOk)
            pool = if (sameLanguage.isNotEmpty()) sameLanguage else saved
        }
        if (useSavedFilter && Store.source(c) == 2) {
            val mine = customs(c)
            val sameLanguage = mine.filter(langOk)
            pool = if (sameLanguage.isNotEmpty()) sameLanguage else mine
        }
        if (pool.size > 1) pool = pool.filter { it.text != avoid }
        if (pool.isEmpty()) pool = Messages.all

        // Time-aware tone only when no single type is chosen.
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val useTone = kind == null && Store.timeTone(c)

        val bag = ArrayList<Msg>()
        pool.forEach { m ->
            val weight = when {
                m.text in favs -> 3
                m.text in own -> 2
                else -> 1
            }
            val tone = if (useTone) toneWeight(m.kind, hour) else 1
            repeat(weight * tone) { bag.add(m) }
        }
        if (bag.isEmpty()) pool.forEach { bag.add(it) }
        return bag.random().text
    }

    /**
     * How much each type is favored at this hour. Morning: warm and gentle. Afternoon: firm
     * (the "why" and "restart" pushes). Evening: easing off. Night: calm only.
     */
    fun toneWeight(kind: Kind, hour: Int): Int = when (hour) {
        in 5..11 -> when (kind) {
            Kind.AFFIRM, Kind.FAITH -> 3
            Kind.STEP -> 2
            Kind.WHY, Kind.RESTART -> 1
        }
        in 12..17 -> when (kind) {
            Kind.WHY, Kind.RESTART, Kind.STEP -> 3
            Kind.AFFIRM, Kind.FAITH -> 1
        }
        in 18..21 -> when (kind) {
            Kind.AFFIRM, Kind.FAITH, Kind.STEP -> 2
            Kind.WHY, Kind.RESTART -> 1
        }
        else -> when (kind) {
            Kind.FAITH -> 4
            Kind.AFFIRM -> 3
            Kind.STEP -> 1
            Kind.WHY, Kind.RESTART -> 0
        }
    }
}
