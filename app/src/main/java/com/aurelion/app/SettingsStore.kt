package com.aurelion.app

import android.content.Context

/**
 * Persisted user preferences that tune AURELION's pacing. Values are
 * multipliers (1f = the original spec's default pacing), so the settings
 * UI can offer a simple slider without the person needing to think in
 * milliseconds.
 */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadTalkativeness(): Float = prefs.getFloat(KEY_TALKATIVENESS, 1f)
    fun saveTalkativeness(value: Float) {
        prefs.edit().putFloat(KEY_TALKATIVENESS, value).apply()
    }

    fun loadMovementFrequency(): Float = prefs.getFloat(KEY_MOVEMENT, 1f)
    fun saveMovementFrequency(value: Float) {
        prefs.edit().putFloat(KEY_MOVEMENT, value).apply()
    }

    companion object {
        private const val PREFS_NAME = "aurelion_settings"
        private const val KEY_TALKATIVENESS = "talkativeness"
        private const val KEY_MOVEMENT = "movement_frequency"
    }
}
