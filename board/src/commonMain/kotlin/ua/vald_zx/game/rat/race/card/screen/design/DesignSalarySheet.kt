package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.fundRateAtSalary
import ua.vald_zx.game.rat.race.card.shared.toLayer
import ua.vald_zx.game.rat.race.card.splitDecimal

private enum class Game { High, Medium, Low }

@Composable
fun DesignSalarySheet(vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
    val player = state.player
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    var openGame by remember { mutableStateOf<Game?>(null) }
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

    BottomSheetContainer(verticalScrollState = null) {
        when (val game = openGame) {
            null -> Overview(
                salary = player.salaryPosition?.let { player.cashFlow() },
                fundRate = fundRate,
                onTakeSalary = {
                    bottomSheetNavigator.hide()
                    vm.takeSalary()
                },
                onPick = { openGame = it },
            )

            else -> GameForm(
                game = game,
                fundRate = fundRate ?: 0,
                available = player.cash,
                onBack = { openGame = null },
                onPlay = { amount, guess, even ->
                    bottomSheetNavigator.hide()
                    when (game) {
                        Game.High -> vm.playHighRiskInvestment(amount, guess ?: 1)
                        Game.Medium -> vm.playMediumRiskInvestment(amount, even ?: true)
                        Game.Low -> vm.investInFund(amount)
                    }
                },
            )
        }
    }
}

@Composable
private fun ColumnScope.Overview(
    salary: Long?,
    fundRate: Long?,
    onTakeSalary: () -> Unit,
    onPick: (Game) -> Unit,
) {
    val colors = Design.colors
    Text(
        text = stringResource(Res.string.salary),
        style = Design.type.title,
        color = colors.scaffold.onSurface,
    )
    if (salary != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrassToken(stringResource(Res.string.salary), salary)
            DesignButton(
                text = "${stringResource(Res.string.take_salary)} ${salary.splitDecimal()}",
                kind = DesignButtonKind.Brass,
                modifier = Modifier.weight(1f),
                onClick = onTakeSalary,
            )
        }
    }
    if (fundRate != null) {
        DesignSectionTitle(stringResource(Res.string.investments))
        GameTile(
            title = stringResource(Res.string.high_risk),
            hint = stringResource(Res.string.high_risk_hint),
        ) { onPick(Game.High) }
        GameTile(
            title = stringResource(Res.string.medium_risk),
            hint = stringResource(Res.string.medium_risk_hint),
        ) { onPick(Game.Medium) }
        GameTile(
            title = stringResource(Res.string.low_risk),
            hint = stringResource(Res.string.low_risk_hint, fundRate.toString()),
        ) { onPick(Game.Low) }
    }
}

@Composable
private fun GameTile(title: String, hint: String, onClick: () -> Unit) {
    val colors = Design.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .plinth(colors.scaffold.outlineStrong, 3.dp, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .clickableSingle(onClick = onClick)
            .padding(14.dp),
    ) {
        Text(title, style = Design.type.subtitle, color = colors.scaffold.onSurface)
        Text(hint, style = Design.type.body, color = colors.scaffold.onSurfaceMuted)
    }
}

@Composable
private fun ColumnScope.GameForm(
    game: Game,
    fundRate: Long,
    available: Long,
    onBack: () -> Unit,
    onPlay: (amount: Long, guess: Int?, even: Boolean?) -> Unit,
) {
    var guess by remember { mutableStateOf<Int?>(null) }
    var even by remember { mutableStateOf<Boolean?>(null) }
    val ready = when (game) {
        Game.High -> guess != null
        Game.Medium -> even != null
        Game.Low -> true
    }
    val title = when (game) {
        Game.High -> stringResource(Res.string.high_risk)
        Game.Medium -> stringResource(Res.string.medium_risk)
        Game.Low -> stringResource(Res.string.low_risk)
    }
    val subtitle = when (game) {
        Game.High -> stringResource(Res.string.high_risk_hint)
        Game.Medium -> stringResource(Res.string.medium_risk_hint)
        Game.Low -> stringResource(Res.string.low_risk_hint, fundRate.toString())
    }
    val actionWord = when (game) {
        Game.Low -> stringResource(Res.string.invest)
        else -> stringResource(Res.string.play)
    }
    val availableWord = stringResource(Res.string.available)

    DesignAmountForm(
        title = title,
        subtitle = subtitle,
        confirmLabel = { amount -> "$actionWord ${amount.splitDecimal()}" },
        onConfirm = { amount -> onPlay(amount, guess, even) },
        onCancel = onBack,
        cancelLabel = stringResource(Res.string.cancel),
        maxAmount = available,
        maxLabel = stringResource(Res.string.all_in),
        hint = { "$availableWord ${available.splitDecimal()}" },
        validate = { amount -> amount > 0 && ready },
        extraContent = {
            when (game) {
                Game.High -> Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    (1..6).forEach { number ->
                        DesignChip(
                            text = number.toString(),
                            selected = guess == number,
                            modifier = Modifier.weight(1f),
                        ) { guess = number }
                    }
                }

                Game.Medium -> Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    DesignChip(
                        text = stringResource(Res.string.even),
                        selected = even == true,
                        modifier = Modifier.weight(1f),
                    ) { even = true }
                    DesignChip(
                        text = stringResource(Res.string.odd),
                        selected = even == false,
                        modifier = Modifier.weight(1f),
                    ) { even = false }
                }

                Game.Low -> Unit
            }
        },
    )
}
