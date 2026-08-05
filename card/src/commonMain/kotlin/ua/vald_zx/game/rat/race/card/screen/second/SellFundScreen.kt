package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.withdraw
import ua.vald_zx.game.rat.race.card.resources.withdraw_amount
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.design.proportionalAmountOptions
import ua.vald_zx.game.rat.race.card.resources.all_in

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
            value = fund.amount.toString(),
            quickOptions = proportionalAmountOptions(fund.amount, stringResource(Res.string.all_in)),
        )
    }
}
