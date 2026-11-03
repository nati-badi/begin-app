package com.pushme.app

import android.content.Context

/** The app's own labels, in English or Amharic (chosen in Settings). */
object Ui {
    fun isAmharic(c: Context): Boolean = Store.uiLang(c) == 1

    private fun t(c: Context, en: String, am: String): String = if (isAmharic(c)) am else en

    // ---------- main screen ----------

    fun title(c: Context) = t(c, "Begin", "ጀምር")
    fun back(c: Context) = t(c, "Back", "ተመለስ")
    fun next(c: Context) = t(c, "Next", "ቀጣይ")
    fun all(c: Context) = t(c, "All", "ሁሉም")
    fun saved(c: Context) = t(c, "Saved", "የተቀመጡ")
    fun emptySaved(c: Context) = t(
        c,
        "Nothing saved yet. Tap the heart on any message to keep it here.",
        "ገና ምንም የተቀመጠ የለም። ለማስቀመጥ በማንኛውም መልዕክት ላይ ያለውን ልብ ይንኩ።"
    )

    fun kind(c: Context, k: Kind?): String = when (k) {
        null -> all(c)
        Kind.AFFIRM -> t(c, "Affirmation", "ማረጋገጫ")
        Kind.WHY -> t(c, "Why it matters", "ለምን አስፈላጊ ነው")
        Kind.STEP -> t(c, "Tiny step", "ትንሽ እርምጃ")
        Kind.RESTART -> t(c, "Restart", "እንደገና ጀምር")
        Kind.FAITH -> t(c, "Faith", "እምነት")
    }

    fun lang(c: Context, l: Lang): String = when (l) {
        Lang.EN -> t(c, "Eng", "እንግ")
        Lang.AM -> t(c, "Am", "አማ")
        Lang.MIX -> t(c, "Mix", "ቅይጥ")
    }

    /** Small caption above a message: capitals in English, plain in Amharic. */
    fun caption(c: Context, text: String): String {
        val k = Library.kindOf(c, text) ?: return t(c, "BEGIN", "ጀምር")
        return if (isAmharic(c)) {
            when (k) {
                Kind.WHY -> "ለምን አስፈላጊ"
                else -> kind(c, k)
            }
        } else {
            kind(c, k).uppercase()
        }
    }

    fun captionSaved(c: Context): String = if (isAmharic(c)) saved(c) else saved(c).uppercase()

    // ---------- settings ----------

    fun settings(c: Context) = t(c, "Settings", "ቅንብሮች")

    fun secAppearance(c: Context) = t(c, "Appearance", "ገጽታ")
    fun rowColor(c: Context) = t(c, "Color", "ቀለም")
    fun light(c: Context) = t(c, "Light", "ብሩህ")
    fun dark(c: Context) = t(c, "Dark", "ጨለማ")
    fun rowGlass(c: Context) = t(c, "Glass", "መስታወት")
    fun rowLines(c: Context) = t(c, "Lines", "መስመሮች")

    fun glassLevel(c: Context, index: Int): String = when (index) {
        0 -> t(c, "Crystal", "ክሪስታል")
        1 -> t(c, "Clear", "ግልጽ")
        2 -> t(c, "Light", "ቀላል")
        3 -> t(c, "Frosted", "ደብዛዛ")
        else -> t(c, "Milky", "ወተታማ")
    }

    fun lineName(c: Context, index: Int): String = when (index) {
        0 -> off(c)
        1 -> t(c, "Soft", "ለስላሳ")
        else -> t(c, "Clear", "ግልጽ")
    }

    fun secLanguage(c: Context) = t(c, "Language and fonts", "ቋንቋና ቅርጸ ቁምፊ")
    fun rowAppLanguage(c: Context) = t(c, "App language", "የመተግበሪያ ቋንቋ")
    fun rowEnFont(c: Context) = t(c, "English font", "የእንግሊዝኛ ቅርጸ ቁምፊ")
    fun rowAmFont(c: Context) = t(c, "Amharic font", "የአማርኛ ቅርጸ ቁምፊ")

