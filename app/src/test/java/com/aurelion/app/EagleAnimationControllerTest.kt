package com.aurelion.app

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EagleAnimationControllerTest {
    @Test
    fun expressionReturnsToIdle() = runBlocking {
        val controller = EagleAnimationController()
        controller.playExpression(EagleState.CURIOUS, holdMillis = 0)
        assertEquals(EagleState.IDLE, controller.state)
        assertFalse(controller.isPerformingAction)
    }

    @Test
    fun manualSleepSurvivesActionCompletion() = runBlocking {
        val controller = EagleAnimationController()
        controller.forceSleep()
        controller.withExclusiveControl { controller.forceState(EagleState.TAKEOFF) }
        assertEquals(EagleState.SLEEPING, controller.state)
        assertTrue(controller.isManuallyAsleep)
    }

    @Test
    fun tapExpressionCannotOverwriteAnExclusiveAction() = runBlocking {
        val controller = EagleAnimationController()
        val entered = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        val action = async {
            controller.withExclusiveControl {
                controller.forceState(EagleState.TAKEOFF)
                entered.complete(Unit)
                finish.await()
            }
        }
        entered.await()
        controller.playExpression(EagleState.CURIOUS, holdMillis = 0)
        assertEquals(EagleState.TAKEOFF, controller.state)
        finish.complete(Unit)
        action.await()
        assertEquals(EagleState.IDLE, controller.state)
    }
}
