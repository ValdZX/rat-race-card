package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.Fund

class SellFundScreen(val fund: Fund) : Screen {
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = "Сума зняття",
            buttonText = "Зняти",
            validation = { amount -> amount.isNotEmpty() && fund.amount >= amount.toLong() },
            onClick = { amount ->
                TODO()
            },
            value = fund.amount.toString()
        )
    }
}