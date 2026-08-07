package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.GameDebrief
import ua.vald_zx.game.rat.race.card.shared.LedgerFlow
import ua.vald_zx.game.rat.race.card.shared.LedgerReason
import ua.vald_zx.game.rat.race.card.shared.NEUTRAL_INDEX_PERCENT

class DebriefScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val debrief by produceState<GameDebrief?>(initialValue = null) {
            value = vm.loadDebrief()
        }
        DebriefContent(debrief)
    }
}

@Composable
internal fun DebriefContent(debrief: GameDebrief?) {
    if (debrief == null) {
        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(Res.string.debrief_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        val finish = debrief.finish
        if (finish == null) {
            Text(stringResource(Res.string.debrief_empty))
            return@Column
        }

        DebriefRow(
            label = stringResource(Res.string.debrief_final_wealth),
            value = finish.total.formatAmount(),
        )
        if (finish.priceIndexPercent != NEUTRAL_INDEX_PERCENT) {
            DebriefRow(
                label = stringResource(Res.string.debrief_real_wealth),
                value = finish.realTotal.formatAmount(),
                hint = stringResource(Res.string.debrief_real_wealth_hint, debrief.inflationLostValue.formatAmount()),
            )
        }
        DebriefRow(
            label = stringResource(Res.string.debrief_asset_share),
            value = "${debrief.assetShareAtFinishPercent}%",
            hint = stringResource(Res.string.debrief_asset_share_hint),
        )
        DebriefRow(
            label = stringResource(Res.string.debrief_interest_paid),
            value = debrief.totalPaidInterest.formatAmount(),
            hint = stringResource(Res.string.debrief_interest_hint),
        )
        DebriefRow(
            label = stringResource(Res.string.debrief_peak_loan),
            value = debrief.peakLoan.formatAmount(),
        )
        debrief.firstNegativeCashFlow?.let { broken ->
            DebriefRow(
                label = stringResource(Res.string.debrief_cash_flow_broke),
                value = broken.cashFlow.formatAmount(),
                hint = stringResource(Res.string.debrief_cash_flow_hint),
            )
        }

        Text(
            text = stringResource(Res.string.debrief_where_money_went),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp),
        )
        val drains = debrief.flows.filter { it.outflow > 0 }
        if (drains.isEmpty()) {
            Text(stringResource(Res.string.debrief_no_drains))
        } else {
            val worst = drains.maxOf { it.outflow }.coerceAtLeast(1)
            drains.forEach { flow -> FlowBar(flow, worst) }
        }
    }
}

@Composable
private fun DebriefRow(label: String, value: String, hint: String? = null) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = Design.scaffold.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun FlowBar(flow: LedgerFlow, worstOutflow: Long) {
    val fraction = (flow.outflow.toFloat() / worstOutflow).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(flow.reason.label(), style = MaterialTheme.typography.bodyMedium)
            Text(
                flow.outflow.formatAmount(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Design.scaffold.surface2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(Design.semantic.negative.edge),
            )
        }
    }
}

@Composable
private fun LedgerReason.label(): String = when (this) {
    LedgerReason.SALARY -> stringResource(Res.string.salary)
    LedgerReason.BUSINESS_PURCHASE -> stringResource(Res.string.business)
    LedgerReason.ASSET_PURCHASE -> stringResource(Res.string.debrief_assets)
    LedgerReason.CONSUMER_PURCHASE -> stringResource(Res.string.shopping)
    LedgerReason.EXPENSE -> stringResource(Res.string.expenses)
    LedgerReason.LOAN_REPAYMENT -> stringResource(Res.string.loan)
    LedgerReason.DEPOSIT -> stringResource(Res.string.deposit)
    LedgerReason.FUND -> stringResource(Res.string.funds)
    LedgerReason.RISK_INVESTMENT -> stringResource(Res.string.debrief_gambling)
    LedgerReason.SCAM -> stringResource(Res.string.scam_offer)
    LedgerReason.MARKET_SALE -> stringResource(Res.string.store)
    LedgerReason.MARKET_CRASH -> stringResource(Res.string.market_crash)
    LedgerReason.TAX_INSPECTION -> stringResource(Res.string.tax_inspection)
    LedgerReason.CORRUPTION -> stringResource(Res.string.deputy)
    LedgerReason.DREAM -> stringResource(Res.string.desire)
    LedgerReason.FAMILY -> stringResource(Res.string.marriage)
    LedgerReason.TRANSFER -> stringResource(Res.string.debrief_transfers)
    LedgerReason.BANKRUPTCY -> stringResource(Res.string.bankruptcy)
    LedgerReason.OTHER -> stringResource(Res.string.debrief_other)
}
