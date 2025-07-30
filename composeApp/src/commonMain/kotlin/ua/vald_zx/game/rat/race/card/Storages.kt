package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.logic.CardState
import ua.vald_zx.game.rat.race.card.logic.CardStatistics
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.Statistics

internal expect inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T>
val raceRate2KStore: KStore<RatRace2CardState>
    get() = getStore("raceRate2.json")
val statistics2KStore: KStore<Statistics>
    get() = getStore("statistics2.json")

fun boardCard(boardId:String): KStore<CardState> = getStore("card_$boardId.json")
fun boardStatistic(boardId:String): KStore<CardStatistics> = getStore("card_stat_$boardId.json")