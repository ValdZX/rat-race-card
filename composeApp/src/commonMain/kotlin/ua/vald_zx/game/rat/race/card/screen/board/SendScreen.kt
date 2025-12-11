package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.Player

class SendScreen(private val player: Player,private val vm: BoardViewModel) : Screen {
    @OptIn(ExperimentalKStoreApi::class)
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = "Відправити кошти до ${player.card.profession}",
            buttonText = "Відправити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price ->
                vm.sendMoney(player.id, price.toLong())
            },
        )
    }
}