package com.example.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var toneGen: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playOk(enabled: Boolean) {
        if (!enabled) return
        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        vibrate(20)
    }

    fun playErr(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
            vibrate(40)
            delay(60)
            vibrate(40)
        }
    }

    fun playWin(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
            vibrate(30)
            delay(150)
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
            vibrate(50)
            delay(150)
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            vibrate(100)
        }
    }

    fun playFail(enabled: Boolean) {
        if (!enabled) return
        toneGen?.startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 600)
        vibrate(300)
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        toneGen?.release()
    }
}
