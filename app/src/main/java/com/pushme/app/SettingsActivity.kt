package com.pushme.app

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Settings: one scrolling page with grouped sections (Appearance, Language and fonts, Widget,
 * Reminders, Feedback, Messages). The page is rebuilt whenever a choice changes, so what you
 * see always matches what you picked.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var look: Look
    private lateinit var backdrop: BackdropView
    private lateinit var scroll: ScrollView
    private lateinit var content: LinearLayout

    private var entered = false

    private val exportBackup =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) writeBackup(uri)
        }

    private val importBackup =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) readBackup(uri)
        }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        look = Look(this)

        val root = FrameLayout(this)
        backdrop = BackdropView(this)
        backdrop.setPlain(true)
        root.addView(backdrop, FrameLayout.LayoutParams(MATCH, MATCH))

        scroll = ScrollView(this)
        scroll.isVerticalScrollBarEnabled = false
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        scroll.addView(content, FrameLayout.LayoutParams(MATCH, WRAP))
        root.addView(scroll, FrameLayout.LayoutParams(MATCH, MATCH))

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val keepScroll = scroll.scrollY
        look = Look(this)
        look.applyWindow(this)
        backdrop.setArt(look.theme)

        content.removeAllViews()
        content.setPadding(look.dp(22), look.dp(14), look.dp(22), look.dp(36))

        buildHeader()
        buildAppearance()
        buildLanguage()
        buildWidget()
        buildReminders()
        buildFeedback()
        buildMessages()
        buildBackup()

        if (!entered) {
            // Sections fade in one after another the first time the page opens.
            entered = true
            Motion.enter(this, (0 until content.childCount).map { content.getChildAt(it) }, step = 40)
        }

        scroll.post { scroll.scrollTo(0, keepScroll) }
    }

    // ---------- building blocks ----------

    private fun buildHeader() {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL

        val back = ImageView(this)
        back.setImageResource(R.drawable.ic_back)
        back.contentDescription = "Back"
        back.setPadding(look.dp(10), look.dp(10), look.dp(10), look.dp(10))
        look.styleIconButton(back)
        back.setOnClickListener { finish() }
        row.addView(back, LinearLayout.LayoutParams(look.dp(look.iconDp), look.dp(look.iconDp)))

        val heading = TextView(this)
        heading.text = Fonts.styled(this, Ui.settings(this))
        heading.textSize = 26f
        heading.setTextColor(look.ink)
        row.addView(heading, look.lp(WRAP, WRAP, start = 14))

        content.addView(row, look.lp())
    }

    /** Adds a small section title and returns the empty panel below it. */
    private fun section(title: String): LinearLayout {
        val label = TextView(this)
        val amharic = Ui.isAmharic(this)
        label.text = if (amharic) title else title.uppercase()
        label.textSize = 12f
        label.setTypeface(label.typeface, Typeface.BOLD)
        label.letterSpacing = if (amharic) 0f else 0.1f
        label.setTextColor(look.muted)
        label.setPadding(look.dp(6), 0, 0, 0)
        content.addView(label, look.lp(top = 26, bottom = 8))

        val panel = look.panel()
        content.addView(panel, look.lp())
        return panel
    }

    /** One row in a panel: a title (and optional tip), then a control underneath. */
    private fun addRow(
        panel: LinearLayout,
        title: String,
        tip: String? = null,
        control: View? = null,
        trailing: View? = null
    ) {
        if (panel.childCount > 0) panel.addView(look.divider(), look.dividerParams())

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val vertical = if (look.big) 18 else 14
        box.setPadding(look.dp(16), look.dp(vertical), look.dp(16), look.dp(vertical))

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val name = TextView(this)
        name.text = title
        name.textSize = 15f
        name.setTypeface(name.typeface, Typeface.BOLD)
        name.setTextColor(look.ink)
        head.addView(name, LinearLayout.LayoutParams(0, WRAP, 1f))
        if (trailing != null) head.addView(trailing)
        box.addView(head)

        if (tip != null) {
            val tipView = TextView(this)
            tipView.text = tip
            tipView.textSize = 13f
            tipView.setTextColor(look.muted)
            tipView.setPadding(0, look.dp(3), 0, 0)
            box.addView(tipView)
        }
        if (control != null) box.addView(control, look.lp(top = 12))

        panel.addView(box, look.lp())
    }

    /** A row that opens another screen. */
    private fun addNav(panel: LinearLayout, title: String, onClick: () -> Unit) {
        if (panel.childCount > 0) panel.addView(look.divider(), look.dividerParams())

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(look.dp(16), look.dp(16), look.dp(12), look.dp(16))
        row.setOnClickListener { onClick() }

        val name = TextView(this)
        name.text = title
        name.textSize = 15f
        name.setTypeface(name.typeface, Typeface.BOLD)
        name.setTextColor(look.ink)
        row.addView(name, LinearLayout.LayoutParams(0, WRAP, 1f))

        val chevron = ImageView(this)
        chevron.setImageResource(R.drawable.ic_chevron_right)
        chevron.imageTintList = ColorStateList.valueOf(look.muted)
        row.addView(chevron, LinearLayout.LayoutParams(look.dp(22), look.dp(22)))

        panel.addView(row, look.lp())
    }

    private fun smallLabel(text: String): TextView {
        val label = TextView(this)
        label.text = text
        label.textSize = 12f
        label.setTextColor(look.muted)
        return label
    }

    // ---------- Appearance ----------

    private fun buildAppearance() {
        val panel = section(Ui.secAppearance(this))

        addRow(
            panel, Ui.rowAuto(this), Ui.autoTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.autoTheme(this)) 1 else 0) {
                if (it == 1 && !Store.autoTheme(this)) {
                    // Start from what you are using now: it becomes the light or the dark side.
                    val now = Store.themeIndex(this)
                    if (Themes.all[now.coerceIn(0, Themes.all.lastIndex)].dark) Store.setDarkIndex(this, now)
                    else Store.setLightIndex(this, now)
                }
                Motion.crossfade(this) {
                    Store.setAutoTheme(this, it == 1)
                    PushWidget.refreshAll(this)
                    render()
                }
            }
        )

        val colors = LinearLayout(this)
        colors.orientation = LinearLayout.VERTICAL
        colors.addView(smallLabel(Ui.light(this)))
        colors.addView(dotsRow(Themes.all.indices.filter { !Themes.all[it].dark }), look.lp(top = 8, bottom = 14))
        colors.addView(smallLabel(Ui.dark(this)))
        colors.addView(dotsRow(Themes.all.indices.filter { Themes.all[it].dark }), look.lp(top = 8))
        addRow(panel, Ui.rowColor(this), null, colors)

        val glassName = TextView(this)
        glassName.text = Ui.glassLevel(this, Store.glass(this))
        glassName.textSize = 13f
        glassName.setTypeface(glassName.typeface, Typeface.BOLD)
        glassName.setTextColor(look.ink)

        val seek = SeekBar(this)
        seek.max = 4
        seek.progress = Store.glass(this)
        seek.progressTintList = ColorStateList.valueOf(look.ink)
        seek.thumbTintList = ColorStateList.valueOf(look.ink)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Store.setGlass(this@SettingsActivity, progress)
                    glassName.text = Ui.glassLevel(this@SettingsActivity, progress)
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {}

            override fun onStopTrackingTouch(bar: SeekBar?) {
                PushWidget.refreshAll(this@SettingsActivity)
            }
        })
        addRow(panel, Ui.rowGlass(this), null, seek, glassName)

        val lineNames = listOf(Ui.lineName(this, 0), Ui.lineName(this, 1), Ui.lineName(this, 2))
        addRow(
            panel, Ui.rowLines(this), null,
            look.chipsRow(lineNames, Store.lines(this)) {
                Store.setLines(this, it)
                render()
            }
        )
        addRow(
            panel, Ui.rowTimeTint(this), Ui.timeTintTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.timeTint(this)) 1 else 0) {
                Store.setTimeTint(this, it == 1)
                PushWidget.refreshAll(this)
                Motion.crossfade(this) { render() }
            }
        )
    }

    private fun dotsRow(indices: List<Int>): View {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val auto = Store.autoTheme(this)
        indices.forEach { index ->
            val theme = Themes.all[index]
            val dot = View(this)
            dot.contentDescription = theme.name
            val shape = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(if (theme.dark) theme.bgStart else theme.start, if (theme.dark) theme.start else theme.end)
            )
            shape.shape = GradientDrawable.OVAL
            val chosen = when {
                !auto -> index == Store.themeIndex(this)
                theme.dark -> index == Store.darkIndex(this)
                else -> index == Store.lightIndex(this)
            }
            if (chosen) {
                shape.setStroke(look.dp(3), look.ink)
            } else if (look.hairline != android.graphics.Color.TRANSPARENT) {
                shape.setStroke(look.dp(1), look.hairline)
            }
            dot.background = shape
            dot.setOnClickListener {
                Motion.crossfade(this) {
                    if (auto) {
                        if (theme.dark) Store.setDarkIndex(this, index) else Store.setLightIndex(this, index)
                    } else {
                        Store.setThemeIndex(this, index)
                    }
                    PushWidget.refreshAll(this)
                    render()
                }
            }
            row.addView(dot, look.lp(look.dp(look.iconDp), look.dp(look.iconDp), end = 14))
        }
        return row
    }

    // ---------- Language and fonts ----------

    private fun buildLanguage() {
        val panel = section(Ui.secLanguage(this))

        addRow(
            panel, Ui.rowAppLanguage(this), null,
            look.chipsRow(listOf("English", "አማርኛ"), Store.uiLang(this)) {
                Store.setUiLang(this, it)
                PushWidget.refreshAll(this)
                render()
            }
        )
        addRow(
            panel, Ui.rowEnFont(this), null,
            fontRow(Fonts.english, "Aa", Store.enFont(this)) { Store.setEnFont(this, it) }
        )
        addRow(
            panel, Ui.rowAmFont(this), null,
            fontRow(Fonts.amharic, "አማ", Store.amFont(this)) { Store.setAmFont(this, it) }
        )
    }

    /** Chips that show a sample in each font, with the font's name underneath. */
    private fun fontRow(
        options: List<FontOption>,
        sample: String,
        selected: Int,
        onPick: (Int) -> Unit
    ): View {
        val labels = options.map { option ->
            val face = Fonts.load(this, option)
            val text = SpannableStringBuilder()
            text.append(sample)
            text.setSpan(FontSpan(face), 0, sample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(AbsoluteSizeSpan(22, true), 0, sample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.append("\n")
            val nameStart = text.length
            text.append(option.label)
            text.setSpan(AbsoluteSizeSpan(11, true), nameStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text as CharSequence
        }
        val chips = mutableListOf<TextView>()
        val row = look.chipsRow(labels, selected, radiusDp = 16, out = chips) {
            onPick(it)
            PushWidget.refreshAll(this)
            render()
        }
        chips.forEach { it.minWidth = look.dp(78) }
        return row
    }

    // ---------- Widget ----------

    private fun buildWidget() {
        val panel = section(Ui.secWidget(this))
        val sizes = listOf(Ui.sizeName(this, 0), Ui.sizeName(this, 1), Ui.sizeName(this, 2))

        val preview = ImageView(this)
        val chips = mutableListOf<TextView>()
        val chipRow = look.chipsRow(sizes, Store.widgetText(this), out = chips) { picked ->
            Store.setWidgetText(this, picked)
            chips.forEachIndexed { i, chip -> look.styleChip(chip, i == picked) }
            PushWidget.refreshAll(this)
            drawPreview(preview)
        }

        // Chips and the picture of the widget live in one row, so you see the change at once.
        val control = LinearLayout(this)
        control.orientation = LinearLayout.VERTICAL
        control.addView(chipRow, look.lp())
        control.addView(previewFrame(preview), look.lp(top = 14))
        addRow(panel, Ui.rowWidgetText(this), Ui.widgetTip(this), control)
        addRow(
            panel, Ui.rowGesture(this), Ui.gestureTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.saved(this), Ui.mine(this)), Store.gestureMode(this).coerceIn(0, 2)) {
                Store.setGestureMode(this, it)
                Haptics.tick(this)
                render()
            }
        )
        addRow(
            panel, Ui.rowNightDim(this), Ui.nightDimTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.nightDim(this)) 1 else 0) {
                Store.setNightDim(this, it == 1)
                PushWidget.refreshAll(this)
                render()
            }
        )
    }

    /** A dark, wallpaper-like frame, so the see-through glass of the widget shows as it will on your home screen. */
    private fun previewFrame(preview: ImageView): View {
        val frame = FrameLayout(this)
        frame.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFF1B2433.toInt(), 0xFF3B4C6B.toInt())
        ).apply { cornerRadius = look.dp(18).toFloat() }
        frame.setPadding(look.dp(12), look.dp(12), look.dp(12), look.dp(12))

        preview.scaleType = ImageView.ScaleType.FIT_CENTER
        preview.adjustViewBounds = true
        preview.setOnClickListener {
            // Tap the picture to try another message, just like tapping the real widget.
            Store.next(this)
            PushWidget.refreshAll(this)
            drawPreview(preview)
        }
        frame.addView(preview, FrameLayout.LayoutParams(MATCH, WRAP))
        drawPreview(preview)

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(frame, look.lp())
        box.addView(
            TextView(this).apply {
                text = Ui.previewTip(this@SettingsActivity)
                textSize = 12f
                setTextColor(look.muted)
            },
            look.lp(top = 8)
        )
        return box
    }

    /** Paints the widget at the size of the one on your home screen (or a normal size if there is none yet). */
    private fun drawPreview(preview: ImageView) {
        val size = PushWidget.placedSizeDp(this) ?: Pair(300, 130)
        preview.setImageBitmap(WidgetArt.render(this, size.first, size.second.coerceAtMost(260)))
    }

    // ---------- Reminders ----------

    private fun buildReminders() {
        val panel = section(Ui.secReminders(this))

        val times = Store.reminderTimes(this)
        val timeLabels = times.map { Reminders.format(it.first, it.second) + "  ×" } + Ui.addTime(this)
        addRow(
            panel, Ui.rowTimes(this), if (times.isEmpty()) null else Ui.timesTip(this),
            look.chipsRow(timeLabels, times.size) { index ->
                if (index == times.size) {
                    addTimeFlow()
                } else {
                    removeTime(times[index].first, times[index].second)
                }
            }
        )

        addRow(
            panel, Ui.rowSkipRecent(this), Ui.skipRecentTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.skipRecent(this)) 1 else 0) {
                Store.setSkipRecent(this, it == 1)
                render()
            }
        )

        val verseOn = Store.verseOn(this)
        val verseLabels = mutableListOf(Ui.off(this), Ui.on(this))
        if (verseOn) verseLabels.add(Reminders.format(Store.verseHour(this), Store.verseMinute(this)))
        addRow(
            panel, Ui.rowVerse(this), Ui.verseTip(this),
            look.chipsRow(verseLabels, if (verseOn) 1 else 0) { index ->
                when (index) {
                    0 -> setVerse(false)
                    1 -> setVerse(true)
                    else -> pickVerseTime()
                }
            }
        )

        val checkInOn = Store.checkInOn(this)
        val checkInLabels = mutableListOf(Ui.off(this), Ui.on(this))
        if (checkInOn) checkInLabels.add(Reminders.format(Store.checkInHour(this), Store.checkInMinute(this)))
        addRow(
            panel, Ui.rowCheckIn(this), Ui.checkInTip(this),
            look.chipsRow(checkInLabels, if (checkInOn) 1 else 0) { index ->
                when (index) {
                    0 -> setCheckIn(false)
                    1 -> setCheckIn(true)
                    else -> pickCheckInTime()
                }
            }
        )
    }

    private fun setCheckIn(on: Boolean) {
        val hour = Store.checkInHour(this)
        val minute = Store.checkInMinute(this)
        Store.setCheckIn(this, on, hour, minute)
        if (on) {
            ensureNotificationPermission()
            Reminders.schedule(this, Reminders.TYPE_CHECKIN, hour, minute, Reminders.CHECKIN_CODE)
        } else {
            Reminders.cancel(this, Reminders.TYPE_CHECKIN, hour, minute, Reminders.CHECKIN_CODE)
        }
        render()
    }

    private fun pickCheckInTime() {
        TimePickerDialog(this, { _, hour, minute ->
            Reminders.cancel(
                this, Reminders.TYPE_CHECKIN, Store.checkInHour(this), Store.checkInMinute(this), Reminders.CHECKIN_CODE
            )
            Store.setCheckIn(this, true, hour, minute)
            Reminders.schedule(this, Reminders.TYPE_CHECKIN, hour, minute, Reminders.CHECKIN_CODE)
            render()
        }, Store.checkInHour(this), Store.checkInMinute(this), false).show()
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun addTimeFlow() {
        if (Store.reminderTimes(this).size >= 6) return
        ensureNotificationPermission()
        TimePickerDialog(this, { _, hour, minute ->
            val times = Store.reminderTimes(this)
            if (times.none { it.first == hour && it.second == minute }) {
                Store.setReminderTimes(this, times + Pair(hour, minute))
                Reminders.schedule(this, Reminders.TYPE_PUSH, hour, minute, Reminders.timeCode(hour, minute))
            }
            render()
        }, 9, 0, false).show()
    }

    private fun removeTime(hour: Int, minute: Int) {
        Store.setReminderTimes(this, Store.reminderTimes(this).filterNot { it.first == hour && it.second == minute })
        Reminders.cancel(this, Reminders.TYPE_PUSH, hour, minute, Reminders.timeCode(hour, minute))
        render()
    }

    private fun setVerse(on: Boolean) {
        val hour = Store.verseHour(this)
        val minute = Store.verseMinute(this)
        Store.setVerse(this, on, hour, minute)
        if (on) {
            ensureNotificationPermission()
            Reminders.schedule(this, Reminders.TYPE_VERSE, hour, minute, Reminders.VERSE_CODE)
        } else {
            Reminders.cancel(this, Reminders.TYPE_VERSE, hour, minute, Reminders.VERSE_CODE)
        }
        render()
    }

    private fun pickVerseTime() {
        TimePickerDialog(this, { _, hour, minute ->
            Reminders.cancel(
                this, Reminders.TYPE_VERSE, Store.verseHour(this), Store.verseMinute(this), Reminders.VERSE_CODE
            )
            Store.setVerse(this, true, hour, minute)
            Reminders.schedule(this, Reminders.TYPE_VERSE, hour, minute, Reminders.VERSE_CODE)
            render()
        }, Store.verseHour(this), Store.verseMinute(this), false).show()
    }

    // ---------- Feedback ----------

    private fun buildFeedback() {
        val panel = section(Ui.secFeedback(this))
        addRow(
            panel, Ui.rowVibration(this), Ui.vibrationTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.haptics(this)) 1 else 0) {
                Store.setHaptics(this, it == 1)
                if (it == 1) Haptics.tap(this)
                render()
            }
        )
        addRow(
            panel, Ui.rowSounds(this), Ui.soundsTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.sounds(this)) 1 else 0) {
                Store.setSounds(this, it == 1)
                if (it == 1) Haptics.tap(this)
                render()
            }
        )
        addRow(
            panel, Ui.rowAnimations(this), Ui.animationsTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.animations(this)) 1 else 0) {
                Store.setAnimations(this, it == 1)
                render()
            }
        )
        addRow(
            panel, Ui.rowDrift(this), Ui.driftTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.drift(this)) 1 else 0) {
                Store.setDrift(this, it == 1)
                PushWidget.refreshAll(this)
                render()
            }
        )
        addRow(
            panel, Ui.rowBig(this), Ui.bigTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.bigTargets(this)) 1 else 0) {
                Store.setBigTargets(this, it == 1)
                render()
            }
        )
    }

    // ---------- Messages ----------

    private fun buildMessages() {
        val panel = section(Ui.secMessages(this))
        addRow(
            panel, Ui.rowTone(this), Ui.toneTip(this),
            look.chipsRow(listOf(Ui.off(this), Ui.on(this)), if (Store.timeTone(this)) 1 else 0) {
                Store.setTimeTone(this, it == 1)
                render()
            }
        )
        addNav(panel, Ui.navSaved(this, Library.savedMessages(this).size)) { openLibrary(LibraryActivity.TAB_SAVED) }
        addNav(panel, Ui.navMine(this, Library.customs(this).size)) { openLibrary(LibraryActivity.TAB_MINE) }
    }

    // ---------- Backup ----------

    private fun buildBackup() {
        val panel = section(Ui.secBackup(this))
        addNav(panel, Ui.rowBackup(this)) { exportBackup.launch("begin-backup.json") }
        addNav(panel, Ui.rowRestore(this)) { importBackup.launch(arrayOf("application/json", "text/plain", "*/*")) }
        val tip = TextView(this)
        tip.text = Ui.backupTip(this)
        tip.textSize = 13f
        tip.setTextColor(look.muted)
        tip.setPadding(look.dp(6), 0, look.dp(6), 0)
        content.addView(tip, look.lp(top = 8))
    }

    private fun writeBackup(uri: android.net.Uri) {
        val ok = try {
            contentResolver.openOutputStream(uri)?.use { it.write(Library.exportJson(this).toByteArray()) } != null
        } catch (e: Exception) {
            false
        }
        Toast.makeText(this, if (ok) Ui.backupDone(this) else Ui.shareFailed(this), Toast.LENGTH_SHORT).show()
    }

    private fun readBackup(uri: android.net.Uri) {
        val raw = try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            null
        }
        val added = if (raw == null) -1 else Library.importJson(this, raw)
        val note = if (added < 0) Ui.restoreBad(this) else Ui.restoreDone(this, added)
        Toast.makeText(this, note, Toast.LENGTH_SHORT).show()
        PushWidget.refreshAll(this)
        render()
    }

    private fun openLibrary(tab: Int) {
        startActivity(Intent(this, LibraryActivity::class.java).putExtra(LibraryActivity.EXTRA_TAB, tab))
    }
}
