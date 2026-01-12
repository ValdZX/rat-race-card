package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.shoppingCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.title
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.ShoppingCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {

    remember(card.id) {
        shoppingCards[card.id]
    }?.let { card ->
        val density = LocalDensity.current
        val cardWidth = max(maxWidth, 100.dp)
        val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
        val unitDp = cardWidth / 300
        val padding = unitDp * 12
        val smallPadding = unitDp * 6
        Column(modifier = Modifier.padding(padding)) {
            Row {
                Text(
                    text = card.type.title,
                    modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                    fontSize = unitTS * 14,
                    lineHeight = unitTS * 12,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .size(unitDp * 40),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.vp_short),
                        color = Color.White,
                        fontSize = unitTS * 20,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                modifier = Modifier.padding(top = smallPadding),
                text = card.description,
                fontSize = unitTS * 12,
                lineHeight = unitTS * 10,
            )
            Text(
                stringResource(Res.string.full_price),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                card.price.formatAmount(),
                fontSize = unitTS * 12,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                modifier = Modifier.padding(top = smallPadding),
                text = card.credit,
                fontSize = unitTS * 12,
                lineHeight = unitTS * 10,
            )
            val state by vm.uiState.collectAsState()
            if (isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            vm.pass()
                        },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        enabled = state.canPay(card.price),
                        onClick = {
                            vm.buy(card)
                        },
                        content = {
                            Text(stringResource(Res.string.buy), fontSize = unitTS * 14)
                        },
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun CardShoppingFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ShoppingCardFront(CardLink(BoardCardType.Shopping, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ShoppingCardFront(CardLink(BoardCardType.Shopping, 2), isActive = true, vm)
            }
        }
    }
}