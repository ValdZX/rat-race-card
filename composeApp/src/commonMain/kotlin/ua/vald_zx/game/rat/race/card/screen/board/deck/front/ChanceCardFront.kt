package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.components.OutlinedBasicTextField
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.AuctionScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.chanceCards
import ua.vald_zx.game.rat.race.card.screen.board.page.label
import ua.vald_zx.game.rat.race.card.shared.*


@Composable
fun BoxWithConstraintsScope.ChanceCardFront(
    cardLink: CardLink,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        chanceCards[cardLink.id]
    }?.let { chanceCard ->
        when (chanceCard) {
            is BoardCard.Chance.Estate -> {
                EstateCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.Land -> {
                LandCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.RandomJob -> {
                RandomJobCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.Shares -> {
                SharesCardFront(cardLink, chanceCard, vm)
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.EstateCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Estate,
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
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.pass),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    onClick = { vm.buy(card) },
                    title = stringResource(Res.string.buy),
                    enabled = state.canPay(card.price),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    onClick = {
                        bottomSheetNavigator.show(
                            AuctionScreen(
                                vm, Auction.EstateAuction(
                                    Estate(
                                        name = card.name,
                                        price = card.price,
                                    ), card.price
                                )
                            )
                        )
                    },
                    title = stringResource(Res.string.auction),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.LandCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Land,
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
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.pass),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    onClick = { vm.buy(card) },
                    title = stringResource(Res.string.buy),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    onClick = {
                        bottomSheetNavigator.show(
                            AuctionScreen(
                                vm, Auction.LandAuction(
                                    Land(
                                        name = card.name,
                                        area = card.area,
                                        price = card.price,
                                    ), card.price
                                )
                            )
                        )
                    },
                    title = stringResource(Res.string.auction),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.RandomJobCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.RandomJob,
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
        val state by vm.uiState.collectAsState()
        if (state.currentPlayerIsActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EButton(
                    onClick = { vm.randomJob(card) },
                    title = stringResource(Res.string.ok),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.SharesCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Shares,
    vm: BoardViewModel
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
                "${state.board.auction?.quantity ?: card.maxCount}",
                fontSize = unitTS * 14,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.spacedBy(smallPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.pass),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                var count by remember { mutableStateOf(0L) }
                val value = if (count <= 0) "" else count.toString()
                OutlinedBasicTextField(
                    modifier = Modifier.padding(top = smallPadding).weight(1f),
                    value = value,
                    onValueChange = {
                        val tapedCount = it.toLongOrNull() ?: 0
                        if (tapedCount <= card.maxCount) {
                            count = tapedCount
                        }
                    },
                    label = {
                        Text(
                            stringResource(Res.string.quantity),
                            fontSize = unitTS * 11
                        )
                    },
                    contentPadding = contentPadding(
                        top = unitDp * 4,
                        bottom = unitDp * 4
                    ),
                )
                EButton(
                    onClick = { vm.buyShares(card, count) },
                    enabled = count > 0 && state.canPay(card.price * count),
                    title = stringResource(Res.string.buy),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    onClick = {
                        bottomSheetNavigator.show(
                            AuctionScreen(
                                vm, Auction.SharesAuction(
                                    Shares(card.sharesType, card.maxCount, card.price), card.price
                                )
                            )
                        )
                    },
                    title = stringResource(Res.string.auction),
                    unitTS = unitTS,
                    unitDp = unitDp,
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
                ChanceCardFront(CardLink(BoardCardType.Chance, 52), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 53), vm)
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
                ChanceCardFront(CardLink(BoardCardType.Chance, 16), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 17), vm)
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
                ChanceCardFront(CardLink(BoardCardType.Chance, 1), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 2), vm)
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
                ChanceCardFront(CardLink(BoardCardType.Chance, 36), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ChanceCardFront(CardLink(BoardCardType.Chance, 37), vm)
            }
        }
    }
}