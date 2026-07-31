package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.fundRateAtSalary
import ua.vald_zx.game.rat.race.card.shared.toLayer
import ua.vald_zx.game.rat.race.card.splitDecimal

class SalaryScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val player = state.player
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val fundRate = remember(player.investmentPosition, player.location.level) {
            player.investmentPosition?.let { position ->
                player.location.level.toLayer().fundRateAtSalary(position)
            }
        }
        LaunchedEffect(player.salaryPosition, player.investmentPosition) {
            if (player.salaryPosition == null && player.investmentPosition == null) {
                bottomSheetNavigator.hide()
            }
        }
        BottomSheetContainer {
            if (player.salaryPosition != null) {
                ElevatedButton(
                    modifier = Modifier.widthIn(min = 220.dp),
                    onClick = {
                        bottomSheetNavigator.hide()
                        vm.takeSalary()
                    },
                ) {
                    Text("${stringResource(Res.string.take_salary)}  ${player.cashFlow().splitDecimal()}")
                }
            }
            if (fundRate != null) {
                Text(
                    text = stringResource(Res.string.investments),
                    style = MaterialTheme.typography.titleMedium,
                )
                HighRiskCard { stake, guess -> vm.playHighRiskInvestment(stake, guess) }
                MediumRiskCard { stake, even -> vm.playMediumRiskInvestment(stake, even) }
                LowRiskCard(rate = fundRate) { amount -> vm.investInFund(amount) }
            }
        }
    }
}

@Composable
private fun InvestmentCard(
    title: String,
    hint: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}

@Composable
internal fun HighRiskCard(onPlay: (stake: Long, guess: Int) -> Unit) {
    val stake = remember { mutableStateOf(TextFieldValue("")) }
    var guess by remember { mutableStateOf<Int?>(null) }
    InvestmentCard(
        title = stringResource(Res.string.high_risk),
        hint = stringResource(Res.string.high_risk_hint),
    ) {
        NumberTextField(input = stake, inputLabel = stringResource(Res.string.stake))
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            (1..6).forEach { number ->
                FilterChip(
                    selected = guess == number,
                    onClick = { guess = number },
                    label = { Text(number.toString()) },
                )
            }
        }
        val amount = stake.value.text.toLongOrNull()
        val selected = guess
        ElevatedButton(
            enabled = amount != null && amount > 0 && selected != null,
            onClick = {
                onPlay(amount ?: return@ElevatedButton, selected ?: return@ElevatedButton)
                stake.value = TextFieldValue("")
                guess = null
            },
        ) { Text(stringResource(Res.string.play)) }
    }
}

@Composable
internal fun MediumRiskCard(onPlay: (stake: Long, even: Boolean) -> Unit) {
    val stake = remember { mutableStateOf(TextFieldValue("")) }
    var even by remember { mutableStateOf<Boolean?>(null) }
    InvestmentCard(
        title = stringResource(Res.string.medium_risk),
        hint = stringResource(Res.string.medium_risk_hint),
    ) {
        NumberTextField(input = stake, inputLabel = stringResource(Res.string.stake))
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = even == true,
                onClick = { even = true },
                label = { Text(stringResource(Res.string.even)) },
            )
            FilterChip(
                selected = even == false,
                onClick = { even = false },
                label = { Text(stringResource(Res.string.odd)) },
            )
        }
        val amount = stake.value.text.toLongOrNull()
        val choice = even
        ElevatedButton(
            enabled = amount != null && amount > 0 && choice != null,
            onClick = {
                onPlay(amount ?: return@ElevatedButton, choice ?: return@ElevatedButton)
                stake.value = TextFieldValue("")
                even = null
            },
        ) { Text(stringResource(Res.string.play)) }
    }
}

@Composable
internal fun LowRiskCard(rate: Long, onInvest: (amount: Long) -> Unit) {
    val amountInput = remember { mutableStateOf(TextFieldValue("")) }
    InvestmentCard(
        title = stringResource(Res.string.low_risk),
        hint = stringResource(Res.string.low_risk_hint, rate.toString()),
    ) {
        NumberTextField(input = amountInput, inputLabel = stringResource(Res.string.amount))
        val amount = amountInput.value.text.toLongOrNull()
        ElevatedButton(
            modifier = Modifier.padding(top = 8.dp),
            enabled = amount != null && amount > 0,
            onClick = {
                onInvest(amount ?: return@ElevatedButton)
                amountInput.value = TextFieldValue("")
            },
        ) { Text(stringResource(Res.string.invest)) }
    }
}
