package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.components.OutlinedBasicTextField
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.design.DesignTextField
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.AuctionScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.cardOf
import ua.vald_zx.game.rat.race.card.screen.design.LocalAuctionPanelToggle
import ua.vald_zx.game.rat.race.card.shared.*

@Composable
fun BoxWithConstraintsScope.ChanceCardFront(
    cardLink: CardLink,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
    remember(cardLink.id, state.board.generatedCards, state.board.generatedTexts, locale) {
        state.board.cardOf(cardLink, locale) as? BoardCard.Chance
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

            is BoardCard.Chance.Scam -> {
                ScamCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.Shares -> {
                SharesCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.CorruptBusiness -> {
                CorruptBusinessCardFront(cardLink, chanceCard, vm)
            }

            is BoardCard.Chance.CorruptLand -> {
                CorruptLandCardFront(cardLink, chanceCard, vm)
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
                text = card.name.ifBlank { stringResource(Res.string.realEstate) },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = "H", unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 20f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
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
    val locale = Locale.current.language
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val auctionPanelToggle = LocalAuctionPanelToggle.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.close),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    kind = DesignButtonKind.Filled,
                    onClick = { vm.buy(card) },
                    title = stringResource(Res.string.buy),
                    enabled = state.canBuy(card.price),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = {
                        val auction = Auction.EstateAuction(
                            Estate(name = card.name, price = card.price),
                            card.price,
                        )
                        if (designV2Enabled.value) auctionPanelToggle(auction)
                        else bottomSheetNavigator.show(AuctionScreen(vm, auction))
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
                text = card.name.ifBlank { "Земля" },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = "З", unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 20f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
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
    val locale = Locale.current.language
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val auctionPanelToggle = LocalAuctionPanelToggle.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.close),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = state.canBuy(card.price),
                    onClick = { vm.buy(card) },
                    title = stringResource(Res.string.buy),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = {
                        val auction = Auction.LandAuction(
                            Land(name = card.name, area = card.area, price = card.price),
                            card.price,
                        )
                        if (designV2Enabled.value) auctionPanelToggle(auction)
                        else bottomSheetNavigator.show(AuctionScreen(vm, auction))
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
                text = card.name.ifBlank { "Випадковий заробіток" },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = "ВЗ", unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 20f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
        )
        Text(
            card.profit.formatAmount(),
            fontSize = unitTS * 15,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = smallPadding).align(Alignment.CenterHorizontally)
        )
        val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
        if (state.currentPlayerIsActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = !state.isProgress,
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
private fun BoxWithConstraintsScope.ScamCardFront(
    cardLink: CardLink,
    card: BoardCard.Chance.Scam,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    val state by vm.uiState.collectAsState()
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = card.name.ifBlank { stringResource(Res.string.scam_offer) },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = "!", unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 20f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
        )
        Row(
            modifier = Modifier.padding(top = smallPadding).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                stringResource(Res.string.scam_stake, card.price.formatAmount()),
                fontSize = unitTS * 12,
            )
            Text(
                stringResource(Res.string.scam_promise, card.promisedReturnPercentage.toString()),
                fontSize = unitTS * 12,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.currentPlayerIsActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = !state.isProgress && state.canBuy(card.price),
                    onClick = { vm.investInScam(card) },
                    title = stringResource(Res.string.scam_invest),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    kind = DesignButtonKind.Tonal,
                    enabled = !state.isProgress,
                    onClick = { vm.declineScam() },
                    title = stringResource(Res.string.scam_decline),
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
    val locale = Locale.current.language
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    val availableCount = state.board.sharesCount ?: card.maxCount
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = card.name.ifBlank { "Акції" },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(
                glyph = state.board.shareTicker(card.sharesType),
                unitTS = unitTS,
                unitDp = unitDp,
                id = cardLink.id,
                glyphSize = 13f,
            )
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
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
                "${
                    state.board.auction?.quantity
                        ?: state.board.sharesCount
                        ?: card.maxCount
                }",
                fontSize = unitTS * 14,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val auctionPanelToggle = LocalAuctionPanelToggle.current
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
            horizontalArrangement = Arrangement.spacedBy(smallPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.currentPlayerIsActive) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = { vm.pass() },
                    title = stringResource(Res.string.close),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                var count by remember(availableCount) { mutableStateOf(0L) }
                val value = if (count <= 0) "" else count.toString()
                if (designV2Enabled.value) {
                    DesignTextField(
                        value = value,
                        onValueChange = { input ->
                            val enteredCount = input.filter(Char::isDigit).toLongOrNull() ?: 0
                            if (enteredCount <= availableCount) count = enteredCount
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = stringResource(Res.string.quantity),
                        fieldHeight = unitDp * 34,
                        textStyle = TextStyle(
                            fontSize = unitTS * 9,
                            fontWeight = FontWeight.Bold,
                        ),
                        contentPadding = PaddingValues(
                            horizontal = unitDp * 4,
                            vertical = unitDp * 4,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    )
                } else {
                    OutlinedBasicTextField(
                        modifier = Modifier.padding(top = smallPadding).weight(1f),
                        value = value,
                        onValueChange = {
                            val enteredCount = it.toLongOrNull() ?: 0
                            if (enteredCount <= availableCount) count = enteredCount
                        },
                        label = {
                            Text(
                                stringResource(Res.string.quantity),
                                fontSize = unitTS * 11,
                            )
                        },
                        contentPadding = contentPadding(
                            top = unitDp * 4,
                            bottom = unitDp * 4,
                        ),
                    )
                }
                EButton(
                    kind = DesignButtonKind.Filled,
                    onClick = { vm.buyShares(card, count) },
                    enabled = count in 1..availableCount && state.canBuy(card.price * count),
                    title = stringResource(Res.string.buy),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
            if (state.currentPlayerIsActive || state.board.auction != null) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = {
                        val auction = Auction.SharesAuction(
                            Shares(
                                type = card.sharesType,
                                count = availableCount,
                                buyPrice = card.price,
                            ),
                            card.price,
                        )
                        if (designV2Enabled.value) auctionPanelToggle(auction)
                        else bottomSheetNavigator.show(AuctionScreen(vm, auction))
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
