package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
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
                                    DetailsField(stringResource(Res.string.bid), item.bid.formatAmount())
                                    DetailsField(
                                        stringResource(Res.string.profit),
                                        auction.getProfit(item).formatAmount()
                                    )
                                }
                                if (state.currentPlayerIsActive) {
                                    Button(
                                        stringResource(Res.string.resell),
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    ) {
                                        vm.sellBid(item)
                                        bottomSheetNavigator.hide()
                                    }
                                }
                            }
                        }
                    }
                }
                if (!state.currentPlayerIsActive) {
                    Row {
                        val bidState = remember {
                            mutableStateOf(TextFieldValue(minBid.toString()))
                        }
                        NumberTextField(
                            input = bidState,
                            inputLabel = stringResource(Res.string.bid),
                            modifier = Modifier.weight(1f),
                        )
                        val bid = bidState.value.text.toLongOrNull() ?: 0
                        ElevatedButton(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .widthIn(min = 200.dp),
                            onClick = {
                                vm.makeBid(bid, 1)
                            },
                            enabled = bidState.value.text.isNotEmpty() && bid >= minBid,
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