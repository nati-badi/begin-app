package com.pushme.app

import android.content.Context
import android.provider.Settings
import org.json.JSONArray

/** Remembers the chosen filters, look, fonts, reminders and the message history. */
object Store {
    private const val FILE = "begin"
    private const val KEY_KIND = "kind"
    private const val KEY_LANG = "lang"
    private const val KEY_CURRENT = "current"
    private const val KEY_HISTORY = "history"
    private const val KEY_POS = "hist_pos"
    private const val KEY_THEME = "theme"
    private const val KEY_GLASS = "glass"
    private const val KEY_LINES = "lines"
    private const val KEY_EN_FONT = "en_font"
    private const val KEY_AM_FONT = "am_font"
    private const val KEY_UI_LANG = "ui_lang"
    private const val KEY_WIDGET_TEXT = "widget_text"
    private const val KEY_SAVED_ONLY = "fav_only"
    private const val KEY_HAPTICS = "haptics"
    private const val KEY_TIMES = "reminder_times"
    private const val KEY_VERSE_ON = "verse_on"
    private const val KEY_VERSE_HOUR = "verse_hour"
    private const val KEY_VERSE_MINUTE = "verse_minute"

    /** How many past messages the back tap can walk through. */
    private const val HISTORY_MAX = 60

    fun prefs(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ---------- look ----------

    /** 0 (most see-through) to 4 (most frosted). */
    fun glass(c: Context): Int = prefs(c).getInt(KEY_GLASS, 1)

    fun setGlass(c: Context, level: Int) {
        prefs(c).edit().putInt(KEY_GLASS, level).apply()
    }

    fun themeIndex(c: Context): Int = prefs(c).getInt(KEY_THEME, 0)

    fun setThemeIndex(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_THEME, i).apply()
    }

    /** Border lines: 0 = off, 1 = soft, 2 = clear. */
    fun lines(c: Context): Int = prefs(c).getInt(KEY_LINES, 1)

