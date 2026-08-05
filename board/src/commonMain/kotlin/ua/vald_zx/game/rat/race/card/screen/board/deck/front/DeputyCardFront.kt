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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.cards.cardOf
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.DEPUTY_CARD_PRICE
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
fun BoxWithConstraintsScope.DeputyCardFront(
    card: CardLink,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    val deputy = state.board.cardOf(card, locale) as? BoardCard.Deputy

    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = deputy?.name.orEmpty().ifBlank { stringResource(Res.string.deputies) },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(
                glyph = stringResource(Res.string.deputy_short),
                unitTS = unitTS,
                unitDp = unitDp,
                id = card.id,
                glyphSize = 20f,
            )
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = if (deputy?.corrupt == true) {
                stringResource(Res.string.deputy_bought)
            } else {
                deputy?.description.orEmpty()
            },
            fontSize = unitTS * 11,
            lineHeight = unitTS * 15,
        )
        Text(
            modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
            text = stringResource(Res.string.deputies_owned, state.player.deputies),
            fontSize = unitTS * 12,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
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
                    enabled = state.canBuy(DEPUTY_CARD_PRICE),
                    onClick = { vm.buyDeputy() },
                    title = stringResource(
                        Res.string.deputy_buy_more,
                        DEPUTY_CARD_PRICE.splitDecimal(),
                    ),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        }
    }
}
