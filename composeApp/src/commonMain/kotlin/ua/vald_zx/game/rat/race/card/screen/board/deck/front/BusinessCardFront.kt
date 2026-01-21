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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.auction
import rat_race_card.composeapp.generated.resources.buy
import rat_race_card.composeapp.generated.resources.pass
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.AuctionScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.bigBusinessCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.mediumBusinessCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.smallBusinessCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.title
import ua.vald_zx.game.rat.race.card.shared.*


@Composable
fun BoxWithConstraintsScope.SmallBusinessCardFront(
    cardLink: CardLink,
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
        val state by vm.uiState.collectAsState()
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
                    card.price.formatAmount(),
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    card.profit.formatAmount(),
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            val bottomSheetNavigator = LocalBottomSheetNavigator.current
            val state by vm.uiState.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (state.currentPlayerIsActive) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        enabled = state.canPay(card.price) && state.canBuyBusiness(),
                        onClick = {
                            vm.buyBusiness(
                                Business(
                                    type = BusinessType.SMALL,
                                    name = card.name,
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
                if (state.currentPlayerIsActive || state.board.auction != null) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            bottomSheetNavigator.show(
                                AuctionScreen(
                                    vm, Auction.BusinessAuction(
                                        Business(
                                            type = BusinessType.SMALL,
                                            name = cardLink.id.toString(),
                                            price = card.price,
                                            profit = card.profit
                                        ), card.price
                                    )
                                )
                            )
                        },
                        content = {
                            Text(stringResource(Res.string.auction), fontSize = unitTS * 14)
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
                    card.price.formatAmount(),
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    card.profit.formatAmount(),
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
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        enabled = state.canPay(card.price) && state.canBuyBusiness(),
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
                if (state.currentPlayerIsActive || state.board.auction != null) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            bottomSheetNavigator.show(
                                AuctionScreen(
                                    vm, Auction.BusinessAuction(
                                        Business(
                                            type = BusinessType.MEDIUM,
                                            name = cardLink.id.toString(),
                                            price = card.price,
                                            profit = card.profit
                                        ), card.price
                                    )
                                )
                            )
                        },
                        content = {
                            Text(stringResource(Res.string.auction), fontSize = unitTS * 14)
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
                    card.price.formatAmount(),
                    fontSize = unitTS * 12,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    card.profit.formatAmount(),
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
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = { vm.pass() },
                        content = {
                            Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                        },
                    )
                    ElevatedButton(
                        modifier = Modifier,
                        enabled = state.canPay(card.price) && state.canBuyBusiness(),
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
                if (state.currentPlayerIsActive || state.board.auction != null) {
                    ElevatedButton(
                        modifier = Modifier,
                        onClick = {
                            bottomSheetNavigator.show(
                                AuctionScreen(
                                    vm, Auction.BusinessAuction(
                                        Business(
                                            type = BusinessType.LARGE,
                                            name = cardLink.id.toString(),
                                            price = card.price,
                                            profit = card.profit
                                        ), card.price
                                    )
                                )
                            )
                        },
                        content = {
                            Text(stringResource(Res.string.auction), fontSize = unitTS * 14)
                        },
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun CardSmallFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 1), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SmallBusinessCardFront(CardLink(BoardCardType.SmallBusiness, 2), vm)
            }
        }
    }
}


@Preview
@Composable
fun CardMediumFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 1), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.size(300.dp, 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MediumBusinessCardFront(CardLink(BoardCardType.MediumBusiness, 2), vm)
            }
        }
    }
}