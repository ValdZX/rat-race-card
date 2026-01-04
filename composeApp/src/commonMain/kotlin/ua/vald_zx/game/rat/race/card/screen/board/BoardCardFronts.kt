package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.*
import ua.vald_zx.game.rat.race.card.screen.board.page.label
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.screen.board.visualize.getLocal
import ua.vald_zx.game.rat.race.card.shared.*


@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: CardLink,
    isActive: Boolean,
    size: DpSize,
    vm: BoardViewModel,
) {
    val width = maxWidth
    val height = minHeight
    val scaleX = size.width / width
    val rounding = min(width, height) / 16
    BoxWithConstraints(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scaleX,
                scaleY = 1f
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
fun BoxWithConstraintsScope.ChanceCardFront(
    cardLink: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        chanceCards[cardLink.id]
    }?.let { chanceCard ->
        when (chanceCard) {
            is BoardCard.Chance.Estate -> {
                EstateCardFront(cardLink, chanceCard, isActive, vm)
            }

            is BoardCard.Chance.Land -> {
                LandCardFront(cardLink, chanceCard, isActive, vm)
            }

            is BoardCard.Chance.RandomJob -> {
                RandomJobCardFront(cardLink, chanceCard, isActive, vm)
            }

            is BoardCard.Chance.Shares -> {
                SharesCardFront(cardLink, chanceCard, isActive, vm)
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.EstateCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Estate,
    isActive: Boolean,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = stringResource(Res.string.realEstate),
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .size(unitDp * 40),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#${cardLink.id}",
                    color = Color.White,
                    fontSize = unitTS * 10,
                    lineHeight = unitTS * 7,
                    modifier = Modifier.align(Alignment.End)
                )
                Text(
                    text = "H",
                    color = Color.White,
                    fontSize = unitTS * 20,
                    lineHeight = unitTS * 17,
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
                "Вартість:",
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

@Composable
fun BoxWithConstraintsScope.LandCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Land,
    isActive: Boolean,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = "Земля",
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .size(unitDp * 40),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#${cardLink.id}",
                    color = Color.White,
                    fontSize = unitTS * 10,
                    lineHeight = unitTS * 7,
                    modifier = Modifier.align(Alignment.End)
                )
                Text(
                    text = "З",
                    color = Color.White,
                    fontSize = unitTS * 20,
                    lineHeight = unitTS * 17,
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
        ) {
            Text(
                "Вартість:",
                modifier = Modifier.weight(1f),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center
            )
            Text(
                "Площа землі:",
                modifier = Modifier.weight(1f),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center
            )
            Text(
                "Ціна за сотку:",
                modifier = Modifier.weight(1f),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "$${card.price}",
                modifier = Modifier.weight(1f),
                fontSize = unitTS * 12,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${card.area} соток",
                modifier = Modifier.weight(1f),
                fontSize = unitTS * 12,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$${card.price / card.area}",
                modifier = Modifier.weight(1f),
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

@Composable
fun BoxWithConstraintsScope.RandomJobCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.RandomJob,
    isActive: Boolean,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = "Випадковий заробіток",
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .size(unitDp * 40),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#${cardLink.id}",
                    color = Color.White,
                    fontSize = unitTS * 10,
                    lineHeight = unitTS * 7,
                    modifier = Modifier.align(Alignment.End)
                )
                Text(
                    text = "ВЗ",
                    color = Color.White,
                    fontSize = unitTS * 20,
                    lineHeight = unitTS * 17,
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
            "$${card.profit}",
            fontSize = unitTS * 15,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = smallPadding).align(Alignment.CenterHorizontally)
        )
        if (isActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = { vm.randomJob(card) },
                    content = {
                        Text(stringResource(Res.string.ok), fontSize = unitTS * 14)
                    },
                )
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.SharesCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Shares,
    isActive: Boolean,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = "Акції",
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .size(unitDp * 40),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#${cardLink.id}",
                    color = Color.White,
                    fontSize = unitTS * 10,
                    lineHeight = unitTS * 7,
                    modifier = Modifier.align(Alignment.End)
                )
                Text(
                    text = card.sharesType.label(),
                    color = Color.White,
                    fontSize = unitTS * 18,
                    lineHeight = unitTS * 17,
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
                "Вартість:",
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center
            )
            Text(
                "Кількість:",
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
                fontSize = unitTS * 14,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${card.maxCount}",
                fontSize = unitTS * 14,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        if (isActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = { vm.pass() },
                    content = {
                        Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                    },
                )
                var count by remember { mutableStateOf(0L) }
                val value = if (count <= 0) "" else count.toString()
                BasicTextField(
                    value = value,
                    onValueChange = {
                        val tapedCount = it.toLongOrNull() ?: 0
                        if (tapedCount <= card.maxCount) {
                            count = tapedCount
                        }
                    },
                    modifier = Modifier.padding(start = padding).weight(1f),
                    singleLine = true,
                ) { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        contentPadding = contentPadding(top = 0.dp, bottom = 0.dp),
                    )
                }
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        vm.buyShares(card, count)
                    },
                    enabled = count > 0,
                    content = {
                        Text(stringResource(Res.string.buy), fontSize = unitTS * 14)
                    },
                )
            }
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
                Column(
                    modifier = Modifier
                        .background(Color.Black)
                        .size(unitDp * 40),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "#${cardLink.id}",
                        color = Color.White,
                        fontSize = unitTS * 10,
                        lineHeight = unitTS * 7,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = "МБ",
                        color = Color.White,
                        fontSize = unitTS * 20,
                        lineHeight = unitTS * 17,
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
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
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

@Composable
fun BoxWithConstraintsScope.DeputyCardFront(
    card: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    if (isActive) {
        Button(stringResource(Res.string.pass)) {
            vm.pass()
        }
    }
}

@Preview
@Composable
fun CardSmallFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 2), isActive = true, vm)
            }
        }
    }
}


@Preview
@Composable
fun CardMediumFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 2), isActive = true, vm)
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

@Preview
@Composable
fun EstateCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 52), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 53), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun LandCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 16), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 17), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun RandomJobCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 2), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun SharesCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 36), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 37), isActive = true, vm)
            }
        }
    }
}