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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.design.DesignTextField
import ua.vald_zx.game.rat.race.card.components.OutlinedBasicTextField
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.cards.cardOf
import ua.vald_zx.game.rat.race.card.screen.board.EstateSelectScreen
import ua.vald_zx.game.rat.race.card.screen.board.SellLandScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.eventStoreCards
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.shareTicker

@Composable
fun BoxWithConstraintsScope.EventStoreCardFront(
    cardLink: CardLink,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
    remember(cardLink.id, state.board.generatedCards, state.board.generatedTexts, locale) {
        state.board.cardOf(cardLink, locale) as? BoardCard.EventStore
    }?.let { eventCard ->
        when (eventCard) {
            is BoardCard.EventStore.Estate -> {
                EstateCardFront(cardLink, eventCard, vm)
            }

            is BoardCard.EventStore.Land -> {
                LandCardFront(cardLink, eventCard, vm)
            }

            is BoardCard.EventStore.Shares -> {
                SharesCardFront(cardLink, eventCard, vm)
            }

            is BoardCard.EventStore.BusinessExtending -> {
                BusinessExtendingCardFront(cardLink, eventCard, vm)
            }

            is BoardCard.EventStore.Reelection -> {
                ReelectionCardFront(cardLink, eventCard, vm)
            }

            is BoardCard.EventStore.Announcement -> {
                AnnouncementCardFront(cardLink, eventCard, vm)
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.EstateCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Estate,
    vm: BoardViewModel
) {
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
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
                stringResource(Res.string.cost),
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
        if (state.board.takenCard != null) {
            val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
            if (state.player.estateList.isNotEmpty() && currentPlayerNotProcessed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    EButton(
                        enabled = !state.isProgress,
                        onClick = { vm.passEstate() },
                        title = stringResource(Res.string.close),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                    EButton(
                        kind = DesignButtonKind.Filled,
                        enabled = !state.isProgress,
                        onClick = { bottomSheetNavigator.show(EstateSelectScreen(vm, card.price)) },
                        title = stringResource(Res.string.sell),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                }
            } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
                EButton(
                    enabled = !state.isProgress,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { vm.passEstate() },
                    title = stringResource(Res.string.close),
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
    card: BoardCard.EventStore.Land,
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
                text = card.name.ifBlank { stringResource(Res.string.land) },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = stringResource(Res.string.land_short), unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 20f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
        )
        Text(
            modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
            text = stringResource(Res.string.priceOfUnit),
            fontSize = unitTS * 10,
            textAlign = TextAlign.Center,
        )
        Text(
            card.price.formatAmount(),
            fontSize = unitTS * 12,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
        if (state.player.landList.isNotEmpty() && currentPlayerNotProcessed) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EButton(
                    enabled = !state.isProgress,
                    onClick = { vm.passLand() },
                    title = stringResource(Res.string.close),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = !state.isProgress,
                    onClick = { bottomSheetNavigator.show(SellLandScreen(vm, card.price)) },
                    title = stringResource(Res.string.sell),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
            EButton(
                enabled = !state.isProgress,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.passLand() },
                title = stringResource(Res.string.close),
                unitTS = unitTS,
                unitDp = unitDp,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.SharesCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Shares,
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
                text = card.name.ifBlank { stringResource(Res.string.shares) },
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
                glyphSize = 15f,
            )
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
        )
        Text(
            modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
            text = stringResource(Res.string.cost),
            fontSize = unitTS * 10,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = card.price.formatAmount(),
            fontSize = unitTS * 14,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        if (card.forcedSale) {
            Text(
                modifier = Modifier.padding(top = smallPadding).fillMaxWidth(),
                text = stringResource(Res.string.forced_share_sale),
                fontSize = unitTS * 11,
                lineHeight = unitTS * 15,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
            )
        }
        val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
        if (state.player.sharesList.any { it.type == card.sharesType } && currentPlayerNotProcessed) {
            val maxCount = state.player.sharesList.filter { it.type == card.sharesType }.sumOf { it.count }
            if (card.forcedSale) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(Res.string.in_stock, maxCount.toString()),
                        fontSize = unitTS * 12,
                    )
                    EButton(
                        kind = DesignButtonKind.Filled,
                        enabled = !state.isProgress,
                        onClick = { vm.sellShares(card, maxCount) },
                        title = stringResource(Res.string.sell_all_shares),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                }
            } else {
                var count by remember { mutableStateOf(0L) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    EButton(
                        enabled = !state.isProgress,
                        onClick = { vm.passShares(card.sharesType) },
                        title = stringResource(Res.string.close),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = padding).weight(1f),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            stringResource(Res.string.in_stock, maxCount.toString()),
                            fontSize = unitTS * 12,
                        )
                        val value = if (count <= 0) "" else count.toString()
                        if (designV2Enabled.value) {
                            DesignTextField(
                                value = value,
                                onValueChange = { input ->
                                    val enteredCount = input.filter(Char::isDigit).toLongOrNull() ?: 0
                                    if (enteredCount <= maxCount) count = enteredCount
                                },
                                modifier = Modifier.padding(top = smallPadding),
                                placeholder = stringResource(Res.string.quantity),
                                fieldHeight = unitDp * 34,
                                contentPadding = PaddingValues(
                                    horizontal = unitDp * 9,
                                    vertical = unitDp * 4,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                            )
                        } else {
                            OutlinedBasicTextField(
                                modifier = Modifier.padding(top = smallPadding),
                                value = value,
                                onValueChange = {
                                    val enteredCount = it.toLongOrNull() ?: 0
                                    if (enteredCount <= maxCount) count = enteredCount
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
                    }
                    EButton(
                        kind = DesignButtonKind.Filled,
                        enabled = !state.isProgress && count > 0,
                        onClick = { vm.sellShares(card, count) },
                        title = stringResource(Res.string.sell),
                        unitTS = unitTS,
                        unitDp = unitDp,
                    )
                }
            }
        } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
            EButton(
                enabled = !state.isProgress,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.passShares(card.sharesType) },
                title = stringResource(Res.string.close),
                unitTS = unitTS,
                unitDp = unitDp,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.BusinessExtendingCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.BusinessExtending,
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
                text = card.name.ifBlank { stringResource(Res.string.business_expansion) },
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(glyph = stringResource(Res.string.business_expansion_short), unitTS = unitTS, unitDp = unitDp, id = cardLink.id, glyphSize = 15f)
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = card.description,
            fontSize = unitTS * 12,
            lineHeight = unitTS * 16,
        )
        val state by vm.uiState.collectAsState()
    val locale = Locale.current.language
        val randomSmallBusiness =
            remember { state.player.businesses.filter { it.type == BusinessType.SMALL }.randomOrNull() }
        if (state.currentPlayerIsActive && randomSmallBusiness != null) {
            Text(
                modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.business_profit_increased_to, randomSmallBusiness.name),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.business_profit_increased),
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = card.profit.formatAmount(),
            fontSize = unitTS * 14,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        if (state.currentPlayerIsActive && randomSmallBusiness != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EButton(
                    kind = DesignButtonKind.Filled,
                    enabled = !state.isProgress,
                    modifier = Modifier,
                    onClick = { vm.extendBusiness(randomSmallBusiness, card) },
                    title = stringResource(Res.string.great),
                    unitTS = unitTS,
                    unitDp = unitDp,
                )
            }
        } else if (state.currentPlayerIsActive) {
            Text(stringResource(Res.string.no_businesses_yet), fontSize = unitTS * 14, modifier = Modifier.padding(smallPadding))
            EButton(
                enabled = !state.isProgress,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.pass() },
                title = stringResource(Res.string.close),
                unitTS = unitTS,
                unitDp = unitDp,
            )
        }
    }
}

@Preview
@Composable
fun EventEstateCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(Color.White).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 27), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 28), vm)
            }
        }
    }
}

@Preview
@Composable
fun EventLandCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(Color.White).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 19), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 20), vm)
            }
        }
    }
}

@Preview
@Composable
fun EventSharesCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(Color.White).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 1), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 2), vm)
            }
        }
    }
}

@Preview
@Composable
fun EventBusinessExtendingPreview() {
    InitPreviewWithVm { vm ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(Color.White).padding(16.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 31), vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 32), vm)
            }
        }
    }
}
