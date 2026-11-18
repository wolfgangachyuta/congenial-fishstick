package com.aurelion.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Owns AURELION's five pet-care stats: simulates their slow drift over
 * time, applies the effect of user actions (feed/pet), and persists them
 * so they survive an app/overlay restart.
 */
class PetStatsController(context: Context) {

    private val store = PetStatsStore(context.applicationContext)

    var stats by mutableStateOf(store.load())
        private set

    /** Set by the UI layer to true while the eagle is asleep, so energy
     *  regenerates instead of draining. */
    var isResting: Boolean = false

    /** Call once, from a LaunchedEffect — simulates gentle drift and saves
     *  periodically until its coroutine is cancelled. */
    suspend fun run() {
        while (true) {
            delay(TICK_MILLIS)
            stats = stats.copy(
                energy = if (isResting) {
                    (stats.energy + ENERGY_RESTORE_PER_TICK).coerceAtMost(1f)
                } else {
                    (stats.energy - ENERGY_DECAY_PER_TICK).coerceAtLeast(0f)
                },
                happiness = (stats.happiness - HAPPINESS_DECAY_PER_TICK).coerceAtLeast(0f),
                affection = (stats.affection - AFFECTION_DECAY_PER_TICK).coerceAtLeast(0f),
                curiosity = (stats.curiosity + CURIOSITY_GROWTH_PER_TICK).coerceAtMost(1f),
                hunger = (stats.hunger + HUNGER_GROWTH_PER_TICK).coerceAtMost(1f)
            ).clamp()
            store.save(stats)
        }
    }

    fun feed() {
        stats = stats.copy(
            hunger = (stats.hunger - 0.45f).coerceAtLeast(0f),
            happiness = (stats.happiness + 0.15f).coerceAtMost(1f),
            affection = (stats.affection + 0.05f).coerceAtMost(1f)
        ).clamp()
        store.save(stats)
    }

    fun pet() {
        stats = stats.copy(
            affection = (stats.affection + 0.2f).coerceAtMost(1f),
            happiness = (stats.happiness + 0.1f).coerceAtMost(1f)
        ).clamp()
        store.save(stats)
    }

    companion object {
        private const val TICK_MILLIS = 60_000L // one tick per minute
        private const val ENERGY_DECAY_PER_TICK = 0.01f
        private const val ENERGY_RESTORE_PER_TICK = 0.03f
        private const val HAPPINESS_DECAY_PER_TICK = 0.006f
        private const val AFFECTION_DECAY_PER_TICK = 0.004f
        private const val CURIOSITY_GROWTH_PER_TICK = 0.012f
        private const val HUNGER_GROWTH_PER_TICK = 0.01f
    }
}
