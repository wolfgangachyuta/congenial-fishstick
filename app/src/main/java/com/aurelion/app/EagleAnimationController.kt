package com.aurelion.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Drives AURELION's autonomous idle behaviour: blinking and brief random
 * expression changes. Holds no Activity/Context/WindowManager reference,
 * so the same controller + EagleView can be reused verbatim inside the
 * floating overlay.
 *
 * Other systems (Phase 3 movement, later speech/touch) that need to take
 * over the eagle's pose for a while — e.g. to show a takeoff animation
 * mid-flight — call [withExclusiveControl] so this class's own idle loop
 * steps aside instead of fighting them for control of [state].
 */
class EagleAnimationController {

    var state by mutableStateOf(EagleState.IDLE)
        private set

    var isPerformingAction by mutableStateOf(false)
        private set

    private var lastVariation: EagleState? = null

    /** Call once, from a LaunchedEffect — runs until its coroutine is cancelled. */
    suspend fun run() {
        while (true) {
            val waitMillis = Random.nextLong(2500, 6000)
            delay(waitMillis)

            if (!isPerformingAction && state == EagleState.IDLE) {
                if (Random.nextFloat() < 0.35f) {
                    playExpression(pickVariation())
                } else {
                    blink()
                }
            }
        }
    }

    /** Briefly shows a pose, then returns to idle. */
    suspend fun playExpression(target: EagleState, holdMillis: Long = 1800) {
        if (isPerformingAction) return
        state = target
        delay(holdMillis)
        state = EagleState.IDLE
    }

    /**
     * Runs [action] with exclusive control of [state]: the idle loop in
     * [run] won't touch it until [action] completes. [action] is free to
     * call [setState] directly as many times as it needs.
     */
    suspend fun <T> withExclusiveControl(action: suspend () -> T): T {
        isPerformingAction = true
        try {
            return action()
        } finally {
            isPerformingAction = false
            state = EagleState.IDLE
        }
    }

    /** Only meant to be called by code already holding exclusive control
     *  (see [withExclusiveControl]). */
    fun setState(target: EagleState) {
        state = target
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
