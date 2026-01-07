package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.chanceCards
import ua.vald_zx.game.rat.race.card.screen.board.page.label
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink


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
private fun BoxWithConstraintsScope.EstateCardFront(
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
                card.price.formatAmount(),
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
private fun BoxWithConstraintsScope.LandCardFront(
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
                card.price.formatAmount(),
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
                (card.price / card.area).formatAmount(),
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
private fun BoxWithConstraintsScope.RandomJobCardFront(
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
            card.profit.formatAmount(),
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
private fun BoxWithConstraintsScope.SharesCardFront(
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
                    fontSize = unitTS * 15,
                    lineHeight = unitTS * 15,
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
                card.price.formatAmount(),
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
                        contentPadding = contentPadding(top = unitDp * 4, bottom = unitDp * 4),
                        label = { Text(stringResource(Res.string.quantity), fontSize = unitTS * 14) },
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

@Preview
@Composable
fun ChanceEstateCardFrontPreview() {
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
fun ChanceLandCardFrontPreview() {
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
fun ChanceRandomJobCardFrontPreview() {
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
fun ChanceSharesCardFrontPreview() {
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