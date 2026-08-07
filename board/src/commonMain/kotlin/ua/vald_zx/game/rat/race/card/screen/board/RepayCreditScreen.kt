package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.design.DesignAmountSheet
import ua.vald_zx.game.rat.race.card.shared.Debt
import ua.vald_zx.game.rat.race.card.shared.DebtKind
import ua.vald_zx.game.rat.race.card.shared.avalancheOrder
import ua.vald_zx.game.rat.race.card.shared.balance
import ua.vald_zx.game.rat.race.card.shared.resolvedDebts
import ua.vald_zx.game.rat.race.card.splitDecimal
import kotlin.math.min

class RepayCreditScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val debts = state.player.resolvedDebts()
        if (!designV2Enabled.value) {
            LegacyRepayCreditScreen(vm, state.player, min(state.player.loan, state.player.balance()))
            return
        }
        var selected by remember(debts.size) { mutableStateOf(debts.singleOrNull()?.id) }
        val target = debts.firstOrNull { it.id == selected }
        if (target == null) {
            DebtPicker(debts) { selected = it.id }
            return
        }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val repayable = min(target.principal, state.player.balance())
        val action = stringResource(Res.string.repay_action)
        val tooMuch = stringResource(Res.string.not_enough_money)
        DesignAmountSheet(
            title = stringResource(Res.string.repay_amount),
            available = repayable,
            initial = repayable,
            confirmLabel = { amount -> "$action ${amount.splitDecimal()}" },
            errorFor = { amount -> if (amount > repayable) tooMuch else null },
            onConfirm = { amount ->
                bottomSheetNavigator.hide()
                vm.repayDebt(target.id, amount)
            },
            onCancel = { bottomSheetNavigator.hide() },
        )
    }
}

@Composable
internal fun DebtPicker(debts: List<Debt>, onPick: (Debt) -> Unit) {
    val costliest = debts.avalancheOrder().firstOrNull()
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.debts),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        debts.forEach { debt ->
            DebtRow(debt, isCostliest = debt.id == costliest?.id, onClick = { onPick(debt) })
        }
    }
}

@Composable
private fun DebtRow(debt: Debt, isCostliest: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Design.scaffold.surface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(debt.kind.label(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(Res.string.debt_rate, debt.ratePercent.toString()) +
                        " · −${debt.interestPerPeriod.formatAmount()}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCostliest) Design.semantic.negative.edge else Design.scaffold.onSurfaceMuted,
            )
            if (isCostliest && debt.ratePercent > 0) {
                Text(
                    text = stringResource(Res.string.debt_costliest_first),
                    style = MaterialTheme.typography.labelSmall,
                    color = Design.semantic.negative.edge,
                )
            }
        }
        Text(
            text = debt.principal.formatAmount(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun DebtKind.label(): String = when (this) {
    DebtKind.CREDIT_LINE -> stringResource(Res.string.debt_credit_line)
    DebtKind.PAYDAY -> stringResource(Res.string.debt_payday)
    DebtKind.MORTGAGE -> stringResource(Res.string.debt_mortgage)
    DebtKind.CONSUMER -> stringResource(Res.string.debt_consumer)
}
