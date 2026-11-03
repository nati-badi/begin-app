package com.pushme.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Two lists in one screen, switched by tabs:
 *  - Saved: every message you hearted. Tap one to show it on the card, tap the heart to remove it.
 *  - Mine: write your own messages. Tap a line to edit it, tap the cross to delete it.
 */
class LibraryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAB = "tab"
        const val TAB_SAVED = 0
        const val TAB_MINE = 1
    }

    private lateinit var look: Look
    private lateinit var root: FrameLayout
    private lateinit var backdrop: BackdropView
    private lateinit var scroll: ScrollView
    private lateinit var content: LinearLayout

    private var tab = TAB_SAVED

    // What you are typing in the "Mine" tab, kept while the page is redrawn.
    private var draft = ""
    private var draftKind = Kind.AFFIRM
    private var editingIndex = -1

    private val kindOptions = listOf(Kind.AFFIRM, Kind.WHY, Kind.STEP, Kind.RESTART, Kind.FAITH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tab = intent.getIntExtra(EXTRA_TAB, TAB_SAVED)
        look = Look(this)

        root = FrameLayout(this)
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

    private fun render(resetScroll: Boolean = false) {
        val keepScroll = if (resetScroll) 0 else scroll.scrollY
        look = Look(this)
        look.applyWindow(this)
        backdrop.setArt(look.theme)

        content.removeAllViews()
        content.setPadding(look.dp(22), look.dp(14), look.dp(22), look.dp(36))

        buildHeader()
        buildTabs()
        if (tab == TAB_SAVED) buildSaved() else buildMine()

        scroll.post { scroll.scrollTo(0, keepScroll) }
    }

    // ---------- building blocks ----------

    private fun buildHeader() {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL

        val back = ImageView(this)
        back.setImageResource(R.drawable.ic_back)
        back.contentDescription = Ui.back(this)
        back.setPadding(look.dp(10), look.dp(10), look.dp(10), look.dp(10))
        look.styleIconButton(back)
        back.setOnClickListener { finish() }
        row.addView(back, LinearLayout.LayoutParams(look.dp(40), look.dp(40)))

        val heading = TextView(this)
        heading.text = Fonts.styled(this, Ui.secMessages(this))
        heading.textSize = 26f
        heading.setTextColor(look.ink)
        row.addView(heading, look.lp(WRAP, WRAP, start = 14))

        content.addView(row, look.lp())
    }

    private fun buildTabs() {
        val labels = listOf(
            Ui.tabSaved(this, Library.savedMessages(this).size),
            Ui.tabMine(this, Library.customs(this).size)
        )
        val tabs = look.chipsRow(labels, tab, quiet = false) { picked ->
            tab = picked
            render(resetScroll = true)
        }
        content.addView(tabs, look.lp(top = 18))
    }

    private fun note(text: String): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = 13f
        view.setTextColor(look.muted)
        return view
    }

    private fun smallLabel(text: String): TextView {
        val view = TextView(this)
        val amharic = Ui.isAmharic(this)
        view.text = if (amharic) text else text.uppercase()
        view.textSize = 12f
        view.letterSpacing = if (amharic) 0f else 0.1f
        view.setTextColor(look.muted)
        return view
    }

    /** One line in a list: the kind (small), the message, and one icon button on the right. */
    private fun messageRow(msg: Msg, iconRes: Int, iconTint: Int, onTap: () -> Unit, onIcon: () -> Unit): View {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(look.dp(16), look.dp(12), look.dp(8), look.dp(12))
        row.setOnClickListener { onTap() }

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL

        val caption = TextView(this)
        caption.text = Ui.kind(this, msg.kind)
        caption.textSize = 12f
        caption.setTextColor(look.muted)
        box.addView(caption)

        val body = TextView(this)
        body.text = Fonts.styled(this, Library.tidy(msg.text))
        body.textSize = 16f
        body.breakStrategy = android.text.Layout.BREAK_STRATEGY_BALANCED
        body.hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NONE
        body.setTextColor(look.ink)
        body.setPadding(0, look.dp(3), 0, 0)
        box.addView(body)

        row.addView(box, LinearLayout.LayoutParams(0, WRAP, 1f))

        val icon = ImageView(this)
        icon.setImageResource(iconRes)
        icon.imageTintList = ColorStateList.valueOf(iconTint)
        icon.setPadding(look.dp(9), look.dp(9), look.dp(9), look.dp(9))
        icon.setOnClickListener { onIcon() }
        row.addView(icon, LinearLayout.LayoutParams(look.dp(40), look.dp(40)))
        return row
    }

    // ---------- Saved ----------

    private fun buildSaved() {
        val saved = Library.savedMessages(this)
        if (saved.isEmpty()) {
            content.addView(note(Ui.emptySaved(this)), look.lp(top = 24))
            return
        }
        content.addView(note(Ui.tapToShow(this)), look.lp(top = 16, bottom = 10))

        val panel = look.panel()
        content.addView(panel, look.lp())
        saved.forEachIndexed { index, msg ->
            if (index > 0) panel.addView(look.divider(), look.dividerParams())
            val row = messageRow(
                msg, R.drawable.ic_heart_filled, look.rose,
                onTap = { showOnCard(msg.text) },
                onIcon = {
                    Library.toggleSaved(this, msg.text)
                    render()
                    UndoBar.show(root, look, Ui.removedFromSaved(this), Ui.undo(this)) {
                        Library.toggleSaved(this, msg.text)
                        render()
                    }
                }
            )
            panel.addView(row, look.lp())
        }
    }

    /** Puts the chosen message on the card and goes straight back to the main screen. */
    private fun showOnCard(text: String) {
        Store.show(this, text)
        PushWidget.refreshAll(this)
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    // ---------- Mine ----------

    private fun buildMine() {
        val items = Library.customs(this)

        val editor = look.panel()
        content.addView(editor, look.lp(top = 18))

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(look.dp(14), look.dp(14), look.dp(14), look.dp(16))
        editor.addView(box, look.lp())

        val input = EditText(this)
        input.hint = Ui.libHint(this)
        input.setText(draft)
        input.minLines = 3
        input.gravity = Gravity.TOP or Gravity.START
        input.inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        input.textSize = 16f
        input.setTextColor(look.ink)
        input.setHintTextColor(look.muted)
        input.background = look.rounded(look.quietFill, 14, look.hairline)
        input.setPadding(look.dp(14), look.dp(12), look.dp(14), look.dp(12))
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                draft = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        box.addView(input, look.lp())

        box.addView(smallLabel(Ui.libType(this)), look.lp(top = 14, bottom = 8))
        val kindChips = look.chipsRow(
            kindOptions.map { Ui.kind(this, it) },
            kindOptions.indexOf(draftKind)
        ) { picked ->
            draftKind = kindOptions[picked]
            render()
        }
        box.addView(kindChips, look.lp())

        val button = Button(this)
        button.text = if (editingIndex >= 0) Ui.save(this) else Ui.add(this)
        button.textSize = 16f
        button.isAllCaps = false
        button.stateListAnimator = null
        look.stylePrimary(button, 27)
        button.setOnClickListener {
            val text = draft.trim()
            if (text.isEmpty()) return@setOnClickListener
            if (editingIndex >= 0) {
                Library.updateCustom(this, editingIndex, text, draftKind)
            } else {
                Library.addCustom(this, text, draftKind)
            }
            hideKeyboard(input)
            draft = ""
            editingIndex = -1
            render()
        }
        box.addView(button, look.lp(height = look.dp(54), top = 18))

        if (items.isEmpty()) {
            content.addView(note(Ui.emptyMine(this)), look.lp(top = 18))
            return
        }
        content.addView(note(Ui.tapToEdit(this)), look.lp(top = 18, bottom = 10))

        val panel = look.panel()
        content.addView(panel, look.lp())
        items.forEachIndexed { index, msg ->
            if (index > 0) panel.addView(look.divider(), look.dividerParams())
            val row = messageRow(
                msg, R.drawable.ic_close, look.muted,
                onTap = { startEdit(index, msg) },
                onIcon = { delete(index) }
            )
            panel.addView(row, look.lp())
        }
    }

    private fun startEdit(index: Int, msg: Msg) {
        editingIndex = index
        draft = msg.text
        draftKind = msg.kind
        render(resetScroll = true)
    }

    private fun delete(index: Int) {
        val removed = Library.customs(this).getOrNull(index)
        Library.deleteCustom(this, index)
        if (editingIndex == index) {
            editingIndex = -1
            draft = ""
        } else if (editingIndex > index) {
            editingIndex -= 1
        }
        render()
        if (removed != null) {
            UndoBar.show(root, look, Ui.messageDeleted(this), Ui.undo(this)) {
                Library.restoreCustom(this, index, removed)
                render()
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
