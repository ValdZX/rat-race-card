package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.Player

class SendScreen(private val player: Player) : Screen {
    @OptIn(ExperimentalKStoreApi::class)
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = "Відправити кошти до ${player.playerCard.profession}",
            buttonText = "Відправити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price ->
                raceRate2BoardStore.dispatch(
                    BoardAction.SendCash(
                        player.id,
                        price.toLong()
                    )
                )
            },
        )
    }
}