    fun secWidget(c: Context) = t(c, "Widget", "መግብር")
    fun rowWidgetText(c: Context) = t(c, "Text size", "የጽሑፍ መጠን")
    fun widgetTip(c: Context) = t(
        c,
        "Tap the right half for the next message, the left half to go back. Long-press the widget and drag its edges to any size you like.",
        "ቀኝ ግማሽ ለቀጣይ፣ ግራ ለመመለስ ይንኩ። ቀኙን ሁለቴ ሲነኩ የራስዎ መልዕክት ብቻ፣ ግራውን ሁለቴ ሲነኩ የተቀመጡት ብቻ (እንደገና ሲነኩ ይመለሳል)። ተጭነው ጠርዞቹን በመጎተት መጠኑን ይቀይሩ።"
    )

    fun sizeName(c: Context, index: Int): String = when (index) {
        0 -> t(c, "Small", "ትንሽ")
        1 -> t(c, "Medium", "መካከለኛ")
        else -> t(c, "Large", "ትልቅ")
    }

    fun secReminders(c: Context) = t(c, "Reminders", "ማሳሰቢያዎች")
    fun rowTimes(c: Context) = t(c, "Times", "ሰዓቶች")
    fun timesTip(c: Context) = t(c, "Tap a time to remove it.", "ለማስወገድ ሰዓቱን ይንኩ።")
    fun addTime(c: Context) = t(c, "+ Add time", "+ ሰዓት ጨምር")
    fun rowVerse(c: Context) = t(c, "Daily verse", "የዕለቱ ጥቅስ")
    fun verseTip(c: Context) = t(c, "One faith line every morning.", "በየጠዋቱ አንድ የእምነት መልዕክት።")

    fun secFeedback(c: Context) = t(c, "Feedback", "ግብረ መልስ")
    fun rowVibration(c: Context) = t(c, "Vibration", "ንዝረት")
    fun vibrationTip(c: Context) = t(c, "A soft tap when a new message appears.", "አዲስ መልዕክት ሲመጣ ቀላል ንዝረት።")

    fun on(c: Context) = t(c, "On", "በርቷል")
    fun off(c: Context) = t(c, "Off", "ጠፍቷል")

    fun rowTone(c: Context) = t(c, "Time-aware tone", "እንደ ሰዓቱ ቃና")
    fun toneTip(c: Context) = t(
        c,
        "Gentle in the morning, firmer in the afternoon, calmer toward night. Choosing one message type overrides it.",
        "ጠዋት ለስላሳ፣ ከሰዓት ጠንካራ፣ ወደ ማታ ረጋ ያለ። አንድ አይነት ከመረጡ ይሄ አይሠራም።"
    )
    fun previewTip(c: Context) = t(c, "This is your widget. Tap it to try another message.", "ይህ መግብርዎ ነው። ሌላ መልዕክት ለመሞከር ይንኩት።")
    fun secMessages(c: Context) = t(c, "Messages", "መልዕክቶች")
    fun navSaved(c: Context, count: Int) = t(c, "Saved ($count)", "የተቀመጡ ($count)")
    fun navMine(c: Context, count: Int) = t(c, "My messages ($count)", "የራሴ መልዕክቶች ($count)")

    // ---------- saved and my messages ----------

