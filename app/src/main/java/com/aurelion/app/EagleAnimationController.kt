package com.aurelion.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * Drives AURELION's autonomous behaviour: idle blinking/expression
 * changes, unprompted speech, and (via [withExclusiveControl]) lets other
 * systems — movement, speech, later touch reactions — take the pose over
 * for a while without fighting the idle loop, or each other, for control.
 *
 * Holds no Activity/Context/WindowManager reference, so the same
 * controller + EagleView work identically in the main app preview and
 * inside the floating overlay.
 */
class EagleAnimationController {

    var state by mutableStateOf(EagleState.IDLE)
        private set

    var speechText by mutableStateOf<String?>(null)
        private set

    var isPerformingAction by mutableStateOf(false)
        private set

    private var lastVariation: EagleState? = null
    private val exclusiveControlMutex = Mutex()

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

    /** Call once, from a LaunchedEffect — periodically has the eagle speak
     *  an unprompted line from [EagleDialogue], roughly every 10–30
     *  minutes by default. Shrink the interval constants below for quick
     *  testing. */
    suspend fun runSpeechLoop() {
        while (true) {
            delay(Random.nextLong(SPEECH_MIN_INTERVAL_MS, SPEECH_MAX_INTERVAL_MS))
            say(EagleDialogue.pickLine())
        }
    }

    /** Shows [text] in a speech bubble for [holdMillis], with a matching
     *  talking/calling pose, then clears it. Claims exclusive control so
     *  it doesn't fight the idle loop or autonomous movement. */
    suspend fun say(text: String, holdMillis: Long = 4200) {
        withExclusiveControl {
            try {
                speechText = text
                forceState(if (Random.nextBoolean()) EagleState.TALKING else EagleState.CALLING)
                delay(holdMillis)
            } finally {
                speechText = null
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
     * [run] won't touch it until [action] completes, and any other
     * concurrent caller of this function waits its turn rather than
     * running at the same time (e.g. movement and speech can't collide).
     */
    suspend fun <T> withExclusiveControl(action: suspend () -> T): T {
        return exclusiveControlMutex.withLock {
            isPerformingAction = true
            try {
                action()
            } finally {
                isPerformingAction = false
                state = EagleState.IDLE
            }
        }
    }

    /** Only meant to be called by code already holding exclusive control
     *  (see [withExclusiveControl]). */
    fun forceState(target: EagleState) {
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

    companion object {
        // Spec default: 10–30 minutes between unprompted remarks. Shrink
        // these (e.g. 15_000L to 45_000L) while testing if you don't want
        // to wait around for it to talk.
        private const val SPEECH_MIN_INTERVAL_MS = 15_000L
        private const val SPEECH_MAX_INTERVAL_MS = 45_000L
    }
}
