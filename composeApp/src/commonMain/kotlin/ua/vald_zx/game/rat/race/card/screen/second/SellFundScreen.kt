package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.raceRate2store

class SellFundScreen(val fund: Fund) : Screen {
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = "Сума зняття",
            buttonText = "Зняти",
            validation = { amount -> amount.isNotEmpty() && fund.amount >= amount.toLong() },
            onClick = { amount ->
                raceRate2store.dispatch(
                    RatRace2CardAction.FromFund(
                        fund,
                        amount.toLong()
                    )
                )
            },
            value = fund.amount.toString()
        )
    }
}