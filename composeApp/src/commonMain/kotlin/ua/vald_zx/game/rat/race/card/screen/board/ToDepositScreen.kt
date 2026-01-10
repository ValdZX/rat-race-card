package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.InputScreen

class ToDepositScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        InputScreen(
            inputLabel = "Сума вкладу",
            buttonText = "Вкласти",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> vm.toDeposit(amount = amount.toLong()) },
            value = state.player.cash.toString()
        )
    }
}