    fun setLines(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_LINES, i).apply()
    }

    /** Index into Fonts.english (default Inter). */
    fun enFont(c: Context): Int = prefs(c).getInt(KEY_EN_FONT, 1)

    fun setEnFont(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_EN_FONT, i).apply()
    }

    /** Index into Fonts.amharic (default Noto Sans). */
    fun amFont(c: Context): Int = prefs(c).getInt(KEY_AM_FONT, 1)

    fun setAmFont(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_AM_FONT, i).apply()
    }

    /** Language of the app's own labels: 0 = English, 1 = Amharic. */
    fun uiLang(c: Context): Int = prefs(c).getInt(KEY_UI_LANG, 0)

    fun setUiLang(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_UI_LANG, i).apply()
    }

    /** Widget text size: 0 = small, 1 = medium, 2 = large. */
    fun widgetText(c: Context): Int = prefs(c).getInt(KEY_WIDGET_TEXT, 1)

    fun setWidgetText(c: Context, i: Int) {
        prefs(c).edit().putInt(KEY_WIDGET_TEXT, i).apply()
    }

    // ---------- feedback and reminders ----------

    /** Show only messages you saved (hearted). */
    fun savedOnly(c: Context): Boolean = prefs(c).getBoolean(KEY_SAVED_ONLY, false)

    fun setSavedOnly(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean(KEY_SAVED_ONLY, on).apply()
    }

    /** Where messages come from: 0 = everything, 1 = only saved, 2 = only my own. */
    fun source(c: Context): Int = when {
        savedOnly(c) -> 1
        prefs(c).getBoolean("mine_only", false) -> 2
        else -> 0
    }

    fun setSource(c: Context, source: Int) {
        prefs(c).edit()
            .putBoolean(KEY_SAVED_ONLY, source == 1)
            .putBoolean("mine_only", source == 2)
            .apply()
    }

    /**
     * Soft motion: messages slide in, things pop and glow when touched. Until you choose,
     * it follows your phone: if the phone's animations are switched off, so are ours.
     */
    fun animations(c: Context): Boolean {
        val p = prefs(c)
        return if (p.contains("animations")) p.getBoolean("animations", true) else phoneAllowsMotion(c)
    }

    private fun phoneAllowsMotion(c: Context): Boolean = try {
        Settings.Global.getFloat(c.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    } catch (e: Exception) {
        true
    }

    fun setAnimations(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("animations", on).apply()
    }

    /** The widget's up/down gesture: 0 = off, 1 = step through Saved, 2 = step through My messages. */
    fun gestureMode(c: Context): Int = prefs(c).getInt("double_tap_mode", 1)

    fun setGestureMode(c: Context, mode: Int) {
        prefs(c).edit().putInt("double_tap_mode", mode).apply()
    }

    /** Where the walk through a list stopped (so it carries on from there). */
    fun gesturePos(c: Context, mode: Int): Int = prefs(c).getInt("double_tap_pos_$mode", -1)

    fun setGesturePos(c: Context, mode: Int, pos: Int) {
        prefs(c).edit().putInt("double_tap_pos_$mode", pos).apply()
    }

    /** Follow the phone's dark mode: a chosen light color by day, a chosen dark color at night. */
    fun autoTheme(c: Context): Boolean = prefs(c).getBoolean("auto_theme", false)

    fun setAutoTheme(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("auto_theme", on).apply()
    }

    fun lightIndex(c: Context): Int =
        prefs(c).getInt("light_theme", Themes.all.indexOfFirst { !it.dark }.coerceAtLeast(0))

    fun darkIndex(c: Context): Int =
        prefs(c).getInt("dark_theme", Themes.all.indexOfFirst { it.dark }.coerceAtLeast(0))

    fun setLightIndex(c: Context, i: Int) {
        prefs(c).edit().putInt("light_theme", i).apply()
    }

    fun setDarkIndex(c: Context, i: Int) {
        prefs(c).edit().putInt("dark_theme", i).apply()
    }

    /** Bigger buttons and chips for easier tapping. */
    fun bigTargets(c: Context): Boolean = prefs(c).getBoolean("big_targets", false)

    fun setBigTargets(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("big_targets", on).apply()
    }

    /** Soft click sound on next, back and save. Off until you switch it on. */
    fun sounds(c: Context): Boolean = prefs(c).getBoolean("sounds", false)

    fun setSounds(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("sounds", on).apply()
    }

    /** After 10 PM the widget gets a little darker so it is not glaring. */
    fun nightDim(c: Context): Boolean = prefs(c).getBoolean("night_dim", true)

    fun setNightDim(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("night_dim", on).apply()
    }

    /** A reminder waits if you had the app open in the last hour. */
    fun skipRecent(c: Context): Boolean = prefs(c).getBoolean("skip_recent", true)

    fun setSkipRecent(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("skip_recent", on).apply()
    }

    /** When the app was last on screen (milliseconds since 1970). */
    fun lastOpen(c: Context): Long = prefs(c).getLong("last_open", 0L)

    fun markOpen(c: Context) {
        prefs(c).edit().putLong("last_open", System.currentTimeMillis()).apply()
    }

    /** Drifting glass: colors float slowly in the app and shift a little each time the widget refreshes. */
    fun drift(c: Context): Boolean = prefs(c).getBoolean("drift", true)

    fun setDrift(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("drift", on).apply()
    }

    /** When the running 2-minute timer ends (milliseconds since 1970), or 0 if none is running. */
    fun timerEnd(c: Context): Long = prefs(c).getLong("timer_end", 0L)

    fun setTimerEnd(c: Context, time: Long) {
        prefs(c).edit().putLong("timer_end", time).apply()
    }

    /** Optional morning check-in notification: "What will you begin today?" */
    fun checkInOn(c: Context): Boolean = prefs(c).getBoolean("checkin_on", false)
    fun checkInHour(c: Context): Int = prefs(c).getInt("checkin_hour", 8)
    fun checkInMinute(c: Context): Int = prefs(c).getInt("checkin_minute", 0)

    fun setCheckIn(c: Context, on: Boolean, hour: Int, minute: Int) {
        prefs(c).edit()
            .putBoolean("checkin_on", on)
            .putInt("checkin_hour", hour)
            .putInt("checkin_minute", minute)
            .apply()
    }

    /** A soft warm tint in the morning and a cooler one at night, on top of the chosen theme. */
    fun timeTint(c: Context): Boolean = prefs(c).getBoolean("time_tint", true)

    fun setTimeTint(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("time_tint", on).apply()
    }

    /** Gentle in the morning, firmer in the afternoon, calmer toward night. */
    fun timeTone(c: Context): Boolean = prefs(c).getBoolean("time_tone", true)

    fun setTimeTone(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("time_tone", on).apply()
    }

    /** Soft vibration when a new message appears. */
    fun haptics(c: Context): Boolean = prefs(c).getBoolean(KEY_HAPTICS, true)

    fun setHaptics(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean(KEY_HAPTICS, on).apply()
    }

    /** Reminder times as (hour, minute) pairs, earliest first. */
    fun reminderTimes(c: Context): List<Pair<Int, Int>> =
        (prefs(c).getStringSet(KEY_TIMES, emptySet()) ?: emptySet())
            .mapNotNull { entry ->
                val parts = entry.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull()
                val m = parts.getOrNull(1)?.toIntOrNull()
                if (h != null && m != null) Pair(h, m) else null
            }
            .sortedBy { it.first * 60 + it.second }

    fun setReminderTimes(c: Context, times: List<Pair<Int, Int>>) {
        prefs(c).edit().putStringSet(KEY_TIMES, times.map { "${it.first}:${it.second}" }.toSet()).apply()
    }

    /** Optional morning verse (a faith line). */
    fun verseOn(c: Context): Boolean = prefs(c).getBoolean(KEY_VERSE_ON, false)
    fun verseHour(c: Context): Int = prefs(c).getInt(KEY_VERSE_HOUR, 7)
    fun verseMinute(c: Context): Int = prefs(c).getInt(KEY_VERSE_MINUTE, 0)

    fun setVerse(c: Context, on: Boolean, hour: Int, minute: Int) {
        prefs(c).edit()
            .putBoolean(KEY_VERSE_ON, on)
            .putInt(KEY_VERSE_HOUR, hour)
            .putInt(KEY_VERSE_MINUTE, minute)
            .apply()
    }

    // ---------- filters ----------

    /** null means "all". */
    fun kind(c: Context): Kind? =
        prefs(c).getString(KEY_KIND, null)?.let { name -> Kind.values().firstOrNull { it.name == name } }

    /** Language filter: English, Amharic, or null for both. (The old "Mix" choice was removed.) */
    fun lang(c: Context): Lang? =
        prefs(c).getString(KEY_LANG, null)
            ?.let { name -> Lang.values().firstOrNull { it.name == name } }
            ?.takeIf { it != Lang.MIX }

    fun setKind(c: Context, k: Kind?) {
        prefs(c).edit().putString(KEY_KIND, k?.name).apply()
    }

    fun setLang(c: Context, l: Lang?) {
        prefs(c).edit().putString(KEY_LANG, l?.name).apply()
    }

    // ---------- the message on screen, and the ones before it ----------
    //
    // The history is a short list of the messages shown so far, and a position in it.
    // "Back" moves the position back, "next" moves forward (or picks a new message
    // when you are already at the newest one).

    private fun history(c: Context): MutableList<String> {
        val raw = prefs(c).getString(KEY_HISTORY, null) ?: return mutableListOf()
        return try {
            val array = JSONArray(raw)
            MutableList(array.length()) { array.getString(it) }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun position(c: Context, size: Int): Int =
        prefs(c).getInt(KEY_POS, size - 1).coerceIn(0, maxOf(size - 1, 0))

    private fun saveHistory(c: Context, list: List<String>, pos: Int) {
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs(c).edit()
            .putString(KEY_HISTORY, array.toString())
            .putInt(KEY_POS, pos)
            .putString(KEY_CURRENT, list.getOrNull(pos))
            .apply()
    }

    /** The message showing right now, or null if none has been shown yet. */
    fun peekCurrent(c: Context): String? {
        val list = history(c)
        if (list.isNotEmpty()) return list[position(c, list.size)]
        return prefs(c).getString(KEY_CURRENT, null)
    }

    /** The message showing right now (picks one if there is none yet). */
    fun current(c: Context): String {
        val list = history(c)
        if (list.isNotEmpty()) return list[position(c, list.size)]
        val older = prefs(c).getString(KEY_CURRENT, null)
        if (older != null) {
            saveHistory(c, listOf(older), 0)
            return older
        }
        return pickNew(c)
    }

    /** Puts a message on screen as the newest one, dropping anything "ahead" of the current spot. */
    fun show(c: Context, text: String): String {
        val list = history(c)
        val pos = position(c, list.size)
        while (list.size > pos + 1) list.removeAt(list.size - 1)
        if (list.lastOrNull() != text) list.add(text)
        while (list.size > HISTORY_MAX) list.removeAt(0)
        saveHistory(c, list, list.size - 1)
        return text
    }

    /**
     * Forgets the messages shown so far. Called when a filter changes (type, language, saved, mine),
     * so "Back" never walks into a message that does not fit the filter you just chose.
     */
    fun startOver(c: Context) {
        prefs(c).edit().remove(KEY_HISTORY).remove(KEY_POS).apply()
    }

    /** Picks a brand new message that fits the filters and shows it. */
    fun pickNew(c: Context): String {
        val list = history(c)
        val previous = if (list.isEmpty()) null else list[position(c, list.size)]
        return show(c, Library.pick(c, kind(c), lang(c), previous))
    }

    /** Forward: the next message in the history, or a new one if you are at the newest. */
    fun next(c: Context): String {
        val list = history(c)
        val pos = position(c, list.size)
        if (list.isNotEmpty() && pos < list.size - 1) {
            saveHistory(c, list, pos + 1)
            return list[pos + 1]
        }
        return pickNew(c)
    }

    /** Back: the message before this one, or null if there is none. */
    fun previous(c: Context): String? {
        val list = history(c)
        val pos = position(c, list.size)
        if (list.isEmpty() || pos <= 0) return null
        saveHistory(c, list, pos - 1)
        return list[pos - 1]
    }
}
