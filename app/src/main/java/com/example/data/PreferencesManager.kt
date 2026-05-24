package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("skygame_prefs", Context.MODE_PRIVATE)

    var soundOn: Boolean
        get() = prefs.getBoolean("sound_on", true)
        set(value) = prefs.edit().putBoolean("sound_on", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    fun getBestTime(modeId: Int, diffId: String): Float? {
        val key = "best_${modeId}_$diffId"
        if (!prefs.contains(key)) return null
        return prefs.getFloat(key, 0f)
    }

    fun saveBestTime(modeId: Int, diffId: String, time: Float): Boolean {
        val key = "best_${modeId}_$diffId"
        val currentBest = getBestTime(modeId, diffId)
        if (currentBest == null || time < currentBest) {
            prefs.edit().putFloat(key, time).apply()
            return true
        }
        return false
    }
}
