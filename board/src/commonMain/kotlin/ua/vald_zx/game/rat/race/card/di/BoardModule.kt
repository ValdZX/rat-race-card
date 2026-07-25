package ua.vald_zx.game.rat.race.card.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

val boardModule = module {
    viewModel { parameters ->
        BoardViewModel(
            board = parameters.get<Board>(),
            player = parameters.get<Player>(),
            serviceProvider = { get() }
        )
    }
}
