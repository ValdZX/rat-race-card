package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scaleX,
                scaleY = scaleY
            )
            .clip(RoundedCornerShape(rounding))
            .border(2.dp, card.type.color(), RoundedCornerShape(rounding))
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
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
fun BoxScope.SmallBusinessCardFront(
    card: CardLink,
    isActive: Boolean,
    discardCard: () -> Unit,
) {
    remember(card.id) {
        businessCardsLangs[card.id]
    }?.let { card ->
        Column {
            Row {
                Text(
                    text = card.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(Modifier.background(Color.Black).padding(8.dp)) {
                    Text(
                        text = "MБ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = card.description,
                fontSize = 9.sp,
                lineHeight = 9.sp
            )
            Row(
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    "Початкові вкладення:",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Постійний прибуток:",
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    "$${card.price}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$${card.profit}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isActive) {
                Row(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton({ discardCard() }) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null
                        )
                    }
                    IconButton({
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
                    }) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = null
                        )
                    }
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