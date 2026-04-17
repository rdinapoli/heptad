package com.heptad.app.data.models

import org.junit.Assert.*
import org.junit.Test

class RankTest {

    @Test
    fun `0 percent returns ADRIFT`() {
        val rank = Rank.fromScorePercentage(0f)
        assertEquals(Rank.ADRIFT, rank)
    }

    @Test
    fun `5 percent returns TELLURIC`() {
        val rank = Rank.fromScorePercentage(0.05f)
        assertEquals(Rank.TELLURIC, rank)
    }

    @Test
    fun `10 percent returns ORBITAL`() {
        val rank = Rank.fromScorePercentage(0.10f)
        assertEquals(Rank.ORBITAL, rank)
    }

    @Test
    fun `20 percent returns SELENIAN`() {
        val rank = Rank.fromScorePercentage(0.20f)
        assertEquals(Rank.SELENIAN, rank)
    }

    @Test
    fun `30 percent returns COMETARY`() {
        val rank = Rank.fromScorePercentage(0.30f)
        assertEquals(Rank.COMETARY, rank)
    }

    @Test
    fun `40 percent returns METEORIC`() {
        val rank = Rank.fromScorePercentage(0.40f)
        assertEquals(Rank.METEORIC, rank)
    }

    @Test
    fun `55 percent returns STELLAR`() {
        val rank = Rank.fromScorePercentage(0.55f)
        assertEquals(Rank.STELLAR, rank)
    }

    @Test
    fun `70 percent returns NEBULAR`() {
        val rank = Rank.fromScorePercentage(0.70f)
        assertEquals(Rank.NEBULAR, rank)
    }

    @Test
    fun `85 percent returns GALACTIC`() {
        val rank = Rank.fromScorePercentage(0.85f)
        assertEquals(Rank.GALACTIC, rank)
    }

    @Test
    fun `100 percent returns UNIVERSAL`() {
        val rank = Rank.fromScorePercentage(1.0f)
        assertEquals(Rank.UNIVERSAL, rank)
    }

    @Test
    fun `fromScore calculates correctly`() {
        val rank = Rank.fromScore(55, 100)
        assertEquals(Rank.STELLAR, rank) // 55% = STELLAR
    }

    @Test
    fun `fromScore with 0 maxScore returns ADRIFT`() {
        val rank = Rank.fromScore(10, 0)
        assertEquals(Rank.ADRIFT, rank)
    }

    @Test
    fun `getNextRank returns correct next rank`() {
        val nextRank = Rank.getNextRank(Rank.ADRIFT)
        assertEquals(Rank.TELLURIC, nextRank)
    }

    @Test
    fun `getNextRank for UNIVERSAL returns null`() {
        val nextRank = Rank.getNextRank(Rank.UNIVERSAL)
        assertNull(nextRank)
    }

    @Test
    fun `boundary values handled correctly`() {
        // Just under 5% should be ADRIFT
        assertEquals(Rank.ADRIFT, Rank.fromScorePercentage(0.04f))

        // Just at 5% should be TELLURIC
        assertEquals(Rank.TELLURIC, Rank.fromScorePercentage(0.05f))

        // Between 5% and 10% should still be TELLURIC
        assertEquals(Rank.TELLURIC, Rank.fromScorePercentage(0.07f))

        // Between 30% and 40% should be COMETARY
        assertEquals(Rank.COMETARY, Rank.fromScorePercentage(0.35f))

        // Between 40% and 55% should be METEORIC
        assertEquals(Rank.METEORIC, Rank.fromScorePercentage(0.50f))
    }
}
