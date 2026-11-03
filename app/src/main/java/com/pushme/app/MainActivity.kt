package com.pushme.app

import android.animation.ValueAnimator
import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Outline
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.min

/**
 * The main screen: a big message on a frosted glass card, message-type chips and a language
 * switch. Touch the left half of the card (or swipe right) to go back, the right half (or swipe
 * left) to go forward. Hold the card to copy the message. Tap the title for a calm, bare screen.
 * Everything else lives in Settings.
 */
class MainActivity : AppCompatActivity() {

    /** One chip in the type row. [source]: 0 = everything, 1 = only saved, 2 = only my own. */
    private data class TypeChip(val kind: Kind?, val source: Int)

    private val typeChips = listOf(
        TypeChip(null, 0),
        TypeChip(null, 1),
        TypeChip(null, 2),
        TypeChip(Kind.AFFIRM, 0),
        TypeChip(Kind.WHY, 0),
        TypeChip(Kind.STEP, 0),
        TypeChip(Kind.RESTART, 0),
        TypeChip(Kind.FAITH, 0)
    )

    // Top-right switch. Tap the selected one again to go back to "all languages".
    private val langs: List<Lang> = listOf(Lang.EN, Lang.AM)

    private lateinit var look: Look
    private lateinit var backdrop: BackdropView
    private lateinit var backdropBlur: BackdropView
    private lateinit var card: FrameLayout
    private lateinit var cardTint: View
    private lateinit var glow: TouchGlowView
    private lateinit var burst: BurstView
    private lateinit var message: TextView
    private lateinit var cardLabel: TextView
    private lateinit var heart: ImageView
    private lateinit var share: ImageView
    private lateinit var timerPill: TextView
    private lateinit var title: TextView
    private lateinit var hintBack: TextView
    private lateinit var hintNext: TextView
    private lateinit var settingsButton: ImageView
    private lateinit var headerDivider: View
    private lateinit var langToggle: FrameLayout
    private lateinit var langRow: LinearLayout
    private lateinit var langIndicator: View
    private lateinit var typeRow: LinearLayout
    private lateinit var chipsScroll: View
    private lateinit var root: FrameLayout

    private val typeViews = mutableListOf<TextView>()
    private val langViews = mutableListOf<TextView>()

    // The 2-minute start timer.
    private val ticker = Handler(Looper.getMainLooper())
    private var timerDone = false
    private val tick = object : Runnable {
        override fun run() {
            if (updateTimer()) ticker.postDelayed(this, 250)
        }
    }
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var focus = false
    private var showingEmpty = false
    private var indicatorAnimator: ValueAnimator? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Paint the window in the chosen theme before anything else, so the screen never flashes another color.
        Look(this).applyWindow(this)
        setContentView(R.layout.activity_main)

        look = Look(this)
        root = findViewById(R.id.root)
        backdrop = findViewById(R.id.backdrop)
        backdropBlur = findViewById(R.id.backdrop_blur)
        card = findViewById(R.id.card)
        cardTint = findViewById(R.id.card_tint)
        glow = findViewById(R.id.touch_glow)
        burst = findViewById(R.id.burst)
        message = findViewById(R.id.message)
        cardLabel = findViewById(R.id.card_label)
        heart = findViewById(R.id.heart)
        share = findViewById(R.id.btn_share)
        timerPill = findViewById(R.id.btn_timer)
        title = findViewById(R.id.title)
        hintBack = findViewById(R.id.hint_back)
        hintNext = findViewById(R.id.hint_next)
        settingsButton = findViewById(R.id.btn_settings)
        headerDivider = findViewById(R.id.divider_header)
        langToggle = findViewById(R.id.lang_toggle)
        langRow = findViewById(R.id.lang_row)
        langIndicator = findViewById(R.id.lang_indicator)
        typeRow = findViewById(R.id.chips_kind)
        chipsScroll = typeRow.parent as View

