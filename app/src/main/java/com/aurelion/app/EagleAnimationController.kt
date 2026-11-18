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
 * Drives AURELION's autonomous behaviour while ensuring that movement,
 * speech and user-triggered expressions cannot overwrite one another.
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
        currentPetStats = stats.clamp()
    }

    /** 1f = spec default pacing; 2f = twice as talkative; 0.5f = half as talkative. */
    fun updateTalkativeness(multiplier: Float) {
        talkativenessMultiplier = sanitizePacingMultiplier(multiplier)
    }

    /** Call once from a LaunchedEffect; runs until its coroutine is cancelled. */
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

            if (isManuallyAsleep || currentPetStats.energy < 0.2f ||
                (isNight && Random.nextFloat() < 0.5f)
            ) {
                if (state != EagleState.SLEEPING) state = EagleState.SLEEPING
                continue
            }

            if (state == EagleState.SLEEPING) {
                state = EagleState.IDLE
                continue
            }
            if (state != EagleState.IDLE) continue

            val variationChance =
                if (currentPetStats.happiness > 0.7f) 0.55f else 0.35f
            when {
                currentPetStats.hunger > 0.75f && Random.nextFloat() < 0.4f ->
                    playExpression(EagleState.CALLING)
                Random.nextFloat() < variationChance ->
                    playExpression(pickVariation())
                else -> blink()
            }
        }
    }

    /** Periodically speaks; night-time speech is intentionally less frequent. */
    suspend fun runSpeechLoop() {
        while (true) {
            val base = Random.nextLong(SPEECH_MIN_INTERVAL_MS, SPEECH_MAX_INTERVAL_MS)
            delay((base / talkativenessMultiplier).toLong())

            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNight = hour < 6 || hour >= 23
            if (isNight && Random.nextFloat() < 0.8f) continue
            say(EagleDialogue.pickLine())
        }
    }

    suspend fun say(text: String, holdMillis: Long = 4200) {
        withExclusiveControl {
            try {
                speechText = text
                forceState(
                    if (Random.nextBoolean()) EagleState.TALKING else EagleState.CALLING
                )
                delay(holdMillis)
            } finally {
                speechText = null
            }
        }
    }

    /**
     * A short, non-queuing expression. If another action owns the eagle,
     * the tap is ignored rather than racing it or delaying behind it.
     */
    suspend fun playExpression(target: EagleState, holdMillis: Long = 1800) {
        if (!exclusiveControlMutex.tryLock()) return
        isPerformingAction = true
        try {
            if (!isManuallyAsleep) {
                state = target
                delay(holdMillis)
            }
        } finally {
            state = restingOrIdleState()
            isPerformingAction = false
            exclusiveControlMutex.unlock()
        }
    }

    fun forceSleep() {
        isManuallyAsleep = true
        state = EagleState.SLEEPING
    }

    fun forceWake() {
        isManuallyAsleep = false
        state = EagleState.IDLE
    }

    /**
     * Serializes longer actions such as speech and flight. The mutex is
     * always released, including when the caller is cancelled.
     */
    suspend fun <T> withExclusiveControl(action: suspend () -> T): T {
        return exclusiveControlMutex.withLock {
            isPerformingAction = true
            try {
                action()
            } finally {
                isPerformingAction = false
                state = restingOrIdleState()
            }
        }
    }

    /** Only call from code already holding exclusive control. */
    fun forceState(target: EagleState) {
        state = target
    }

    private fun restingOrIdleState(): EagleState =
        if (isManuallyAsleep) EagleState.SLEEPING else EagleState.IDLE

    private suspend fun blink() {
        if (!exclusiveControlMutex.tryLock()) return
        isPerformingAction = true
        try {
            if (!isManuallyAsleep) {
                state = EagleState.BLINK
                delay(140)
            }
        } finally {
            state = restingOrIdleState()
            isPerformingAction = false
            exclusiveControlMutex.unlock()
        }
    }

    private fun pickVariation(): EagleState {
        val options = EagleState.IDLE_VARIATIONS.filter { it != lastVariation }
        return options.random().also { lastVariation = it }
    }

    companion object {
        private const val SPEECH_MIN_INTERVAL_MS = 600_000L
        private const val SPEECH_MAX_INTERVAL_MS = 1_800_000L
    }
}
