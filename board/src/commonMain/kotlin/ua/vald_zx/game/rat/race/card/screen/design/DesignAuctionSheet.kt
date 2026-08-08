package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Auction
import ua.vald_zx.game.rat.race.card.shared.Bid
import ua.vald_zx.game.rat.race.card.splitDecimal
import kotlin.math.max
import kotlin.math.min
import ua.vald_zx.game.rat.race.card.formatAmount

@Composable
fun DesignAuctionSheet(vm: BoardViewModel, fallbackAuction: Auction) {
    ClosableBottomSheetContainer(verticalScrollState = null) {
        DesignAuctionPanel(vm, fallbackAuction)
    }
}

@Composable
fun DesignAuctionPanel(
    vm: BoardViewModel,
    fallbackAuction: Auction,
    modifier: Modifier = Modifier,
) {
    val state by vm.uiState.collectAsState()
    val auction = state.board.auction ?: fallbackAuction
    val minBid = auction.minimumBid(state.board.bidList)
    var biddingOpen by remember(auction) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("auction-panel")
            .levelCard(Design.colors, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(Design.scaffold.surface1)
            .border(1.dp, Design.scaffold.outlineStrong, DesignShapes.lg)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        key(auction) {
            when {
                state.board.auction == null -> ScrollableAuctionForm {
                    AdvertiseForm(vm, auction, minBid)
                }

                biddingOpen -> ScrollableAuctionForm {
                    BidForm(
                        auction = auction,
                        minBid = minBid,
                        canPay = { state.canPay(it) },
                        onBack = { biddingOpen = false },
                        onBid = { bid, count ->
                            biddingOpen = false
                            vm.makeBid(bid, count)
                        },
                    )
                }

                else -> {
                    LotHeader(auction, minBid)
                    BidList(
                        bids = state.board.bidList,
                        auction = auction,
                        sellEnabled = state.currentPlayerIsActive,
                        onSell = { vm.sellBid(it) },
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (!state.currentPlayerIsActive && state.canMakeBid()) {
                        DesignButton(
                            text = stringResource(Res.string.placeBet),
                            modifier = Modifier.fillMaxWidth(),
                            height = 52.dp,
                        ) { biddingOpen = true }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ScrollableAuctionForm(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun ColumnScope.AdvertiseForm(vm: BoardViewModel, auction: Auction, minBid: Long) {
    val action = stringResource(Res.string.advertiseAuction)
    val minText = stringResource(Res.string.min_bid, minBid.formatAmount())
    DesignAmountForm(
        title = stringResource(Res.string.firstBid),
        subtitle = minText,
        initial = minBid,
        confirmLabel = { amount -> "$action ${amount.formatAmount()}" },
        validate = { it >= minBid },
        onConfirm = { amount -> vm.advertiseAuction(auction.copy(bid = amount)) },
    )
}

@Composable
private fun ColumnScope.BidForm(
    auction: Auction,
    minBid: Long,
    canPay: (Long) -> Boolean,
    onBack: () -> Unit,
    onBid: (bid: Long, count: Long) -> Unit,
) {
    val isShares = auction is Auction.SharesAuction
    val maxCount = (auction as? Auction.SharesAuction)?.shares?.count ?: 0
    var count by remember { mutableStateOf(if (isShares) 1L else 0L) }
    val tooLow = stringResource(Res.string.min_bid, minBid.formatAmount())
    val notEnough = stringResource(Res.string.not_enough_money)
    val action = stringResource(Res.string.placeBet)
    val quickOptions = listOf(
        AmountQuickOption("+10%", minBid + max(1, minBid / 10)),
        AmountQuickOption("+25%", minBid + max(1, minBid / 4)),
    )

    DesignAmountForm(
        title = stringResource(Res.string.bid),
        subtitle = if (isShares) stringResource(Res.string.quantity) + ": $count / $maxCount" else null,
        initial = minBid,
        confirmLabel = { amount -> "$action ${amount.formatAmount()}" },
        onConfirm = { amount -> onBid(amount, count) },
        onCancel = onBack,
        cancelLabel = stringResource(Res.string.cancel),
        quickOptions = quickOptions,
        validate = { amount -> amount >= minBid && (!isShares || count in 1..maxCount) },
        errorFor = { amount ->
            when {
                amount in 1 until minBid -> tooLow
                amount > 0 && !canPay(if (isShares) amount * count else amount) -> notEnough
                else -> null
            }
        },
        extraContent = {
            if (isShares) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DesignChip("−", selected = false, repeatable = true) {
                            count = max(1, count - 1)
                        }
                        Text(
                            text = "$count",
                            style = Design.type.amountMd,
                            color = Design.scaffold.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        DesignChip("+", selected = false, repeatable = true) {
                            count = min(maxCount, count + 1)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DesignChip(
                            text = "+10",
                            selected = false,
                            modifier = Modifier.weight(1f),
                            repeatable = true,
                        ) { count = min(maxCount, count + 10) }
                        DesignChip(
                            text = "+100",
                            selected = false,
                            modifier = Modifier.weight(1f),
                            repeatable = true,
                        ) { count = min(maxCount, count + 100) }
                        DesignChip(
                            text = stringResource(Res.string.all_in),
                            selected = false,
                            modifier = Modifier.weight(1f),
                        ) { count = maxCount }
                    }
                }
            }
        },
    )
}

@Composable
private fun LotHeader(auction: Auction, minBid: Long) {
    val colors = Design.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .levelCard(colors, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .padding(14.dp),
    ) {
        Text(
            text = stringResource(Res.string.auction),
            style = Design.type.label,
            color = colors.scaffold.onSurfaceMuted,
        )
        Text(
            text = minBid.formatAmount(),
            style = Design.type.amountLg,
            color = colors.scaffold.brass,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            text = stringResource(Res.string.min_bid, minBid.formatAmount()),
            style = Design.type.monoMeta,
            color = colors.scaffold.onSurfaceMuted,
        )
    }
}

@Composable
internal fun DesignAuctionBidFormForTest(auction: Auction, minBid: Long) = Column {
    BidForm(
        auction = auction,
        minBid = minBid,
        canPay = { true },
        onBack = {},
        onBid = { _, _ -> },
    )
}

@Composable
internal fun DesignAuctionBidListForTest(
    bids: List<Bid>,
    auction: Auction,
    sellEnabled: Boolean,
) = BidList(bids, auction, sellEnabled, onSell = {})

@Composable
private fun BidList(
    bids: List<Bid>,
    auction: Auction,
    sellEnabled: Boolean,
    onSell: (Bid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Design.colors
    if (bids.isEmpty()) {
        Text(
            text = stringResource(Res.string.noBetsYet),
            style = Design.type.body,
            color = colors.scaffold.onSurfaceMuted,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(bids) { bid -> BidRow(bid, auction, sellEnabled, onSell) }
    }
}

@Composable
private fun BidRow(bid: Bid, auction: Auction, sellEnabled: Boolean, onSell: (Bid) -> Unit) {
    val colors = Design.colors
    val bidder = remember(bid.playerId) { players.value.find { it.id == bid.playerId } }
    val enoughShares = auction !is Auction.SharesAuction || auction.shares.count >= bid.count
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(DesignShapes.full)
                .background(bidder?.let { Color(it.attrs.color) } ?: colors.scaffold.outlineStrong)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bidder?.card?.name.orEmpty(),
                style = Design.type.subtitle,
                color = colors.scaffold.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (bid.count > 0) {
                    "${bid.bid.formatAmount()} × ${bid.count}"
                } else {
                    bid.bid.formatAmount()
                },
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        val profit = auction.getProfit(bid)
        Text(
            text = profit.formatSigned(),
            style = Design.type.amountMd,
            color = if (profit >= 0) Design.semantic.positive.edge else Design.semantic.negative.edge,
            maxLines = 1,
            softWrap = false,
        )
        if (sellEnabled) {
            DesignButton(
                text = stringResource(Res.string.sell),
                kind = DesignButtonKind.Brass,
                enabled = enoughShares,
                height = 40.dp,
            ) { onSell(bid) }
        }
    }
}
