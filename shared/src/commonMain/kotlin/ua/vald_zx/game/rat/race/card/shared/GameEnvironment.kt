@file:OptIn(kotlin.time.ExperimentalTime::class)

package ua.vald_zx.game.rat.race.card.shared

import kotlin.random.Random
import kotlin.time.Clock

interface GameRandom {
    fun nextInt(from: Int, until: Int): Int

    fun <T> choose(values: List<T>): T? = values.takeIf { it.isNotEmpty() }
        ?.let { it[nextInt(0, it.size)] }
}

object DefaultGameRandom : GameRandom {
    override fun nextInt(from: Int, until: Int): Int = Random.nextInt(from, until)
}

interface GameClock {
    fun nowEpochMilliseconds(): Long
}

object SystemGameClock : GameClock {
    override fun nowEpochMilliseconds(): Long = Clock.System.now().toEpochMilliseconds()
}
