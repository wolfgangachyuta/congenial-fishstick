package com.aurelion.app

import android.content.Context

/** Loads/saves [PetStats] to a small SharedPreferences file so the
 *  eagle's needs persist across app/overlay restarts. */
class PetStatsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): PetStats = PetStats(
        energy = prefs.getFloat(KEY_ENERGY, 0.8f),
        happiness = prefs.getFloat(KEY_HAPPINESS, 0.7f),
        affection = prefs.getFloat(KEY_AFFECTION, 0.5f),
        curiosity = prefs.getFloat(KEY_CURIOSITY, 0.5f),
        hunger = prefs.getFloat(KEY_HUNGER, 0.2f)
    )

    fun save(stats: PetStats) {
        prefs.edit()
            .putFloat(KEY_ENERGY, stats.energy)
            .putFloat(KEY_HAPPINESS, stats.happiness)
            .putFloat(KEY_AFFECTION, stats.affection)
            .putFloat(KEY_CURIOSITY, stats.curiosity)
            .putFloat(KEY_HUNGER, stats.hunger)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "aurelion_pet_stats"
        private const val KEY_ENERGY = "energy"
        private const val KEY_HAPPINESS = "happiness"
        private const val KEY_AFFECTION = "affection"
        private const val KEY_CURIOSITY = "curiosity"
        private const val KEY_HUNGER = "hunger"
    }
}
