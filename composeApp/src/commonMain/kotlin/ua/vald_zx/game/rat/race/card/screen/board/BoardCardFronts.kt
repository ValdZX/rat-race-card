package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.buy
import rat_race_card.composeapp.generated.resources.pass
import rat_race_card.composeapp.generated.resources.pay
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.*
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.screen.board.visualize.getLocal
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: CardLink,
    isActive: Boolean,
    size: DpSize,
    modifier: Modifier = Modifier,
    vm: BoardViewModel,
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
                ChanceCardFront(card, isActive, vm)
            }

            BoardCardType.SmallBusiness -> {
                SmallBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.MediumBusiness -> {
                MediumBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.BigBusiness -> {
                BigBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.Expenses -> {
                ExpensesCardFront(card, isActive, vm)
            }

            BoardCardType.EventStore -> {
                EventStoreCardFront(card, isActive, vm)
            }

            BoardCardType.Shopping -> {
                ShoppingCardFront(card, isActive, vm)
            }

            BoardCardType.Deputy -> {
                DeputyCardFront(card, isActive, vm)
            }
        }
    }
}

@Composable
fun BoxScope.ChanceCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    if (isActive) {
        Button("Pass") {
            vm.pass()
        }
    }
}

@Composable
fun BoxWithConstraintsScope.SmallBusinessCardFront(
    cardLink: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        smallBusinessCards[cardLink.id]
    }?.let { card ->
        val density = LocalDensity.current
        val cardWidth = max(maxWidth, 100.dp)
        val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
        val unitDp = cardWidth / 300
        val padding = unitDp * 10
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
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            vm.buyBusiness(
                                Business(
                                    type = BusinessType.SMALL,
                                    name = cardLink.id.toString(),
                                    price = card.price,
                                    profit = card.profit
                                )
                            )
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

@Composable
fun BoxWithConstraintsScope.MediumBusinessCardFront(
    cardLink: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        mediumBusinessCards[cardLink.id]
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
                        text = "СБ",
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
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            vm.buyBusiness(
                                Business(
                                    type = BusinessType.MEDIUM,
                                    name = cardLink.id.toString(),
                                    price = card.price,
                                    profit = card.profit
                                )
                            )
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

@Composable
fun BoxWithConstraintsScope.BigBusinessCardFront(
    cardLink: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        bigBusinessCards[cardLink.id]
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
                        text = "ВБ",
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
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            vm.buyBusiness(
                                Business(
                                    type = BusinessType.LARGE,
                                    name = cardLink.id.toString(),
                                    price = card.price,
                                    profit = card.profit
                                )
                            )
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
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(top = smallPadding),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ElevatedButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            vm.sideExpenses(card.price)
                        },
                        content = {
                            Text(stringResource(Res.string.pay), fontSize = unitTS * 14)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.EventStoreCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    if (isActive) {
        Button("Pass") {
            vm.pass()
        }
    }
}

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
                        text = "ВП",
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
                "Повна вартість",
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                "$${card.price}",
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
                            Text("Пас", fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            vm.buy(card)
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
fun BoxWithConstraintsScope.DeputyCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    if (isActive) {
        Button("Pass") {
            vm.pass()
        }
    }
}
//
//@Preview
//@Composable
//fun CardSmallFrontPreview() {
//    AppTheme {
//        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 1), isActive = false) {}
//            }
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 2), isActive = true) {}
//            }
//        }
//    }
//}
//
//
//@Preview
//@Composable
//fun CardMediumFrontPreview() {
//    AppTheme {
//        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 1), isActive = false) {}
//            }
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 2), isActive = true) {}
//            }
//        }
//    }
//}
//
//@Preview
//@Composable
//fun CardShoppingFrontPreview() {
//    AppTheme {
//        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                ShoppingCardFront(CardLink(BoardCardType.Shopping, 1), isActive = false) {}
//            }
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                ShoppingCardFront(CardLink(BoardCardType.Shopping, 2), isActive = true) {}
//            }
//        }
//    }
//}
//
//@Preview
//@Composable
//fun CardExpensesFrontPreview() {
//    AppTheme {
//        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                ExpensesCardFront(CardLink(BoardCardType.Expenses, 1), isActive = false) {}
//            }
//            BoxWithConstraints(
//                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                ExpensesCardFront(CardLink(BoardCardType.Expenses, 2), isActive = true) {}
//            }
//        }
//    }
//}