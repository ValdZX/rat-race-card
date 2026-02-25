package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.not_for_me
import rat_race_card.composeapp.generated.resources.pay
import ua.vald_zx.game.rat.race.card.components.EButton
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
            val state by vm.uiState.collectAsState()
            if (state.currentPlayerIsActive) {
                if (vm.uiState.value.player.needPayExpenses(card)) {
                    EButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { vm.sideExpenses(card.price) },
                        title = stringResource(Res.string.pay),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                } else {
                    EButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { vm.pass() },
                        title = stringResource(Res.string.not_for_me),
                        unitTS = unitTS,
                        unitDp = unitDp,
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
                ExpensesCardFront(CardLink(BoardCardType.Expenses, 1), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ExpensesCardFront(CardLink(BoardCardType.Expenses, 2), vm)
            }
        }
    }
}