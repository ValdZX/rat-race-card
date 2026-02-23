package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.shared.Auction

class AuctionScreen(private val vm: BoardViewModel, private val auction: Auction) : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val state by vm.uiState.collectAsState()
        LaunchedEffect(state.board.takenCard) {
            if (state.board.takenCard == null) {
                bottomSheetNavigator.hide()
            }
        }
        val auction = state.board.auction ?: auction
        val minBid = if (state.board.bidList.isEmpty()) {
            auction.getBid
        } else {
            state.board.bidList.maxBy { it.bid }.bid
        }
        BottomSheetContainer(verticalScrollState = null) {
            if (state.board.auction == null) {
                val firstBidState = remember {
                    mutableStateOf(TextFieldValue(minBid.toString()))
                }
                NumberTextField(
                    input = firstBidState,
                    inputLabel = stringResource(Res.string.firstBid),
                )
                ElevatedButton(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        vm.advertiseAuction(auction.copy(bid = firstBidState.value.text.toLong()))
                    },
                    enabled = firstBidState.value.text.isNotEmpty() && firstBidState.value.text.toLong() >= minBid,
                    content = {
                        Text(stringResource(Res.string.advertiseAuction))
                    }
                )
            } else {
                if (state.board.bidList.isEmpty()) {
                    Text(stringResource(Res.string.noBetsYet), style = MaterialTheme.typography.titleSmall)
                    Text("Мінімальна ставка ${auction.getBid}", style = MaterialTheme.typography.titleSmall)
                } else {
                    LazyColumn {
                        items(state.board.bidList) { item ->
                            Row {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(8.dp)
                                ) {
                                    val playerName = remember(item.playerId) {
                                        players.value.find { it.id == item.playerId }?.card?.name.orEmpty()
                                    }
                                    DetailsField(stringResource(Res.string.player_name), playerName)
                                    if (item.count > 0) {
                                        DetailsField(stringResource(Res.string.quantity), item.count.toString())
                                    }
                                    DetailsField(stringResource(Res.string.bid), item.bid.formatAmount())
                                    DetailsField(
                                        stringResource(Res.string.profit),
                                        auction.getProfit(item).formatAmount()
                                    )
                                }
                                if (state.currentPlayerIsActive) {
                                    IconButton(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        onClick = {
                                            vm.sellBid(item)
                                            bottomSheetNavigator.hide()
                                        },
                                        content = {
                                            Icon(Images.Send, contentDescription = null)
                                        },
                                        enabled = auction !is Auction.SharesAuction || auction.shares.count >= item.count
                                    )
                                }
                            }
                        }
                    }
                }
                if (!state.currentPlayerIsActive && state.canMakeBid()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val bidState = remember {
                            mutableStateOf(TextFieldValue(minBid.toString()))
                        }
                        val quantityState = remember {
                            mutableStateOf(TextFieldValue(""))
                        }
                        NumberTextField(
                            input = bidState,
                            inputLabel = stringResource(Res.string.bid),
                            modifier = Modifier.weight(1f),
                        )
                        val isShares = auction is Auction.SharesAuction
                        if (isShares) {
                            NumberTextField(
                                input = quantityState,
                                inputLabel = stringResource(Res.string.quantity),
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                            )
                        }
                        val bid = bidState.value.text.toLongOrNull() ?: 0
                        val count = quantityState.value.text.toLongOrNull() ?: 0
                        ElevatedButton(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .widthIn(min = 200.dp),
                            onClick = {
                                vm.makeBid(bid, count)
                            },
                            enabled = isShares && state.canPay(bid * count) || bidState.value.text.isNotEmpty() && bid >= minBid && state.canPay(bid),
                            content = {
                                Text(stringResource(Res.string.placeBet))
                            }
                        )
                    }
                }
            }
        }
    }
}