    fun tabSaved(c: Context, count: Int) = t(c, "Saved ($count)", "የተቀመጡ ($count)")
    fun tabMine(c: Context, count: Int) = t(c, "Mine ($count)", "የራሴ ($count)")
    fun libHint(c: Context) = t(c, "Write your own line…", "የራስዎን መልዕክት ይጻፉ…")
    fun libType(c: Context) = t(c, "Type", "አይነት")
    fun add(c: Context) = t(c, "Add", "ጨምር")
    fun save(c: Context) = t(c, "Save", "አስቀምጥ")
    fun emptyMine(c: Context) = t(
        c,
        "Nothing here yet. Write a line above and tap Add.",
        "ገና ምንም የለም። ከላይ መልዕክት ጽፈው «ጨምር» ይንኩ።"
    )
    fun tapToEdit(c: Context) = t(c, "Tap a line to edit it.", "ለማስተካከል መልዕክቱን ይንኩ።")
    fun mine(c: Context) = t(c, "Mine", "የራሴ")
    fun captionMine(c: Context): String = if (isAmharic(c)) mine(c) else "MY MESSAGES"
    fun emptyMineMain(c: Context) = t(
        c,
        "No messages of your own yet. Write some in Settings > Messages.",
        "የራስዎ መልዕክት ገና የለም። በቅንብሮች > መልዕክቶች ውስጥ ይጻፉ።"
    )
    fun rowGesture(c: Context) = t(c, "Gesture", "የንክኪ ምልክት")
    fun gestureTip(c: Context) = t(
        c,
        "Tap the top of the widget to go forward through the list you choose, and the bottom to go back. The middle still goes through everything. Needs a widget at least two rows tall.",
        "የመግብሩን የላይኛው ክፍል ሲነኩ በመረጡት ዝርዝር ወደ ፊት፣ የታችኛውን ሲነኩ ወደ ኋላ ይሄዳል። መሃሉ ሁሉንም መልዕክቶች ያልፋል። ቢያንስ ሁለት ረድፍ ከፍታ ያለው መግብር ያስፈልጋል።"
    )
    fun sourceAll(c: Context) = t(c, "Showing everything", "ሁሉንም በማሳየት ላይ")
    fun sourceSaved(c: Context) = t(c, "Showing only Saved ♥", "የተቀመጡትን ብቻ በማሳየት ላይ ♥")
    fun sourceMine(c: Context) = t(c, "Showing only My messages", "የራስዎን መልዕክት ብቻ በማሳየት ላይ")
    fun nothingSaved(c: Context) = t(c, "Nothing saved yet", "ገና ምንም አልተቀመጠም")
    fun nothingMine(c: Context) = t(c, "You have no messages of your own yet", "የራስዎ መልዕክት ገና የለም")

    fun notifSave(c: Context) = t(c, "♥ Save", "♥ አስቀምጥ")
    fun notifSaved(c: Context) = t(c, "♥ Saved", "♥ ተቀምጧል")
    fun undo(c: Context) = t(c, "Undo", "ሰርዝ")
    fun removedFromSaved(c: Context) = t(c, "Removed from Saved", "ከተቀመጡት ተወግዷል")
    fun messageDeleted(c: Context) = t(c, "Message deleted", "መልዕክቱ ተሰርዟል")
    fun rowSounds(c: Context) = t(c, "Sounds", "ድምፅ")
    fun soundsTip(c: Context) = t(
        c,
        "A very soft click on next, back and save. Needs \"Touch sounds\" to be on in your phone's sound settings.",
        "በቀጣይ፣ በተመለስና በማስቀመጥ ላይ በጣም ለስላሳ ድምፅ። በስልክዎ የድምፅ ቅንብር «የንክኪ ድምፅ» መበራት አለበት።"
    )
    fun rowNightDim(c: Context) = t(c, "Dim at night", "በሌሊት አደብዝዝ")
    fun nightDimTip(c: Context) = t(c, "After 10 PM the widget gets a little darker, so it is not glaring.", "ከምሽቱ 4 ሰዓት በኋላ መግብሩ ትንሽ ይጨልማል።")
    fun rowSkipRecent(c: Context) = t(c, "Skip if I just opened the app", "አሁን መተግበሪያውን ከከፈትኩ ዝለል")
    fun skipRecentTip(c: Context) = t(c, "A reminder waits if you had Begin open in the last hour.", "ባለፈው ሰዓት ውስጥ ከከፈቱ ማሳሰቢያው ይዘገያል።")

