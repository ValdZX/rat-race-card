package ua.vald_zx.game.rat.race.card.di

import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore

val cardModule = module {
    single {
        RatRace2CardStore(getKoin())
    }
}
