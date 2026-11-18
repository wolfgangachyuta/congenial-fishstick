package com.aurelion.app

/**
 * Offline dialogue library AURELION draws on for spontaneous, unprompted
 * remarks. Grouped loosely by tone so later phases (e.g. reacting to
 * being touched, or to the time of day) can pull from a specific category
 * instead of the whole pool.
 */
object EagleDialogue {

    val PHILOSOPHICAL = listOf(
        "Have you ever wondered whether freedom is simply the ability to choose your own cage?",
        "The higher one flies, the smaller the world appears.",
        "Perhaps the greatest tragedy of mankind is that it has forgotten how to look at the sky.",
        "I wonder whether birds dream of flying, or simply remember."
    )

    val FUNNY = listOf(
        "You have been staring at that screen for quite some time. Are you hunting something?",
        "I demand a larger perch.",
        "Your thumbs appear to be doing all the work. Curious species, humans.",
        "I could have caught three mice in the time you took to write that message."
    )

    val INTELLECTUAL = listOf(
        "I was contemplating the nature of infinity. Then I saw a pigeon.",
        "Do you suppose Aristotle ever considered the philosophical implications of flight?",
        "Knowledge is a peculiar thing. The more you acquire, the more questions appear."
    )

    val OBSERVATIONS = listOf(
        "Good morning, human.",
        "I believe it is time for a little flight.",
        "Rather quiet today, aren't we?",
        "I have decided that this corner belongs to me."
    )

    private val ALL = PHILOSOPHICAL + FUNNY + INTELLECTUAL + OBSERVATIONS
    private val recentlyUsed = ArrayDeque<String>()
    private const val MEMORY_SIZE = 6

    /** Picks a line not among the last few spoken, so the eagle doesn't
     *  visibly repeat itself in a short span. */
    fun pickLine(): String {
        val options = ALL.filter { it !in recentlyUsed }.ifEmpty { ALL }
        val choice = options.random()
        recentlyUsed.addLast(choice)
        if (recentlyUsed.size > MEMORY_SIZE) recentlyUsed.removeFirst()
        return choice
    }
}
