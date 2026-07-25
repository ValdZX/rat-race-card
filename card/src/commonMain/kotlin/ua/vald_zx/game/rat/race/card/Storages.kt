package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.Statistics

val raceRate2KStore: KStore<RatRace2CardState>
    get() = getStore("raceRate2.json")
val statistics2KStore: KStore<Statistics>
    get() = getStore("statistics2.json")
