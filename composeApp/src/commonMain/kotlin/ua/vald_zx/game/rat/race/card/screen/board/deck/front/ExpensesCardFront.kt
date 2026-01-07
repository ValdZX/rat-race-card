package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.not_for_me
import rat_race_card.composeapp.generated.resources.pay
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.expensesCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.needPayExpenses
import ua.vald_zx.game.rat.race.card.screen.board.visualize.getLocal
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.ExpensesCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(card.id) {
        expensesCards[card.id]
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
                    modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                    text = card.description,
                    fontSize = unitTS * 12,
                    lineHeight = unitTS * 10,
                )
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .size(unitDp * 40),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "НВ",
                        color = Color.White,
                        fontSize = unitTS * 20,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                modifier = Modifier.padding(top = padding).fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = card.priceTitle,
                fontSize = unitTS * 14,
                lineHeight = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.padding(top = smallPadding).fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = card.payer.getLocal(),
                fontSize = unitTS * 12,
                lineHeight = unitTS * 10,
            )
            if (isActive) {
                if (vm.uiState.value.player.needPayExpenses(card)) {
                    ElevatedButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            vm.sideExpenses(card.price)
                        },
                        content = {
                            Text(stringResource(Res.string.pay), fontSize = unitTS * 14)
                        },
                    )
                } else {
                    ElevatedButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            vm.pass()
                        },
                        content = {
                            Text(stringResource(Res.string.not_for_me), fontSize = unitTS * 14)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CardExpensesFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ExpensesCardFront(CardLink(BoardCardType.Expenses, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ExpensesCardFront(CardLink(BoardCardType.Expenses, 2), isActive = true, vm)
            }
        }
    }
}