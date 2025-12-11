package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import dev.lennartegb.shadows.boxShadow
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import rat_race_card.composeapp.generated.resources.Bubbleboddy
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.isVertical
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType


@Composable
fun CardDeck(
    cardType: BoardCardType,
    size: DpSize,
    highlightedCardType: BoardCardType?,
    state: BoardState,
    vm: BoardViewModel,
) {
    val rounding = min(size.width, size.height) / 10
    val blurRadius = min(size.width, size.height) / 10
    val spreadRadius = min(size.width, size.height) / 20

    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .size(size.width, size.height)
            .onGloballyPositioned { layoutCoordinates ->
                val positionInWindow = layoutCoordinates.positionInWindow()
                val offsetX = with(density) { positionInWindow.x.toDp() }
                val offsetY = with(density) { positionInWindow.y.toDp() }
                val offset = DpOffset(offsetX, offsetY)
                val coordinates = offset to size
                deckCoordinatesMap.getOrPut(cardType) {
                    mutableStateOf(coordinates)
                }.value = coordinates
            }
            .optionalModifier(cardType == highlightedCardType && state.currentPlayerIsActive) {
                boxShadow(
                    blurRadius = blurRadius,
                    spreadRadius = spreadRadius,
                    shape = RoundedCornerShape(rounding),
                    color = MaterialTheme.colorScheme.background,
                ).clickable {
                    vm.selectCard(cardType)
                }
            }
    ) {
        val trueHeight = min(size.width, size.height)
        val count = state.board.cards[cardType].orEmpty().size
        if (count == 0) {
            val rounding = trueHeight / 16
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(rounding))
                    .border(trueHeight / 40f, cardType.color())
            ) {
                BoardCardText(cardType, size, size.isVertical)
            }
        } else {
            BoardCardBack(cardType, size, size.isVertical)
            Box(modifier = Modifier.fillMaxSize().optionalModifier(size.isVertical) {
                rotateLayout(Rotation.ROT_90)
            }) {
                val padding = trueHeight / 10
                OutlinedText(
                    "$count",
                    modifier = Modifier.padding(padding).align(Alignment.TopEnd),
                    fillColor = MaterialTheme.colorScheme.onPrimary,
                    outlineColor = Color(0xFF8A8A8A),
                    style = TextStyle.Default.copy(fontSize = (trueHeight / 6f).value.sp)
                )
            }
        }
    }
}

@Composable
fun BoardCardBack(
    card: BoardCardType,
    size: DpSize,
    isVertical: Boolean,
    modifier: Modifier = Modifier
) {
    val rounding = min(size.width, size.height) / 16
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(rounding))
            .background(card.color())
    ) {
        BoardCardText(card, size, isVertical)
    }
}

@Composable
fun BoxScope.BoardCardText(card: BoardCardType, size: DpSize, isVertical: Boolean) {
    val min = min(size.width, size.height)
    OutlinedText(
        text = when (card) {
            BoardCardType.BigBusiness -> "Великий бізнес"
            BoardCardType.Chance -> "Шанс!"
            BoardCardType.Deputy -> "Депутати"
            BoardCardType.EventStore -> "Ринок"
            BoardCardType.Expenses -> "Витрати"
            BoardCardType.MediumBusiness -> "Середній бізнес"
            BoardCardType.Shopping -> "Покупки"
            BoardCardType.SmallBusiness -> "Малий бізнес"
        },
        autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
        fontFamily = FontFamily(
            Font(
                Res.font.Bubbleboddy,
                weight = FontWeight.Medium
            )
        ),
        modifier = Modifier.align(Alignment.Center)
            .padding(min / 14)
            .optionalModifier(isVertical) {
                rotateLayout(Rotation.ROT_90)
            },
        fillColor = MaterialTheme.colorScheme.onPrimary,
        outlineColor = Color(0xFF8A8A8A),
        outlineDrawStyle = Stroke(min.value / 30f),
        maxLines = 1
    )
}

@Composable
fun DiscardPile(
    card: BoardCardType,
    size: DpSize,
    state: BoardState,
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .size(size.width, size.height)
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.positionInWindow()
                val offsetX = with(density) { position.x.toDp() }
                val offsetY = with(density) { position.y.toDp() }
                val offset = DpOffset(offsetX, offsetY)
                val coordinates = offset to size
                discardPilesCoordinatesMap.getOrPut(card) {
                    mutableStateOf(coordinates)
                }.value = coordinates
            }
    ) {
        val trueHeight = min(size.width, size.height)
        var count by remember { mutableStateOf(state.board?.discard[card].orEmpty().size) }
        LaunchedEffect(state.board?.discard) {
            val discardCardCount = state.board?.discard[card].orEmpty().size
            delay(cardMoveAnimationDuration.toLong())
            count = discardCardCount
        }
        if (count == 0) {
            val rounding = trueHeight / 16
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(rounding))
                    .border(trueHeight / 40f, card.color())
            ) {
                BoardCardText(card, size, size.width < size.height)
            }
        } else {
            BoardCardBack(card, size, size.isVertical)
            Box(modifier = Modifier.fillMaxSize().optionalModifier(size.isVertical) {
                rotateLayout(Rotation.ROT_90)
            }) {
                val padding = trueHeight / 10
                OutlinedText(
                    "$count",
                    modifier = Modifier.padding(padding).align(Alignment.TopEnd),
                    fillColor = MaterialTheme.colorScheme.onPrimary,
                    outlineColor = Color(0xFF8A8A8A),
                    style = TextStyle.Default.copy(fontSize = (trueHeight / 6f).value.sp)
                )
            }
        }
    }
}