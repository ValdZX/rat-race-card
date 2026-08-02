package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.*
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

internal const val DECKS_Z = 2f

@Composable
fun BoxScope.DesignCardDecks(layout: CardDeckLayout, vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .zIndex(DECKS_Z)
    ) {
        layout.slots.forEach { slot ->
            val count = if (slot.kind == CardDeckSlotKind.DISCARD) {
                state.board.discard[slot.type].orEmpty().size
            } else {
                state.board.cards[slot.type].orEmpty().size
            }
            val canTake = slot.kind == CardDeckSlotKind.DRAW &&
                    state.board.canTakeCard.contains(slot.type) &&
                    state.currentPlayerIsActive
            Box(
                modifier = Modifier
                    .offset(slot.offset.x, slot.offset.y)
                    .size(slot.size)
                    .zIndex(if (canTake) 1f else 0f)
            ) {
                DeckSlot(
                    type = slot.type,
                    kind = slot.kind,
                    size = slot.size,
                    count = count,
                    actionable = canTake,
                    onClick = { vm.selectCard(slot.type) },
                )
            }
        }
    }
}

@Composable
internal fun DeckSlot(
    type: BoardCardType,
    kind: CardDeckSlotKind,
    size: DpSize,
    count: Int,
    actionable: Boolean,
    onClick: () -> Unit,
) {
    val colors = Design.colors
    val tone = type.tone()
    val density = LocalDensity.current
    val isDraw = kind == CardDeckSlotKind.DRAW
    val minSize = min(size.width, size.height)
    val shape = if (minSize >= 56.dp) DesignShapes.xl else DesignShapes.sm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val offset = with(density) { DpOffset(position.x.toDp(), position.y.toDp()) }
                val map = if (isDraw) deckCoordinatesMap else discardPilesCoordinatesMap
                map.getOrPut(type) { mutableStateOf(offset to size) }.value = offset to size
            }
            .optionalModifier(actionable) { clickableSingle(onClick = onClick) },
        contentAlignment = Alignment.Center,
    ) {
        if (actionable) {
            DesignActivePulse(shape = shape, color = tone.edge)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(if (isDraw && count > 0) tone.fill else colors.scaffold.surface3)
                .border(
                    width = if (actionable) 2.dp else 1.dp,
                    color = when {
                        actionable -> colors.scaffold.accent
                        isDraw && count > 0 -> tone.edge
                        else -> colors.scaffold.outline
                    },
                    shape = shape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val ink = if (isDraw && count > 0) colors.scaffold.onFill else colors.scaffold.onSurfaceMuted
            val withLabel = size.height >= 72.dp
            val iconSize = minSize * if (withLabel) 0.42f else 0.34f
            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                Icon(
                    painter = type.icon(),
                    contentDescription = type.shortLabel(),
                    tint = ink,
                    modifier = Modifier.size(iconSize),
                )
                if (withLabel) {
                    Text(
                        text = type.shortLabel(),
                        style = Design.type.cellSm,
                        color = ink,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = count.toString(),
                    style = Design.type.monoMeta,
                    color = ink,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
internal fun BoardCardType.tone(): SemanticTone = when (this) {
    BoardCardType.Chance -> Design.semantic.chance
    BoardCardType.Expenses -> Design.semantic.expenses
    BoardCardType.Shopping -> Design.semantic.shopping
    BoardCardType.EventStore -> Design.semantic.store
    BoardCardType.Deputy -> Design.semantic.deputy
    BoardCardType.SmallBusiness -> Design.semantic.smallBusiness
    BoardCardType.MediumBusiness -> Design.semantic.business
    BoardCardType.BigBusiness -> Design.semantic.bigBusiness
}

@Composable
internal fun BoardCardType.shortLabel(): String = when (this) {
    BoardCardType.Chance -> stringResource(Res.string.chance)
    BoardCardType.Expenses -> stringResource(Res.string.expenses)
    BoardCardType.Shopping -> stringResource(Res.string.shopping)
    BoardCardType.EventStore -> stringResource(Res.string.store)
    BoardCardType.Deputy -> stringResource(Res.string.deputy)
    BoardCardType.SmallBusiness -> stringResource(Res.string.small_business)
    BoardCardType.MediumBusiness -> stringResource(Res.string.medium_business)
    BoardCardType.BigBusiness -> stringResource(Res.string.big_business)
}
