package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.screen.InputScreen

class SendScreen(
    private val playerId: String,
    private val playerName: String,
    private val sendMoney: (String, Long) -> Unit,
) :
    Screen {
    @OptIn(ExperimentalKStoreApi::class)
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = stringResource(Res.string.send_money_to, playerName),
            buttonText = stringResource(Res.string.send),
            validation = { price -> price.isNotEmpty() },
            onClick = { price ->
                sendMoney(playerId, price.toLong())
            },
        )
    }
}