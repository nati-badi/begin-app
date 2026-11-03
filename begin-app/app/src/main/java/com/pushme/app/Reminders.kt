package com.pushme.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * Daily reminders: a push at times you choose, plus an optional morning verse.
 * Uses Android's alarm clock in its battery-friendly mode, so a reminder can arrive
 * a few minutes after the exact time.
 */
object Reminders {
    private const val EXTRA_TYPE = "type"
    private const val EXTRA_HOUR = "hour"
    private const val EXTRA_MINUTE = "minute"
    private const val EXTRA_CODE = "code"
    const val TYPE_PUSH = "push"
    const val TYPE_VERSE = "verse"
    const val TYPE_CHECKIN = "checkin"
    const val TYPE_TIMER = "timer"
    const val CHECKIN_CODE = 5001
    const val TIMER_CODE = 6000
    private const val TIMER_CHANNEL = "begin_timer"
    private const val TIMER_NOTE_ID = 10
    private const val CHANNEL = "begin_reminders"
    const val VERSE_CODE = 5000
    private const val RECENT_MS = 60L * 60L * 1000L

    fun timeCode(hour: Int, minute: Int): Int = 100 + hour * 60 + minute

    fun format(hour: Int, minute: Int): String {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        val suffix = if (hour < 12) "AM" else "PM"
        return "$h12:${minute.toString().padStart(2, '0')} $suffix"
    }

    /** Sets every alarm again (after a restart, or when anything changed). */
    fun scheduleAll(c: Context) {
        Store.reminderTimes(c).forEach { (h, m) -> schedule(c, TYPE_PUSH, h, m, timeCode(h, m)) }
        if (Store.verseOn(c)) {
            schedule(c, TYPE_VERSE, Store.verseHour(c), Store.verseMinute(c), VERSE_CODE)
        }
        if (Store.checkInOn(c)) {
            schedule(c, TYPE_CHECKIN, Store.checkInHour(c), Store.checkInMinute(c), CHECKIN_CODE)
        }
    }