        // Round the glass card and clip its blurred background to the same shape.
        card.clipToOutline = true
        card.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, look.dp(28).toFloat())
            }
        }

        // Keep the blurred copy lined up with the sharp background behind it.
        val relayout = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> syncBackdrop() }
        root.addOnLayoutChangeListener(relayout)
        card.addOnLayoutChangeListener(relayout)

        buildTypeChips()
        buildLangToggle()
        setUpCardTouch()

        heart.setOnClickListener { toggleSaved() }
        share.setOnClickListener { ShareHelper.shareCard(this) }
        timerPill.setOnClickListener { toggleTimer() }
        settingsButton.setOnClickListener { Motion.open(this, Intent(this, SettingsActivity::class.java)) }
        title.setOnClickListener { setFocus(!focus) }

        applyLook()
        // The card is already in place and showing the last message; only the header and chips ease in.
        Motion.enter(this, listOf(title.parent as View, chipsScroll), step = 40)
    }

    override fun onResume() {
        super.onResume()
        // Settings may have changed, and the widget or a reminder may have changed the message.
        Store.markOpen(this)
        applyLook()
        Reminders.scheduleAll(this)
        // The phone may have switched between light and dark while we were away.
        PushWidget.refreshAll(this)
        ticker.removeCallbacks(tick)
        ticker.post(tick)
    }

    override fun onPause() {
        super.onPause()
        Store.markOpen(this)
        ticker.removeCallbacks(tick)
    }

    override fun onStop() {
        super.onStop()
        // Always come back to the normal screen, never to a hidden-controls screen.
        if (focus) setFocus(false)
    }

    // ---------- the card: tap zones, swipe that follows the finger, hold to copy ----------

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpCardTouch() {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        val holdTime = ViewConfiguration.getLongPressTimeout().toLong()
        var downX = 0f
        var downY = 0f
        var dragging = false
        var held = false
        val hold = Runnable {
            held = true
            copyMessage()
        }

        card.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    dragging = false
                    held = false
                    message.animate().cancel()
                    v.postDelayed(hold, holdTime)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (abs(dx) > slop || abs(dy) > slop) v.removeCallbacks(hold)
                    if (!dragging && abs(dx) > slop && abs(dx) > abs(dy)) {
                        dragging = true
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    if (dragging && Motion.on(this)) {
                        message.translationX = dx * 0.55f
                        message.alpha = 1f - min(0.6f, abs(dx) / v.width)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.removeCallbacks(hold)
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (held) {
                        // The hold already copied the message; do not also move on.
                    } else if (dragging) {
                        if (abs(dx) > look.dp(48)) {
                            if (dx < 0) goNext() else goBack()
                        } else {
                            snapBack()
                        }
                    } else if (abs(dx) < slop && abs(dy) < slop) {
                        v.performClick()
                        glow.pulse(event.x, event.y)
                        if (event.x < v.width / 2f) goBack() else goNext()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.removeCallbacks(hold)
                    snapBack()
                }
            }
            true
        }
    }

    /** The message returns to the middle after a swipe that was not long enough. */
    private fun snapBack() {
        if (!Motion.on(this)) {
            message.translationX = 0f
            message.alpha = 1f
            return
        }
        message.animate().translationX(0f).alpha(1f).setDuration(160).start()
    }

    private fun copyMessage() {
        if (showingEmpty) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Begin", Library.tidy(Store.current(this))))
        Haptics.tap(this)
        // Android 13 and newer shows its own "copied" note.
        if (Build.VERSION.SDK_INT < 33) {
            Toast.makeText(this, Ui.copied(this), Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSaved() {
        val text = Store.current(this)
        Library.toggleSaved(this, text)
        val nowSaved = Library.isSaved(this, text)
        updateHeart()
        if (nowSaved) Haptics.save(this) else Haptics.tick(this)
        Motion.pop(this, heart)
        if (nowSaved) burst.fire(heart.x + heart.width / 2f, heart.y + heart.height / 2f, look.rose)
    }

    // ---------- moving between messages ----------

    private fun goNext() {
        Store.next(this)
        shown(fromRight = true)
    }

    private fun goBack() {
        if (Store.previous(this) == null) {
            nudge()
            return
        }
        shown(fromRight = false)
    }

    /** After a filter changes: always a brand new message. */
    private fun goFresh() {
        Store.pickNew(this)
        shown(fromRight = true, buzz = false)
        Haptics.tick(this)
    }

    private fun shown(fromRight: Boolean, buzz: Boolean = true) {
        showCurrent()
        message.animate().cancel()
        if (Motion.on(this)) {
            message.translationX = look.dp(if (fromRight) 28 else -28).toFloat()
            message.alpha = 0f
            message.animate().translationX(0f).alpha(1f).setDuration(200).start()
            Motion.revealWords(this, message, message.text)
        } else {
            message.translationX = 0f
            message.alpha = 1f
        }
        if (buzz) Haptics.tap(this)
        PushWidget.refreshAll(this)
    }

    /** A small wiggle that says "there is nothing before this one". */
    private fun nudge() {
        if (!Motion.on(this)) {
            snapBack()
            return
        }
        message.animate().translationX(look.dp(14).toFloat()).setDuration(70).withEndAction {
            message.animate().translationX(0f).alpha(1f).setDuration(110).start()
        }.start()
    }

    private fun showCurrent() {
        val source = Store.source(this)
        val nothing = (source == 1 && Library.savedTexts(this).isEmpty()) ||
            (source == 2 && Library.customs(this).isEmpty())
        showingEmpty = nothing
        if (nothing) {
            message.text = if (source == 1) Ui.emptySaved(this) else Ui.emptyMineMain(this)
            cardLabel.text = if (source == 1) Ui.captionSaved(this) else Ui.captionMine(this)
            heart.visibility = View.INVISIBLE
            share.visibility = View.INVISIBLE
            return
        }
        val text = Store.current(this)
        message.text = Fonts.styled(this, Library.tidy(text))
        cardLabel.text = Ui.caption(this, text)
        val buttons = if (focus) View.INVISIBLE else View.VISIBLE
        heart.visibility = buttons
        share.visibility = buttons
        updateHeart()
    }

    private fun updateHeart() {
        val saved = Library.isSaved(this, Store.current(this))
        heart.setImageResource(if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        look.styleIconButton(heart, if (saved) look.rose else look.ink)
    }

    // ---------- the 2-minute start timer ----------

    /** Tap to start two minutes of "just begin"; tap while it runs to stop. */
    private fun toggleTimer() {
        if (Store.timerEnd(this) > System.currentTimeMillis()) {
            Reminders.cancelTimer(this)
            Haptics.tick(this)
        } else {
            askNotificationPermission()
            Reminders.startTimer(this, 2 * 60 * 1000L)
            Haptics.tap(this)
            Motion.pop(this, timerPill)
        }
        timerDone = false
        ticker.removeCallbacks(tick)
        ticker.post(tick)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Refreshes the pill. Returns true while the countdown is still running. */
    private fun updateTimer(): Boolean {
        val end = Store.timerEnd(this)
        val left = end - System.currentTimeMillis()
        if (end > 0L && left <= 0L) {
            // Finished while the app is open: celebrate here, and cancel the notification.
            Reminders.cancelTimer(this)
            Haptics.done(this)
            timerDone = true
            Motion.pop(this, timerPill)
            ticker.postDelayed({
                timerDone = false
                updateTimer()
            }, 3000)
        }
        if (left > 0L) {
            val seconds = ((left + 999L) / 1000L).toInt()
            timerPill.text = String.format("%d:%02d  ✕", seconds / 60, seconds % 60)
            look.styleChip(timerPill, true)
            return true
        }
        timerPill.text = if (timerDone) Ui.timerDone(this) else Ui.timerStart(this)
        look.styleChip(timerPill, false)
        return false
    }

    // ---------- focus mode: only the message ----------

    private fun setFocus(on: Boolean) {
        focus = on
        val hideable = listOf(
            langToggle, settingsButton, headerDivider, chipsScroll, cardLabel, heart, share, hintBack, hintNext, timerPill
        )
        hideable.forEach { view ->
            val shownAlpha = if (view === hintBack || view === hintNext) 0.8f else 1f
            view.animate().cancel()
            if (!Motion.on(this)) {
                view.alpha = shownAlpha
                view.visibility = if (on) View.INVISIBLE else View.VISIBLE
            } else if (on) {
                view.animate().alpha(0f).setDuration(220).withEndAction {
                    if (focus) view.visibility = View.INVISIBLE
                }.start()
            } else {
                view.visibility = View.VISIBLE
                view.alpha = 0f
                view.animate().alpha(shownAlpha).setDuration(220).start()
            }
        }
        title.animate().cancel()
        title.alpha = if (on) 0.5f else 1f
        showCurrent()
    }

    // ---------- chips ----------

    private fun buildTypeChips() {
        typeChips.forEach { entry ->
            val chip = look.chip("")
            chip.setOnClickListener {
                Store.setSource(this, entry.source)
                Store.setKind(this, entry.kind)
                Store.startOver(this)
                refreshChips()
                goFresh()
            }
            typeRow.addView(chip, look.lp(WRAP, WRAP, end = 8))
            typeViews.add(chip)
        }
    }

    private fun buildLangToggle() {
        langToggle.setPadding(look.dp(3), look.dp(3), look.dp(3), look.dp(3))
        langs.forEach { value ->
            val segment = TextView(this).apply {
                textSize = 14f
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                setPadding(look.dp(14), look.dp(7), look.dp(14), look.dp(7))
                setOnClickListener {
                    val already = Store.lang(this@MainActivity) == value
                    Store.setLang(this@MainActivity, if (already) null else value)
                    Store.startOver(this@MainActivity)
                    refreshChips(animate = true)
                    goFresh()
                }
            }
            langRow.addView(segment)
            langViews.add(segment)
        }
    }

    private fun refreshChips(animate: Boolean = false) {
        val source = Store.source(this)
        typeChips.forEachIndexed { i, entry ->
            val chip = typeViews[i]
            chip.text = when {
                entry.source == 1 -> Ui.saved(this)
                entry.source == 2 -> Ui.mine(this)
                else -> Ui.kind(this, entry.kind)
            }
            val selected = if (entry.source != 0) source == entry.source else source == 0 && entry.kind == Store.kind(this)
            look.styleChip(chip, selected)
            if (entry.source == 1) {
                chip.compoundDrawablePadding = look.dp(6)
                chip.setCompoundDrawablesRelative(look.icon(R.drawable.ic_heart_filled, 14, look.rose), null, null, null)
            }
        }

        val current = Store.lang(this)
        langViews.forEachIndexed { i, segment ->
            segment.text = Ui.lang(this, langs[i])
            segment.setTextColor(if (langs[i] == current) look.selText else look.ink)
        }
        placeIndicator(animate)
    }

    /** The raised highlight behind the selected language slides across to the one you tapped. */
    private fun placeIndicator(animate: Boolean) {
        val selected = langs.indexOf(Store.lang(this))
        langRow.post {
            indicatorAnimator?.cancel()
            if (selected < 0) {
                langIndicator.alpha = 0f
                return@post
            }
            val segment = langViews[selected]
            val params = langIndicator.layoutParams
            // Same height as the row of languages (a "match parent" height made the toggle as tall as the screen).
            params.height = langRow.height
            val toX = segment.left.toFloat()
            val toW = segment.width
            val wasHidden = langIndicator.alpha == 0f
            if (!animate || !Motion.on(this) || wasHidden) {
                params.width = toW
                langIndicator.layoutParams = params
                langIndicator.translationX = toX
                if (wasHidden && animate && Motion.on(this)) {
                    langIndicator.animate().alpha(1f).setDuration(160).start()
                } else {
                    langIndicator.alpha = 1f
                }
                return@post
            }
            val fromX = langIndicator.translationX
            val fromW = params.width
            indicatorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 240
                addUpdateListener {
                    val t = it.animatedValue as Float
                    params.width = (fromW + (toW - fromW) * t).toInt()
                    langIndicator.layoutParams = params
                    langIndicator.translationX = fromX + (toX - fromX) * t
                }
                start()
            }
        }
    }

    // ---------- look ----------

    /** Applies the chosen theme, glass, fonts and app language to everything on screen. */
    private fun applyLook() {
        look = Look(this)
        look.applyWindow(this)
        backdrop.setArt(look.theme)
        backdropBlur.setArt(look.theme)

        title.text = Fonts.styled(this, Ui.title(this))
        title.setTextColor(look.ink)
        val spacing = if (Ui.isAmharic(this)) 0f else 0.12f
        cardLabel.letterSpacing = spacing
        cardLabel.setTextColor(look.muted)
        message.setTextColor(look.ink)
        hintBack.text = "‹  " + Ui.back(this)
        hintNext.text = Ui.next(this) + "  ›"
        hintBack.setTextColor(look.muted)
        hintNext.setTextColor(look.muted)
        hintBack.alpha = 0.8f
        hintNext.alpha = 0.8f

        headerDivider.setBackgroundColor(look.hairline)

        // Icon buttons follow the "Bigger tap targets" setting.
        listOf(settingsButton, heart, share).forEach { button ->
            val params = button.layoutParams
            params.width = look.dp(look.iconDp)
            params.height = look.dp(look.iconDp)
            button.layoutParams = params
        }
        (share.layoutParams as FrameLayout.LayoutParams).marginEnd = look.dp(14 + look.iconDp + 8)
        look.styleIconButton(settingsButton)
        look.styleIconButton(share)

        langToggle.background = look.rounded(look.softFill, 100, look.hairline)
        langIndicator.background = look.raised(look.selTop, look.selBottom, 100)

        applyGlass()
        refreshChips()
        showCurrent()
        updateTimer()
    }

    /** The glass card follows the Glass setting: low = clear and sharp, high = frosted and blurry. */
    private fun applyGlass() {
        val level = Themes.level(this)
        cardTint.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Themes.tint(this, level.alphaTop), Themes.tint(this, level.alphaBottom))
        ).apply {
            // Same rounding as the card, otherwise the corners look cut off.
            cornerRadius = look.dp(28).toFloat()
            // A very soft edge, not a hard line.
            if (look.edge != android.graphics.Color.TRANSPARENT) setStroke(look.dp(1), look.edge)
        }

        if (Build.VERSION.SDK_INT >= 31) {
            val px = look.dp(level.blurDp).toFloat()
            backdropBlur.visibility = View.VISIBLE
            backdropBlur.setRenderEffect(RenderEffect.createBlurEffect(px, px, Shader.TileMode.CLAMP))
        } else {
            // Older phones cannot blur; the tinted card shows the sharp background instead.
            backdropBlur.visibility = View.GONE
        }
    }

    private fun syncBackdrop() {
        val w = root.width.toFloat()
        val h = root.height.toFloat()
        if (w <= 0f || h <= 0f) return
        val a = IntArray(2)
        val b = IntArray(2)
        root.getLocationInWindow(a)
        card.getLocationInWindow(b)
        // The card slides in when the screen opens; measure where it really sits, not where it is mid-slide.
        val x = (b[0] - a[0]).toFloat() - card.translationX
        val y = (b[1] - a[1]).toFloat() - card.translationY
        backdrop.setFrame(w, h, 0f, 0f)
        backdropBlur.setFrame(w, h, x, y)
    }
}
