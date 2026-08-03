package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.CardLink

@Composable
fun BoxWithConstraintsScope.CorruptBusinessCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.CorruptBusiness,
    vm: BoardViewModel,
) {
    CorruptCard(
        cardLink = cardLink,
        vm = vm,
        title = stringResource(Res.string.corrupt_business),
        description = card.description,
        price = card.price,
        deputies = card.deputies,
        gainLabel = if (card.oneTimeProfit > 0) {
            stringResource(Res.string.one_time_profit)
        } else {
            stringResource(Res.string.profit)
        },
        gain = if (card.oneTimeProfit > 0) card.oneTimeProfit else card.profit,
        onBuy = { vm.buyCorrupt(card) },
    )
}

@Composable
fun BoxWithConstraintsScope.CorruptLandCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.CorruptLand,
    vm: BoardViewModel,
) {
    CorruptCard(
        cardLink = cardLink,
        vm = vm,
        title = stringResource(Res.string.corrupt_land),
        description = card.description,
        price = card.price,
        deputies = card.deputies,
        gainLabel = stringResource(Res.string.area),
        gain = card.area,
        gainIsMoney = false,
        onBuy = { vm.buyCorrupt(card) },
    )
}

@Composable
private fun BoxWithConstraintsScope.CorruptCard(
    cardLink: CardLink,
    vm: BoardViewModel,
    title: String,
    description: String,
    price: Long,
    deputies: Int,
    gainLabel: String,
    gain: Long,
    gainIsMoney: Boolean = true,
    onBuy: () -> Unit,
) {
    val state by vm.uiState.collectAsState()
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6

    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(
                glyph = stringResource(Res.string.deputy_short),
                unitTS = unitTS,
                unitDp = unitDp,
                id = cardLink.id,
                glyphSize = 20f,
            )
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = description,
            fontSize = unitTS * 10,
            lineHeight = unitTS * 14,
        )
        CardFigure(stringResource(Res.string.cost), price.formatAmount(), unitTS, smallPadding)
        CardFigure(gainLabel, if (gainIsMoney) gain.formatAmount() else gain.toString(), unitTS, smallPadding)
        CardFigure(
            label = stringResource(Res.string.deputies_required),
            value = stringResource(Res.string.deputies_of, deputies, state.player.deputies),
            unitTS = unitTS,
            topPadding = smallPadding,
        )
        if (state.currentPlayerIsActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.close),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = state.canBuyCorrupt(price, deputies),
                    onClick = onBuy,
                    title = stringResource(Res.string.buy),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        }
    }
}

@Composable
private fun CardFigure(label: String, value: String, unitTS: TextUnit, topPadding: Dp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = topPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = unitTS * 10, textAlign = TextAlign.Start)
        Text(
            text = value,
            fontSize = unitTS * 12,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}
