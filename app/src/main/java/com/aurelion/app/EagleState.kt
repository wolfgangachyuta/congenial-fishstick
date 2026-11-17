package com.aurelion.app

/**
 * Every pose AURELION can show right now, each backed by one of the
 * hand-illustrated sprites in res/drawable-nodpi/. Later phases (movement,
 * speech, touch) decide *when* a state is active; this file only defines
 * what each one looks like.
 */
enum class EagleState(val drawableRes: Int) {
    IDLE(R.drawable.idle_front),
    BLINK(R.drawable.blink),
    LOOK_LEFT(R.drawable.look_left),
    LOOK_RIGHT(R.drawable.look_right),
    CURIOUS(R.drawable.curious_tilt),
    TALKING(R.drawable.talking),
    CALLING(R.drawable.calling),
    ANNOYED(R.drawable.grumpy),
    SURPRISED(R.drawable.surprised),
    RESTING(R.drawable.head_down),
    SLEEPING(R.drawable.sleeping_curled),
    PREENING(R.drawable.preening),
    SCRATCHING(R.drawable.scratching),
    STRETCHING(R.drawable.wings_spread),
    EXCITED(R.drawable.wing_flap),
    TAKEOFF(R.drawable.takeoff);

    companion object {
        /** States used for idle, autonomous expression variation. */
        val IDLE_VARIATIONS = listOf(
            LOOK_LEFT, LOOK_RIGHT, CURIOUS, ANNOYED, SURPRISED, PREENING, SCRATCHING, STRETCHING
        )
    }
}
