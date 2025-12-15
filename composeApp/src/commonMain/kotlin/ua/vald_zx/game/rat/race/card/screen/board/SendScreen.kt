package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
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
            inputLabel = "Відправити кошти до $playerName",
            buttonText = "Відправити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price ->
                sendMoney(playerId, price.toLong())
            },
        )
    }
}