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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.EstateSelectScreen
import ua.vald_zx.game.rat.race.card.screen.board.SellLandScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.eventStoreCards
import ua.vald_zx.game.rat.race.card.screen.board.page.label
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.EventStoreCardFront(
    cardLink: CardLink,
    isActive: Boolean,
    vm: BoardViewModel,
) {
    remember(cardLink.id) {
        eventStoreCards[cardLink.id]
    }?.let { eventCard ->
        when (eventCard) {
            is BoardCard.EventStore.Estate -> {
                EstateCardFront(cardLink, eventCard, isActive, vm)
            }

            is BoardCard.EventStore.Land -> {
                LandCardFront(cardLink, eventCard, isActive, vm)
            }

            is BoardCard.EventStore.Shares -> {
                SharesCardFront(cardLink, eventCard, isActive, vm)
            }

            is BoardCard.EventStore.BusinessExtending -> {
                BusinessExtendingCardFront(cardLink, eventCard, isActive, vm)
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.EstateCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Estate,
    isActive: Boolean,
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
        val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
        if (state.player.estateList.isNotEmpty() && currentPlayerNotProcessed) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = { vm.passEstate() },
                    content = {
                        Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                    },
                )
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        bottomSheetNavigator.show(EstateSelectScreen(vm, card.price))
                    },
                    content = {
                        Text(stringResource(Res.string.sell), fontSize = unitTS * 14)
                    },
                )
            }
        } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
            ElevatedButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.passEstate() },
                content = {
                    Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                },
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.LandCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Land,
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
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
        if (state.player.landList.isNotEmpty() && currentPlayerNotProcessed) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = { vm.passLand() },
                    content = {
                        Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                    },
                )
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        bottomSheetNavigator.show(SellLandScreen(vm, card.price))
                    },
                    content = {
                        Text(stringResource(Res.string.sell), fontSize = unitTS * 14)
                    },
                )
            }
        } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
            ElevatedButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.passEstate() },
                content = {
                    Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                },
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.SharesCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Shares,
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
        Text(
            modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
            text = "Вартість:",
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
        val state by vm.uiState.collectAsState()
        val currentPlayerNotProcessed = !state.board.processedPlayerIds.contains(state.player.id)
        if (state.player.sharesList.any { it.type == card.sharesType } && currentPlayerNotProcessed) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = { vm.passShares(card.sharesType) },
                    content = {
                        Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                    },
                )
                var count by remember { mutableStateOf(0L) }
                Column(
                    modifier = Modifier.padding(horizontal = padding).weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    val maxCount = state.player.sharesList.filter { it.type == card.sharesType }.sumOf { it.count }
                    Text(
                        "В наявності: $maxCount",
                        fontSize = unitTS * 12,
                    )
                    val value = if (count <= 0) "" else count.toString()
                    BasicTextField(
                        modifier = Modifier.padding(top = smallPadding),
                        value = value,
                        onValueChange = {
                            val tapedCount = it.toLongOrNull() ?: 0
                            if (tapedCount <= maxCount) {
                                count = tapedCount
                            }
                        },
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
                            label = { Text(stringResource(Res.string.quantity), fontSize = unitTS * 12) },
                        )
                    }
                }
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        vm.sellShares(card, count)
                    },
                    content = {
                        Text(stringResource(Res.string.sell), fontSize = unitTS * 14)
                    },
                )
            }
        } else if (state.currentPlayerIsActive && currentPlayerNotProcessed) {
            ElevatedButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { vm.passEstate() },
                content = {
                    Text(stringResource(Res.string.pass), fontSize = unitTS * 14)
                },
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.BusinessExtendingCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.BusinessExtending,
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
                text = "Розширення бізнесу",
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
                    text = "РБ",
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
        val state by vm.uiState.collectAsState()
        val randomSmallBusiness =
            remember { state.player.businesses.filter { it.type == BusinessType.SMALL }.randomOrNull() }
        if (isActive && randomSmallBusiness != null) {
            Text(
                modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
                text = "Прибуток бізнесу ${randomSmallBusiness.name} виріс на:",
                fontSize = unitTS * 10,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
                text = "Прибуток бізнесу виріс на:",
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
        if (isActive && randomSmallBusiness != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = smallPadding),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        vm.extendBusiness(randomSmallBusiness, card)
                    },
                    content = {
                        Text("Чудово!", fontSize = unitTS * 14)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun EventEstateCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 27), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 28), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun EventLandCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 19), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 20), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun EventSharesCardFrontPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 1), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 2), isActive = true, vm)
            }
        }
    }
}

@Preview
@Composable
fun EventBusinessExtendingPreview() {
    InitPreviewWithVm { vm ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 31), isActive = false, vm)
            }
            BoxWithConstraints(
                modifier = Modifier.width(300.dp).heightIn(min = 200.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                EventStoreCardFront(CardLink(BoardCardType.EventStore, 32), isActive = true, vm)
            }
        }
    }
}