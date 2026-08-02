package ua.vald_zx.game.rat.race.card.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

val boardModule = module {
    viewModel { parameters ->
        val connection = get<RaceRatConnection>()
        BoardViewModel(
            board = parameters.get<Board>(),
            player = parameters.get<Player>(),
            serviceProvider = connection::service,
            reconnectService = connection::reconnect,
            clientUuidProvider = { appKStore.get()?.clientUuid.orEmpty() },
        )
    }
}
