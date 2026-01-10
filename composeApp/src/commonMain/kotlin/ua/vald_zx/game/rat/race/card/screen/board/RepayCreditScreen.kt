package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.balance
import kotlin.math.min

class RepayCreditScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        InputScreen(
            inputLabel = "Сума погашення",
            buttonText = "Погасити",
            validation = { amount -> amount.isNotEmpty() && state.player.balance() >= amount.toInt() },
            onClick = { amount -> vm.repayLoan(amount = amount.toLong()) },
            value = min(state.player.loan, state.player.balance()).toString()
        )
    }
}