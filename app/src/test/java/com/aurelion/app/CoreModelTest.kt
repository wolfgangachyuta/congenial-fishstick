package com.aurelion.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetStatsTest {

    @Test
    fun clampKeepsEveryStatInsideUnitInterval() {
        val result = PetStats(
            energy = -0.5f,
            happiness = 1.5f,
            affection = 0.25f,
            curiosity = 2f,
            hunger = 0f
        ).clamp()

        assertEquals(0f, result.energy, 0f)
        assertEquals(1f, result.happiness, 0f)
        assertEquals(0.25f, result.affection, 0f)
        assertEquals(1f, result.curiosity, 0f)
        assertEquals(0f, result.hunger, 0f)
    }

    @Test
    fun clampReplacesNonFiniteValuesWithDefaults() {
        val result = PetStats(
            energy = Float.NaN,
            happiness = Float.POSITIVE_INFINITY,
            affection = Float.NEGATIVE_INFINITY,
            curiosity = 0.4f,
            hunger = Float.NaN
        ).clamp()

        assertEquals(0.8f, result.energy, 0f)
        assertEquals(0.7f, result.happiness, 0f)
        assertEquals(0.5f, result.affection, 0f)
        assertEquals(0.4f, result.curiosity, 0f)
        assertEquals(0.2f, result.hunger, 0f)
    }

    @Test
    fun defaultStatsAreValid() {
        val stats = PetStats()
        listOf(
            stats.energy, stats.happiness, stats.affection,
            stats.curiosity, stats.hunger
        ).forEach { assertTrue(it in 0f..1f) }
    }
}

class PacingSettingsTest {

    @Test
    fun pacingValuesAreClampedToSupportedRange() {
        assertEquals(0.5f, sanitizePacingMultiplier(0f), 0f)
        assertEquals(2f, sanitizePacingMultiplier(4f), 0f)
        assertEquals(1f, sanitizePacingMultiplier(1f), 0f)
    }

    @Test
    fun nonFinitePacingValuesFallBackToDefault() {
        assertEquals(1f, sanitizePacingMultiplier(Float.NaN), 0f)
        assertEquals(1f, sanitizePacingMultiplier(Float.POSITIVE_INFINITY), 0f)
    }
}
