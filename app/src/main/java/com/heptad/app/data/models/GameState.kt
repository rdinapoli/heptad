package com.heptad.app.data.models

/**
 * Represents the current state of a game in progress.
 *
 * @property puzzleId The ID of the puzzle being played
 * @property foundWords Set of words the player has found
 * @property currentScore Total points earned
 * @property currentRank Current rank based on score
 * @property startedAt Timestamp when the game started
 * @property lastPlayedAt Timestamp of last activity
 * @property hintState State of the hint panel
 */
data class GameState(
    val puzzleId: String,
    val foundWords: Set<String> = emptySet(),
    val currentScore: Int = 0,
    val currentRank: Rank = Rank.BEGINNER,
    val startedAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val hintState: HintState = HintState()
) {
    /**
     * Number of words found
     */
    val wordsFoundCount: Int
        get() = foundWords.size

    /**
     * Check if a word has already been found
     */
    fun hasFoundWord(word: String): Boolean = word.lowercase() in foundWords

    /**
     * Create a new state with a word added
     */
    fun withFoundWord(word: String, score: Int, newRank: Rank): GameState {
        return copy(
            foundWords = foundWords + word.lowercase(),
            currentScore = currentScore + score,
            currentRank = newRank,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    companion object {
        /**
         * Create a new game state for a puzzle
         */
        fun newGame(puzzleId: String): GameState = GameState(
            puzzleId = puzzleId,
            foundWords = emptySet(),
            currentScore = 0,
            currentRank = Rank.BEGINNER,
            startedAt = System.currentTimeMillis(),
            lastPlayedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Tier levels for the hint system
 */
enum class HintTier(val unlockThreshold: Float, val displayName: String) {
    TWO_LETTER_LIST(0.0f, "Two-Letter List"),
    GRID_VIEW(0.25f, "Grid View"),
    LETTER_REVEAL(0.40f, "Letter Reveal"),
    DEFINITION_HINT(0.60f, "Definition Hint");

    /**
     * Check if this tier is unlocked at the given progress percentage
     */
    fun isUnlocked(progressPercentage: Float): Boolean {
        return progressPercentage >= unlockThreshold
    }

    /**
     * Get unlock threshold as percentage (0-100)
     */
    fun getUnlockPercentage(): Int = (unlockThreshold * 100).toInt()
}

/**
 * State of the hint panel
 */
data class HintState(
    val unlockedTiers: Set<HintTier> = setOf(HintTier.TWO_LETTER_LIST),
    val twoLetterExpanded: Boolean = false,
    val gridViewExpanded: Boolean = false,
    val letterRevealExpanded: Boolean = false,
    val definitionHintExpanded: Boolean = false,
    val letterRevealsUsed: Int = 0,
    val definitionHintsUsed: Int = 0,
    val revealedWords: Set<String> = emptySet(),
    val shownDefinitionWords: Set<String> = emptySet()
) {
    /**
     * Update unlocked tiers based on score progress
     */
    fun withUpdatedUnlocks(currentScore: Int, maxScore: Int, forceUnlockAll: Boolean = false): HintState {
        if (forceUnlockAll) {
            return copy(unlockedTiers = HintTier.entries.toSet())
        }
        if (maxScore == 0) return this
        val progress = currentScore.toFloat() / maxScore.toFloat()
        val newUnlocked = HintTier.entries.filter { it.isUnlocked(progress) }.toSet()
        return copy(unlockedTiers = newUnlocked)
    }

    /**
     * Check if a tier is unlocked
     */
    fun isTierUnlocked(tier: HintTier): Boolean = tier in unlockedTiers

    /**
     * Toggle expansion state of a tier
     */
    fun toggleTier(tier: HintTier): HintState {
        return when (tier) {
            HintTier.TWO_LETTER_LIST -> copy(twoLetterExpanded = !twoLetterExpanded)
            HintTier.GRID_VIEW -> copy(gridViewExpanded = !gridViewExpanded)
            HintTier.LETTER_REVEAL -> copy(letterRevealExpanded = !letterRevealExpanded)
            HintTier.DEFINITION_HINT -> copy(definitionHintExpanded = !definitionHintExpanded)
        }
    }
}