    /** Starts the 2-minute timer. If you leave the app, a notification tells you when it is over. */
    fun startTimer(c: Context, millis: Long) {
        val end = System.currentTimeMillis() + millis
        Store.setTimerEnd(c, end)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, end, pending(c, TYPE_TIMER, 0, 0, TIMER_CODE))
        showTimerNotification(c, end)
        PushWidget.refreshAll(c)
    }

    fun cancelTimer(c: Context) {
        Store.setTimerEnd(c, 0L)
        cancel(c, TYPE_TIMER, 0, 0, TIMER_CODE)
        NotificationManagerCompat.from(c).cancel(TIMER_NOTE_ID)
        PushWidget.refreshAll(c)
    }

    /** A quiet status-bar countdown that ticks by itself while the timer runs. */
    @SuppressLint("MissingPermission")
    private fun showTimerNotification(c: Context, end: Long) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = NotificationManagerCompat.from(c)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(TIMER_CHANNEL, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Timer")
                .build()
        )
        val open = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val big = RemoteViews(c.packageName, R.layout.notif_timer)
        big.setChronometerCountDown(R.id.timer_big, true)
        big.setChronometer(R.id.timer_big, SystemClock.elapsedRealtime() + (end - System.currentTimeMillis()), null, true)
        big.setTextViewText(R.id.timer_label, Ui.timerRunning(c))
        val note = NotificationCompat.Builder(c, TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(Ui.title(c))
            .setContentText(Ui.timerRunning(c))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(big)
            .setCustomBigContentView(big)
            .setContentIntent(open)
            .setWhen(end)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setTimeoutAfter((end - System.currentTimeMillis()).coerceAtLeast(1000L))
            .build()
        manager.notify(TIMER_NOTE_ID, note)
    }

    fun schedule(c: Context, type: String, hour: Int, minute: Int, code: Int) {
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 1000) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pending(c, type, hour, minute, code)
        )
    }

    fun cancel(c: Context, type: String, hour: Int, minute: Int, code: Int) {
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending(c, type, hour, minute, code))
    }

    private fun pending(c: Context, type: String, hour: Int, minute: Int, code: Int): PendingIntent {
        val intent = Intent(c, ReminderReceiver::class.java)
            .putExtra(EXTRA_TYPE, type)
            .putExtra(EXTRA_HOUR, hour)
            .putExtra(EXTRA_MINUTE, minute)
            .putExtra(EXTRA_CODE, code)
        return PendingIntent.getBroadcast(
            c, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Called by the alarm: pick a message, update the widget, show the notification. */
    fun fire(c: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_PUSH
        val hour = intent.getIntExtra(EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val code = intent.getIntExtra(EXTRA_CODE, timeCode(hour, minute))

        // The 2-minute timer is a one-off: tell you it is over (unless the app already did) and stop.
        if (type == TYPE_TIMER) {
            if (Store.timerEnd(c) == 0L) return
            Store.setTimerEnd(c, 0L)
            NotificationManagerCompat.from(c).cancel(TIMER_NOTE_ID)
            PushWidget.refreshAll(c)
            Haptics.done(c)
            notify(c, Ui.timerDoneNote(c), 8)
            return
        }

        // Set tomorrow's alarm first, so a problem below never stops the daily reminder.
        schedule(c, type, hour, minute, code)

        if (type == TYPE_CHECKIN) {
            notify(c, Ui.checkInText(c), 9)
            return
        }

        // You just had the app open: no need to nudge you now.
        if (type == TYPE_PUSH && Store.skipRecent(c) &&
            System.currentTimeMillis() - Store.lastOpen(c) < RECENT_MS
        ) return

        val text = if (type == TYPE_VERSE) {
            val verse = Library.pick(c, Kind.FAITH, Store.lang(c), Store.peekCurrent(c), useSavedFilter = false)
            Store.show(c, verse)
        } else {
            Store.pickNew(c)
        }
        PushWidget.refreshAll(c)
        notify(c, text, 7, saved = Library.isSaved(c, text))
    }

    /**
     * Shows a notification. [saved] = null: no heart button (check-in, timer). false: a "♥ Save"
     * button. true: the same notification with "♥ Saved". [quiet] = shown again without any sound.
     */
    @SuppressLint("MissingPermission")
    fun notify(c: Context, text: String, id: Int = 7, saved: Boolean? = null, quiet: Boolean = false) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = NotificationManagerCompat.from(c)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Reminders")
                .build()
        )

        val open = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(Ui.title(c))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setSilent(quiet)
        if (saved != null) {
            val save = Intent(c, SaveReceiver::class.java)
                .putExtra(SaveReceiver.EXTRA_TEXT, text)
                .putExtra(SaveReceiver.EXTRA_ID, id)
            val savePending = PendingIntent.getBroadcast(
                c, 7000 + id, save, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (saved) {
                builder.addAction(R.drawable.ic_heart_filled, Ui.notifSaved(c), open)
            } else {
                builder.addAction(R.drawable.ic_heart_outline, Ui.notifSave(c), savePending)
            }
        }
        manager.notify(id, builder.build())
    }
}

/** Receives the daily alarm. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.fire(context, intent)
    }
}

/** The "♥ Save" button on a notification: saves that message and shows the notification again as "♥ Saved". */
class SaveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra(EXTRA_TEXT) ?: return
        val id = intent.getIntExtra(EXTRA_ID, 7)
        if (!Library.isSaved(context, text)) Library.toggleSaved(context, text)
        Haptics.save(context)
        PushWidget.refreshAll(context)
        Reminders.notify(context, text, id, saved = true, quiet = true)
    }

    companion object {
        const val EXTRA_TEXT = "text"
        const val EXTRA_ID = "id"
    }
}

/** Sets the alarms again after the phone restarts. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.scheduleAll(context)
        }
    }
}
