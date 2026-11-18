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
        energy = energy.finiteUnitOr(0.8f),
        happiness = happiness.finiteUnitOr(0.7f),
        affection = affection.finiteUnitOr(0.5f),
        curiosity = curiosity.finiteUnitOr(0.5f),
        hunger = hunger.finiteUnitOr(0.2f)
    )
}

private fun Float.finiteUnitOr(fallback: Float): Float =
    if (isFinite()) coerceIn(0f, 1f) else fallback
