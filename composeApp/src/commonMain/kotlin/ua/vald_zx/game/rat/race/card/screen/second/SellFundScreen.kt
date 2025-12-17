package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.withdraw
import rat_race_card.composeapp.generated.resources.withdraw_amount
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.screen.InputScreen

class SellFundScreen(val fund: Fund) : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = stringResource(Res.string.withdraw_amount),
            buttonText = stringResource(Res.string.withdraw),
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