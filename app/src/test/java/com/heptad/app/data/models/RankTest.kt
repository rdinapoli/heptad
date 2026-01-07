package com.heptad.app.data.models

import org.junit.Assert.*
import org.junit.Test

class RankTest {

    @Test
    fun `0 percent returns BEGINNER`() {
        val rank = Rank.fromScorePercentage(0f)
        assertEquals(Rank.BEGINNER, rank)
    }

    @Test
    fun `5 percent returns GOOD_START`() {
        val rank = Rank.fromScorePercentage(0.05f)
        assertEquals(Rank.GOOD_START, rank)
    }

    @Test
    fun `10 percent returns MOVING_UP`() {
        val rank = Rank.fromScorePercentage(0.10f)
        assertEquals(Rank.MOVING_UP, rank)
    }

    @Test
    fun `20 percent returns GOOD`() {
        val rank = Rank.fromScorePercentage(0.20f)
        assertEquals(Rank.GOOD, rank)
    }

    @Test
    fun `30 percent returns SOLID`() {
        val rank = Rank.fromScorePercentage(0.30f)
        assertEquals(Rank.SOLID, rank)
    }

    @Test
    fun `40 percent returns NICE`() {
        val rank = Rank.fromScorePercentage(0.40f)
        assertEquals(Rank.NICE, rank)
    }

    @Test
    fun `50 percent returns GREAT`() {
        val rank = Rank.fromScorePercentage(0.50f)
        assertEquals(Rank.GREAT, rank)
    }

    @Test
    fun `60 percent returns AMAZING`() {
        val rank = Rank.fromScorePercentage(0.60f)
        assertEquals(Rank.AMAZING, rank)
    }

    @Test
    fun `70 percent returns GENIUS`() {
        val rank = Rank.fromScorePercentage(0.70f)
        assertEquals(Rank.GENIUS, rank)
    }

    @Test
    fun `100 percent returns QUEEN_BEE`() {
        val rank = Rank.fromScorePercentage(1.0f)
        assertEquals(Rank.QUEEN_BEE, rank)
    }

    @Test
    fun `fromScore calculates correctly`() {
        val rank = Rank.fromScore(50, 100)
        assertEquals(Rank.GREAT, rank) // 50% = GREAT
    }

    @Test
    fun `fromScore with 0 maxScore returns BEGINNER`() {
        val rank = Rank.fromScore(10, 0)
        assertEquals(Rank.BEGINNER, rank)
    }

    @Test
    fun `getNextRank returns correct next rank`() {
        val nextRank = Rank.getNextRank(Rank.BEGINNER)
        assertEquals(Rank.GOOD_START, nextRank)
    }

    @Test
    fun `getNextRank for QUEEN_BEE returns null`() {
        val nextRank = Rank.getNextRank(Rank.QUEEN_BEE)
        assertNull(nextRank)
    }

    @Test
    fun `boundary values handled correctly`() {
        // Just under 5% should be BEGINNER
        assertEquals(Rank.BEGINNER, Rank.fromScorePercentage(0.04f))

        // Just at 5% should be GOOD_START
        assertEquals(Rank.GOOD_START, Rank.fromScorePercentage(0.05f))

        // Between 5% and 10% should still be GOOD_START
        assertEquals(Rank.GOOD_START, Rank.fromScorePercentage(0.07f))
    }
}
