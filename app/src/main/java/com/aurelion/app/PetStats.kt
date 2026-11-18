package com.aurelion.app

/**
 * AURELION's five pet-care stats, each 0f (empty) to 1f (full). These
 * drift gently over time and respond to feeding/petting/sleeping — never
 * meant to punish the user for being away, just to give the eagle
 * something to react to.
 */
data class PetStats(
    val energy: Float = 0.8f,
    val happiness: Float = 0.7f,
    val affection: Float = 0.5f,
    val curiosity: Float = 0.5f,
    val hunger: Float = 0.2f
) {
    fun clamp() = copy(
        energy = energy.coerceIn(0f, 1f),
        happiness = happiness.coerceIn(0f, 1f),
        affection = affection.coerceIn(0f, 1f),
        curiosity = curiosity.coerceIn(0f, 1f),
        hunger = hunger.coerceIn(0f, 1f)
    )
}
