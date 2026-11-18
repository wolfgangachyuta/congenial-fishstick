package com.aurelion.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import kotlin.random.Random

/**
 * Drives AURELION's autonomous behaviour: idle blinking/expression
 * changes (influenced by [PetStats]), unprompted speech (now also aware
 * of time of day), and (via [withExclusiveControl]) lets other systems —
 * movement, speech, touch reactions — take the pose over for a while
 * without fighting the idle loop, or each other, for control.
 */
class EagleAnimationController {

    var state by mutableStateOf(EagleState.IDLE)
        private set

    var speechText by mutableStateOf<String?>(null)
        private set

    var isPerformingAction by mutableStateOf(false)
        private set

    var isManuallyAsleep by mutableStateOf(false)
        private set

    private var currentPetStats: PetStats = PetStats()
    private var talkativenessMultiplier: Float = 1f
    private var lastVariation: EagleState? = null
    private var lastGreetedDayOfYear: Int = -1
    private val exclusiveControlMutex = Mutex()

    fun updatePetStats(stats: PetStats) {
        currentPetStats = stats
    }

    /** 1f = spec default pacing; 2f = roughly twice as talkative; 0.5f =
     *  roughly half as talkative. */
    fun updateTalkativeness(multiplier: Float) {
        talkativenessMultiplier = multiplier
    }

    /** Call once, from a LaunchedEffect — runs until its coroutine is cancelled. */
    suspend fun run() {
        while (true) {
            delay(Random.nextLong(2500, 6000))

            if (isPerformingAction) continue

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val isNight = hour < 6 || hour >= 23

            if (hour in 6..9 && dayOfYear != lastGreetedDayOfYear) {
                lastGreetedDayOfYear = dayOfYear
                say("Good morning, human.")
                continue
            }

            if (isManuallyAsleep || currentPetStats.energy < 0.2f || (isNight && Random.nextFloat() < 0.5f)) {
                if (state != EagleState.SLEEPING) state = EagleState.SLEEPING
                continue
            }

            if (state == EagleState.SLEEPING) {
                state = EagleState.IDLE
                continue
            }

            if (state != EagleState.IDLE) continue

            val variationChance = if (currentPetStats.happiness > 0.7f) 0.55f else 0.35f
            when {
                currentPetStats.hunger > 0.75f && Random.nextFloat() < 0.4f ->
                    playExpression(EagleState.CALLING)
                Random.nextFloat() < variationChance ->
                    playExpression(pickVariation())
                else ->
                    blink()
            }
        }
    }

    /** Call once, from a LaunchedEffect — periodically has the eagle speak
     *  an unprompted line from [EagleDialogue]. Mostly stays quiet at
     *  night, and speaks more or less often depending on
     *  [updateTalkativeness]. */
    suspend fun runSpeechLoop() {
        while (true) {
            val base = Random.nextLong(SPEECH_MIN_INTERVAL_MS, SPEECH_MAX_INTERVAL_MS)
            val adjusted = (base / talkativenessMultiplier.coerceAtLeast(0.1f)).toLong()
            delay(adjusted)

            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNight = hour < 6 || hour >= 23
            if (isNight && Random.nextFloat() < 0.8f) continue

            say(EagleDialogue.pickLine())
        }
    }

    /** Shows [text] in a speech bubble for [holdMillis], with a matching
     *  talking/calling pose, then clears it. */
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

    /** User-requested sleep from the interaction menu — holds until [forceWake]. */
    fun forceSleep() {
        isManuallyAsleep = true
        state = EagleState.SLEEPING
    }

    /** Cancels [forceSleep]. */
    fun forceWake() {
        isManuallyAsleep = false
        state = EagleState.IDLE
    }

    /**
     * Runs [action] with exclusive control of [state]: the idle loop in
     * [run] won't touch it until [action] completes, and any other
     * concurrent caller of this function waits its turn rather than
     * running at the same time.
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
        private const val SPEECH_MIN_INTERVAL_MS = 600_000L
        private const val SPEECH_MAX_INTERVAL_MS = 1_800_000L
    }
}
