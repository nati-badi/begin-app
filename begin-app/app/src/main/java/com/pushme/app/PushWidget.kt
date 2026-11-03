package com.pushme.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast

/**
 * The home-screen widget. One widget that you resize yourself (long-press it, drag the edges).
 *  - Tap the right half: the next message. Tap the left half: the one before.
 *  - Top / bottom of the widget (if a gesture list is chosen in Settings): forward / back through Saved or My messages.
 *  - Tap the small arrow in the corner: opens the app.
 *  - Also changes by itself about once an hour (Android may delay this to save battery).
 * The widget is one picture painted by WidgetArt, so it can use your chosen fonts.
 */
class PushWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // Called when the widget is added and on the hourly refresh.
        Store.pickNew(context)
        ids.forEach { manager.updateAppWidget(it, build(context, manager, it)) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // The widget was resized: redraw at the new size.
        manager.updateAppWidget(appWidgetId, build(context, manager, appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_NEXT -> singleTap(context, right = true)
            ACTION_PREV -> singleTap(context, right = false)
            ACTION_LIST_FORWARD -> stepList(context, forward = true)
            ACTION_LIST_BACK -> stepList(context, forward = false)
        }
    }

    /** Left half = the message before, right half = the next one (through everything). */
    private fun singleTap(context: Context, right: Boolean) {
        // If the list you were filtering by has become empty, go back to everything.
        val source = Store.source(context)
        if ((source == 1 && Library.savedTexts(context).isEmpty()) || (source == 2 && Library.customs(context).isEmpty())) {
            Store.setSource(context, 0)
        }
        if (right) Store.next(context) else Store.previous(context)
        Haptics.tap(context)
        refreshAll(context)
    }

    /**
     * Top of the widget = forward, bottom = back, through the list chosen in Settings > Widget > Gesture
     * (Saved or My messages). It carries on from where it stopped and wraps around.
     */
    private fun stepList(context: Context, forward: Boolean) {
        val mode = Store.gestureMode(context)
        if (mode == 0) return
        val list = if (mode == 2) Library.customs(context) else Library.savedMessages(context)
        if (list.isEmpty()) {
            val text = if (mode == 2) Ui.nothingMine(context) else Ui.nothingSaved(context)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            return
        }
        val current = Store.current(context)
        val shown = list.indexOfFirst { it.text == current }
        val at = if (shown >= 0) shown else Store.gesturePos(context, mode).coerceAtMost(list.size - 1)
        val target = when {
            forward -> (at + 1) % list.size
            at < 0 -> list.size - 1
            else -> (at - 1 + list.size) % list.size
        }
        Store.setGesturePos(context, mode, target)
        Store.show(context, list[target].text)
        Haptics.tap(context)
        refreshAll(context)
    }

    companion object {
        const val ACTION_NEXT = "com.pushme.app.ACTION_NEXT"
        const val ACTION_PREV = "com.pushme.app.ACTION_PREV"
        const val ACTION_LIST_FORWARD = "com.pushme.app.ACTION_LIST_FORWARD"
        const val ACTION_LIST_BACK = "com.pushme.app.ACTION_LIST_BACK"

        /** Redraw every widget with the current message, theme, glass and fonts (no new pick). */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PushWidget::class.java))
            ids.forEach { manager.updateAppWidget(it, build(context, manager, it)) }
        }

        /** Size in dp of the first widget on the home screen, or null if there is none yet. */
        fun placedSizeDp(context: Context): Pair<Int, Int>? {
            val manager = AppWidgetManager.getInstance(context)
            val id = manager.getAppWidgetIds(ComponentName(context, PushWidget::class.java)).firstOrNull()
                ?: return null
            return sizeDp(context, manager, id)
        }

        private fun sizeDp(context: Context, manager: AppWidgetManager, id: Int): Pair<Int, Int> {
            val options = manager.getAppWidgetOptions(id)
            val portrait = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
            val widthDp = options.getInt(
                if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
                else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
            ).takeIf { it > 0 } ?: 250
            val heightDp = options.getInt(
                if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
                else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
            ).takeIf { it > 0 } ?: 180
            return Pair(widthDp, heightDp)
        }

        private fun build(context: Context, manager: AppWidgetManager, id: Int): RemoteViews {
            val (widthDp, heightDp) = sizeDp(context, manager, id)

            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setImageViewBitmap(R.id.widget_image, WidgetArt.render(context, widthDp, heightDp))

            val dark = Themes.darkText(context)
            views.setTextColor(R.id.widget_open, if (dark) 0xFF4B5563.toInt() else 0xCCFFFFFF.toInt())

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE


            val nextIntent = Intent(context, PushWidget::class.java).setAction(ACTION_NEXT)
            views.setOnClickPendingIntent(
                R.id.widget_zone_next,
                PendingIntent.getBroadcast(context, 0, nextIntent, flags)
            )

            val prevIntent = Intent(context, PushWidget::class.java).setAction(ACTION_PREV)
            views.setOnClickPendingIntent(
                R.id.widget_zone_back,
                PendingIntent.getBroadcast(context, 2, prevIntent, flags)
            )

            // Top and bottom bands: only when a gesture list is chosen and the widget is tall enough.
            val bands = Store.gestureMode(context) != 0 && heightDp >= 100
            val band = if (bands) View.VISIBLE else View.GONE
            views.setViewVisibility(R.id.widget_zone_up, band)
            views.setViewVisibility(R.id.widget_zone_down, band)
            val upIntent = Intent(context, PushWidget::class.java).setAction(ACTION_LIST_FORWARD)
            views.setOnClickPendingIntent(
                R.id.widget_zone_up,
                PendingIntent.getBroadcast(context, 3, upIntent, flags)
            )
            val downIntent = Intent(context, PushWidget::class.java).setAction(ACTION_LIST_BACK)
            views.setOnClickPendingIntent(
                R.id.widget_zone_down,
                PendingIntent.getBroadcast(context, 4, downIntent, flags)
            )

            val openIntent = Intent(context, MainActivity::class.java)
            views.setOnClickPendingIntent(
                R.id.widget_open,
                PendingIntent.getActivity(context, 1, openIntent, flags)
            )
            return views
        }
    }
}