    fun rowDrift(c: Context) = t(c, "Drifting glass", "የሚንሳፈፍ መስታወት")
    fun driftTip(c: Context) = t(
        c,
        "Colors float slowly in the app. A widget cannot move live, so its glow shifts a little each time it refreshes.",
        "በመተግበሪያው ውስጥ ቀለማት ቀስ ብለው ይንሳፈፋሉ። መግብር በቀጥታ መንቀሳቀስ ስለማይችል በእያንዳንዱ ጊዜ ሲታደስ ትንሽ ይቀያየራል።"
    )
    fun rowCheckIn(c: Context) = t(c, "Morning check-in", "የጠዋት ጥያቄ")
    fun checkInTip(c: Context) = t(c, "One notification asking what you will begin today.", "ዛሬ ምን እንደሚጀምሩ የሚጠይቅ አንድ ማሳሰቢያ።")
    fun checkInText(c: Context) = t(c, "What will you begin today?", "ዛሬ ምን ይጀምራሉ?")
    fun timerStart(c: Context) = t(c, "▶  2 min start", "▶  ለ2 ደቂቃ ጀምር")
    fun timerRunning(c: Context) = t(c, "Just begin. Two minutes.", "ዝም ብለው ይጀምሩ። ሁለት ደቂቃ።")
    fun timerDone(c: Context) = t(c, "Well done ✓", "ጥሩ ነው ✓")
    fun timerDoneNote(c: Context) = t(
        c,
        "Two minutes are up. Keep going, or stop with a win.",
        "ሁለት ደቂቃ አልቋል። ይቀጥሉ፣ ወይም በድል ያቁሙ።"
    )
    fun rowTimeTint(c: Context) = t(c, "Time of day", "እንደ ሰዓቱ ቀለም")
    fun timeTintTip(c: Context) = t(c, "A soft warm glow in the morning and a cooler tone at night.", "ጠዋት ለስላሳ ሞቅ ያለ፣ ማታ ቀዝቀዝ ያለ ቀለም።")
    fun rowAnimations(c: Context) = t(c, "Animations", "እንቅስቃሴ")
    fun animationsTip(c: Context) = t(c, "Soft motion when messages change and a glow where you touch.", "መልዕክት ሲቀየርና ሲነኩ ለስላሳ እንቅስቃሴ።")

    fun rowAuto(c: Context) = t(c, "Follow phone", "ስልኩን ተከተል")
    fun autoTip(c: Context) = t(
        c,
        "Uses your chosen light color by day and dark color when the phone is in dark mode. Tap a color on each side to choose.",
        "ስልኩ በጨለማ ሁነታ ሲሆን የመረጡትን ጨለማ ቀለም፣ ካልሆነ ብሩህ ቀለም ይጠቀማል።"
    )
    fun rowBig(c: Context) = t(c, "Bigger tap targets", "ትልልቅ አዝራሮች")
    fun bigTip(c: Context) = t(c, "Larger buttons and chips, easier to hit.", "ለመንካት የቀለሉ ትልልቅ አዝራሮች።")
    fun copied(c: Context) = t(c, "Copied", "ተቀድቷል")
    fun share(c: Context) = t(c, "Share", "አጋራ")
    fun shareTitle(c: Context) = t(c, "Share this message", "ይህን መልዕክት አጋራ")
    fun shareFailed(c: Context) = t(c, "Could not share this message", "መልዕክቱን ማጋራት አልተቻለም")

    fun secBackup(c: Context) = t(c, "Backup", "ምትኬ")
    fun rowBackup(c: Context) = t(c, "Save a backup file", "የምትኬ ፋይል አስቀምጥ")
    fun rowRestore(c: Context) = t(c, "Restore from a backup file", "ከምትኬ ፋይል መልስ")
    fun backupTip(c: Context) = t(
        c,
        "Your own messages and saved ones, in one small file you can keep in Drive or send to yourself.",
        "የራስዎ እና የተቀመጡ መልዕክቶች በአንድ ትንሽ ፋይል ውስጥ።"
    )
    fun backupDone(c: Context) = t(c, "Backup saved", "ምትኬ ተቀምጧል")
    fun restoreDone(c: Context, n: Int) = t(c, "Restored $n item(s)", "$n ተመልሷል")
    fun restoreBad(c: Context) = t(c, "That file is not a Begin backup", "ይህ የBegin ምትኬ አይደለም")

    fun tapToShow(c: Context) = t(c, "Tap a message to show it on the card.", "በካርዱ ላይ ለማሳየት መልዕክቱን ይንኩ።")
}
