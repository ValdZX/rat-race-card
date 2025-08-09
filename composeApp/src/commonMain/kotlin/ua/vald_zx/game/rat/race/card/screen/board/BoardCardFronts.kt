package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.CardAction
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.screen.board.cards.businessCardsLangs
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: CardLink,
    isActive: Boolean,
    size: DpSize,
    modifier: Modifier = Modifier,
    discardCard: () -> Unit,
) {
    val width = maxWidth
    val height = maxHeight
    val scaleX = size.width / width
    val scaleY = size.height / height
    val rounding = min(width, height) / 16
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scaleX,
                scaleY = scaleY
            )
            .clip(RoundedCornerShape(rounding))
            .border(2.dp, card.type.color(), RoundedCornerShape(rounding))
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (card.type) {
            BoardCardType.Chance -> {
                ChanceCardFront(card, isActive, discardCard)
            }

            BoardCardType.SmallBusiness -> {
                SmallBusinessCardFront(card, isActive, discardCard)
            }

            BoardCardType.MediumBusiness -> {
                MediumBusinessCardFront(card, isActive, discardCard)
            }

            BoardCardType.BigBusiness -> {
                BigBusinessCardFront(card, isActive, discardCard)
            }

            BoardCardType.Expenses -> {
                ExpensesCardFront(card, isActive, discardCard)
            }

            BoardCardType.EventStore -> {
                EventStoreCardFront(card, isActive, discardCard)
            }

            BoardCardType.Shopping -> {
                ShoppingCardFront(card, isActive, discardCard)
            }

            BoardCardType.Deputy -> {
                DeputyCardFront(card, isActive, discardCard)
            }
        }
    }
}

@Composable
fun BoxScope.ChanceCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxWithConstraintsScope.SmallBusinessCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    remember(card.id) {
        businessCardsLangs[card.id]
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
                    text = card.title,
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
                        text = "MБ",
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
            Row(
                modifier = Modifier.padding(top = smallPadding).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    "Початкові вкладення:",
                    fontSize = unitTS * 10,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Постійний прибуток:",
                    fontSize = unitTS * 10,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    "$${card.price}",
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$${card.profit}",
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = { discardCard() },
                        content = {
                            Text("Пас", fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            raceRate2BoardStore.card.dispatch(
                                CardAction.BuyBusiness(
                                    Business(
                                        type = BusinessType.SMALL,
                                        name = "Name",
                                        price = card.price,
                                        profit = card.profit
                                    )
                                )
                            )
                            discardCard()
                        },
                        content = {
                            Text("Купити", fontSize = unitTS * 14)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.MediumBusinessCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxScope.BigBusinessCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxScope.ExpensesCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Ok") {
            discardCard()
        }
    }
}

@Composable
fun BoxScope.EventStoreCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxScope.ShoppingCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxScope.DeputyCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    if (isActive) {
        Button("Pass") {
            discardCard()
        }
    }
}