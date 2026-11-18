package com.aurelion.app

import android.content.Context

/**
 * Persisted user preferences that tune AURELION's pacing.
 * Values are multipliers; 1f is the default speed.
 */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadTalkativeness(): Float =
        sanitizePacingMultiplier(prefs.getFloat(KEY_TALKATIVENESS, 1f))

    fun saveTalkativeness(value: Float) {
        prefs.edit()
            .putFloat(KEY_TALKATIVENESS, sanitizePacingMultiplier(value))
            .apply()
    }

    fun loadMovementFrequency(): Float =
        sanitizePacingMultiplier(prefs.getFloat(KEY_MOVEMENT, 1f))

    fun saveMovementFrequency(value: Float) {
        prefs.edit()
            .putFloat(KEY_MOVEMENT, sanitizePacingMultiplier(value))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "aurelion_settings"
        private const val KEY_TALKATIVENESS = "talkativeness"
        private const val KEY_MOVEMENT = "movement_frequency"
    }
}

internal fun sanitizePacingMultiplier(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0.5f, 2f) else 1f
