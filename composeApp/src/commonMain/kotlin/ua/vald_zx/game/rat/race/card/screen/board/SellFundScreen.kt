package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.Fund

class SellFundScreen(val fund: Fund) : Screen {
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = stringResource(Res.string.withdraw_amount),
            buttonText = stringResource(Res.string.withdraw),
            validation = { amount -> amount.isNotEmpty() && fund.amount >= amount.toLong() },
            onClick = { amount ->
                TODO()
            },
            value = fund.amount.toString()
        )
    }
}