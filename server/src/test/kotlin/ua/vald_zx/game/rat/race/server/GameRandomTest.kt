package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.DefaultGameRandom
import ua.vald_zx.game.rat.race.card.shared.GameRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameRandomTest {
    @Test
    fun controlledRandomSelectsTheRequestedDiceAndListValues() {
        val random = SequenceGameRandom(5, 2)

        assertEquals(6, random.nextInt(1, 7))
        assertEquals("third", random.choose(listOf("first", "second", "third")))
        assertNull(random.choose(emptyList<String>()))
    }

    @Test
    fun defaultRandomAlwaysHonorsTheRequestedBounds() {
        repeat(1_000) {
            assertEquals(true, DefaultGameRandom.nextInt(1, 7) in 1..6)
        }
    }

    private class SequenceGameRandom(vararg values: Int) : GameRandom {
        private val iterator = values.iterator()

        override fun nextInt(from: Int, until: Int): Int = from + iterator.nextInt()
    }
}
