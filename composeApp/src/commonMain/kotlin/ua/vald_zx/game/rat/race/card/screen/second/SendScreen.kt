package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
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
                raceRate2store.dispatch(
                    action = RatRace2CardAction.SendCash(
                        player.id,
                        price.toLong()
                    )
                )
            },
        )
    }
}