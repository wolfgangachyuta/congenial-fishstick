package com.aurelion.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Drives AURELION's autonomous idle behaviour: blinking and brief random
 * expression changes. Holds no Activity/Context/WindowManager reference,
 * so the same controller + EagleView can be reused verbatim once the
 * floating overlay (Phase 2) hosts them instead of MainActivity.
 */
class EagleAnimationController {

    var state by mutableStateOf(EagleState.IDLE)
        private set

    private var lastVariation: EagleState? = null

    /** Call once, from a LaunchedEffect — runs until its coroutine is cancelled. */
    suspend fun run() {
        while (true) {
            val waitMillis = Random.nextLong(2500, 6000)
            delay(waitMillis)

            if (state == EagleState.IDLE) {
                if (Random.nextFloat() < 0.35f) {
                    playExpression(pickVariation())
                } else {
                    blink()
                }
            }
        }
    }

    /** Briefly shows a pose, then returns to idle. Safe to call externally
     *  once touch interactions (Phase 5) need to force a reaction. */
    suspend fun playExpression(target: EagleState, holdMillis: Long = 1800) {
        state = target
        delay(holdMillis)
        state = EagleState.IDLE
    }

    private suspend fun blink() {
        state = EagleState.BLINK
        delay(140)
        state = EagleState.IDLE
    }

    private fun pickVariation(): EagleState {
        val options = EagleState.IDLE_VARIATIONS.filter { it != lastVariation }
        val choice = options.random()
        lastVariation = choice
        return choice
    }
}
