package ua.vald_zx.game.rat.race.server

import kotlin.random.Random

interface GameRandom {
    fun nextInt(from: Int, until: Int): Int

    fun <T> choose(values: List<T>): T? = values.takeIf { it.isNotEmpty() }
        ?.let { it[nextInt(0, it.size)] }
}

object DefaultGameRandom : GameRandom {
    override fun nextInt(from: Int, until: Int): Int = Random.nextInt(from, until)
}
