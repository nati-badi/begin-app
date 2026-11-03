package com.pushme.app

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Four different feels, so you can tell what happened without looking:
 *  tap = a new message, tick = a light chip or switch, save = a soft double thump,
 *  done = three rising pulses when the 2-minute timer ends.
 */
object Haptics {
    /** A very short, soft vibration (and, if you switched it on, a soft click) when a new message appears. */
    @Suppress("DEPRECATION")
    fun tap(c: Context) {
        if (Store.sounds(c)) {
            val audio = c.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audio?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.4f)
        }
        if (!Store.haptics(c)) return
        val vibrator = c.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(22, 70))
        } else {
            vibrator.vibrate(22)
        }
    }

    /** The lightest touch: choosing a chip or a setting. */
    fun tick(c: Context) = pattern(c, longArrayOf(0, 8), intArrayOf(0, 40))

    /** Saving a message: two soft thumps. */
    fun save(c: Context) = pattern(c, longArrayOf(0, 14, 45, 26), intArrayOf(0, 70, 0, 130))

    /** The timer is finished: three pulses, each a little stronger. */
    fun done(c: Context) = pattern(c, longArrayOf(0, 30, 90, 30, 90, 50), intArrayOf(0, 90, 0, 130, 0, 190))

    @Suppress("DEPRECATION")
    private fun pattern(c: Context, timings: LongArray, amplitudes: IntArray) {
        if (!Store.haptics(c)) return
        val vibrator = c.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(timings, -1)
        }
    }
}
