package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.deck.front.BoardCardFront
import ua.vald_zx.game.rat.race.card.shared.Auction

private const val ENTER_MS = 220
private const val EXIT_MS = 160

internal val LocalAuctionPanelToggle = staticCompositionLocalOf<(Auction) -> Unit> { {} }

@Composable
fun BoxWithConstraintsScope.DesignCardDialog(vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
    val takenCard = state.board.takenCard
    var shownCard by remember { mutableStateOf(takenCard) }
    val visible = takenCard != null
    LaunchedEffect(takenCard) {
        if (takenCard != null) {
            shownCard = takenCard
        } else {
            delay(EXIT_MS.toLong())
            shownCard = null
        }
    }
    val card = shownCard ?: return

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) ENTER_MS else EXIT_MS),
        label = "CardReveal",
    )
    if (progress == 0f && !visible) return

    var requestedAuction by remember(card) { mutableStateOf<Auction?>(null) }
    val shownAuction = requestedAuction?.let { state.board.auction ?: it }
    val showAuction = shownAuction != null
    val naturalWidth = min(max(min(maxWidth, maxHeight) * 2.7f / 3f, 300.dp), 600.dp)
    val targetCardWidth = if (showAuction) {
        min(naturalWidth, max((maxHeight - 240.dp) * 3f / 2f, 240.dp))
    } else {
        naturalWidth
    }
    val dialogWidth by animateDpAsState(targetCardWidth, label = "CardWidth")
    val dialogSize = DpSize(dialogWidth, dialogWidth * 2f / 3f)
    val auctionHeight = (maxHeight - dialogSize.height - 32.dp).coerceAtLeast(160.dp)
    val auctionWidth = min((maxWidth - 24.dp).coerceAtLeast(dialogWidth), max(dialogWidth, 760.dp))
    val contentWidth = if (showAuction) max(dialogWidth, auctionWidth) else dialogWidth

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(progress)
            .background(Design.scaffold.shadow.copy(alpha = 0.62f))
    )
    BoxWithConstraints(
        modifier = Modifier
            .align(Alignment.Center)
            .width(contentWidth)
            .alpha(progress)
            .scale(0.92f + progress * 0.08f),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().imePadding(),
        ) {
            BoxWithConstraints(Modifier.width(dialogSize.width)) {
                CompositionLocalProvider(
                    LocalAuctionPanelToggle provides { auction ->
                        requestedAuction = if (requestedAuction == null) auction else null
                    }
                ) {
                    BoardCardFront(card = card, size = dialogSize, vm = vm)
                }
            }
            shownAuction?.let { auction ->
                DesignAuctionPanel(
                    vm = vm,
                    fallbackAuction = auction,
                    modifier = Modifier
                        .width(auctionWidth)
                        .heightIn(max = auctionHeight),
                )
            }
        }
    }
}
