package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType

data class BoardLayout(
    val outerRoute: RouteLayout,
    val innerRoute: RouteLayout,
    val cardDecks: CardDeckLayout,
)

data class RouteLayout(
    val layer: BoardLayer,
    val route: BoardRoute,
    val size: DpSize,
    val cellSize: DpSize,
    val places: List<BoardPlace>,
)

data class BoardPlace(
    val index: Int,
    val place: Place,
)

data class CardDeckLayout(
    val size: DpSize,
    val slots: List<CardDeckSlot>,
)

data class CardDeckSlot(
    val type: BoardCardType,
    val kind: CardDeckSlotKind,
    val offset: DpOffset,
    val size: DpSize,
)

enum class CardDeckSlotKind {
    DRAW, DISCARD
}

private const val OUTER_RING_THICKNESS_CELLS = 4
private const val INNER_BOARD_GAP_CELLS = 1f / 3f
private const val INNER_CARD_AREA_MARGIN_CELLS = 4
private const val CARD_AREA_PADDING_CELLS = 1
private const val CARD_COLUMNS = 4
private const val CARD_AREA_DIVIDER = 5
private const val CARD_ASPECT_RATIO = 3f / 2f

private val leftDeckTypes = listOf(
    BoardCardType.Chance,
    BoardCardType.BigBusiness,
    BoardCardType.MediumBusiness,
    BoardCardType.SmallBusiness,
)

private val rightDeckTypes = listOf(
    BoardCardType.Expenses,
    BoardCardType.Deputy,
    BoardCardType.EventStore,
    BoardCardType.Shopping,
)

fun calculateBoardLayout(
    boardSize: DpSize,
    isVertical: Boolean,
    layers: BoardLayers = boardLayers,
): BoardLayout? {
    val outRoute = layers.layers[BoardLayer.OUTER] ?: return null
    val inRoute = layers.layers[BoardLayer.INNER] ?: return null
    val actualOutRoute = if (isVertical) outRoute.rotate() else outRoute
    val actualInRoute = if (isVertical) inRoute.rotate() else inRoute
    val outerCell = boardSize.cellSize(actualOutRoute)
    val innerBoardSize = innerBoardSize(boardSize, outerCell.width)
    val innerCell = innerBoardSize.cellSize(actualInRoute)
    val cardAreaSize = cardAreaSize(innerBoardSize, innerCell.width)

    return BoardLayout(
        outerRoute = routeLayout(
            layer = BoardLayer.OUTER,
            route = actualOutRoute,
            size = boardSize,
            cellSize = outerCell,
        ),
        innerRoute = routeLayout(
            layer = BoardLayer.INNER,
            route = actualInRoute,
            size = innerBoardSize,
            cellSize = innerCell,
        ),
        cardDecks = CardDeckLayout(
            size = cardAreaSize,
            slots = cardDeckSlots(cardAreaSize),
        )
    )
}

fun Modifier.fitBoardFrame(
    maxWidth: Dp,
    maxHeight: Dp,
    isVertical: Boolean,
): Modifier {
    val outRoute = boardLayers.layers[BoardLayer.OUTER] ?: return this
    val horizontalRatio = outRoute.horizontalCells.toFloat() / outRoute.verticalCells.toFloat()
    val verticalRatio = outRoute.verticalCells.toFloat() / outRoute.horizontalCells.toFloat()
    val availableRatio = maxWidth / maxHeight
    return if (isVertical) {
        if (availableRatio > horizontalRatio) {
            fillMaxWidth().aspectRatio(verticalRatio)
        } else {
            fillMaxHeight().aspectRatio(verticalRatio)
        }
    } else {
        if (availableRatio > horizontalRatio) {
            fillMaxHeight().aspectRatio(horizontalRatio)
        } else {
            fillMaxWidth().aspectRatio(horizontalRatio)
        }
    }
}

private fun DpSize.cellSize(route: BoardRoute): DpSize {
    return DpSize(
        width = width / route.horizontalCells,
        height = height / route.verticalCells,
    )
}

private fun innerBoardSize(
    boardSize: DpSize,
    outerCellSize: Dp,
): DpSize {
    val inPadding = outerCellSize * INNER_BOARD_GAP_CELLS
    val outerRingSize = outerCellSize * OUTER_RING_THICKNESS_CELLS
    return DpSize(
        width = boardSize.width - outerRingSize - inPadding * 2,
        height = boardSize.height - outerRingSize - inPadding * 2,
    )
}

private fun cardAreaSize(
    innerBoardSize: DpSize,
    innerCellSize: Dp,
): DpSize {
    val cardsPadding = innerCellSize * CARD_AREA_PADDING_CELLS
    val margin = innerCellSize * INNER_CARD_AREA_MARGIN_CELLS
    return DpSize(
        width = innerBoardSize.width - margin - cardsPadding * 2,
        height = innerBoardSize.height - margin - cardsPadding * 2,
    )
}

private fun routeLayout(
    layer: BoardLayer,
    route: BoardRoute,
    size: DpSize,
    cellSize: DpSize,
): RouteLayout {
    var placeOffset = 0
    val places = route.places.mapIndexed { index, type ->
        val location = getLocationOnBoard(placeOffset, route.horizontalCells, route.verticalCells)
        val cellSizeForPlace = type.getDpSize(location, cellSize.width, cellSize.height)
        val cellOffset = type.dpOffset(
            location,
            cellSize.width,
            cellSize.height,
            route.horizontalCells,
            route.verticalCells
        )
        placeOffset += if (type.isBig) 2 else 1
        BoardPlace(index, Place(type, location, cellOffset, cellSizeForPlace))
    }.sortedBy { it.place.type.isSalary }

    return RouteLayout(
        layer = layer,
        route = route,
        size = size,
        cellSize = cellSize,
        places = places,
    )
}

private fun cardDeckSlots(areaSize: DpSize): List<CardDeckSlot> {
    return if (areaSize.width < areaSize.height) {
        verticalCardDeckSlots(areaSize)
    } else {
        horizontalCardDeckSlots(areaSize)
    }
}

private fun verticalCardDeckSlots(areaSize: DpSize): List<CardDeckSlot> {
    val cardWidth = areaSize.width / CARD_AREA_DIVIDER
    val cardSize = DpSize(cardWidth, cardWidth * CARD_ASPECT_RATIO)
    val rowGap = cardWidth / 2
    val xPositions = spacedPositions(areaSize.width, cardSize.width, CARD_COLUMNS)
    val topDeckY = 0.dp
    val topDiscardY = cardSize.height + rowGap
    val bottomDiscardY = areaSize.height - cardSize.height * 2 - rowGap
    val bottomDeckY = areaSize.height - cardSize.height

    return buildList {
        addRowSlots(leftDeckTypes, CardDeckSlotKind.DRAW, xPositions, topDeckY, cardSize)
        addRowSlots(leftDeckTypes, CardDeckSlotKind.DISCARD, xPositions, topDiscardY, cardSize)
        addRowSlots(rightDeckTypes, CardDeckSlotKind.DISCARD, xPositions, bottomDiscardY, cardSize)
        addRowSlots(rightDeckTypes, CardDeckSlotKind.DRAW, xPositions, bottomDeckY, cardSize)
    }
}

private fun horizontalCardDeckSlots(areaSize: DpSize): List<CardDeckSlot> {
    val cardHeight = areaSize.height / CARD_AREA_DIVIDER
    val cardSize = DpSize(cardHeight * CARD_ASPECT_RATIO, cardHeight)
    val columnGap = cardHeight / 2
    val yPositions = spacedPositions(areaSize.height, cardSize.height, CARD_COLUMNS)
    val leftDeckX = 0.dp
    val leftDiscardX = cardSize.width + columnGap
    val rightDiscardX = areaSize.width - cardSize.width * 2 - columnGap
    val rightDeckX = areaSize.width - cardSize.width

    return buildList {
        addColumnSlots(leftDeckTypes, CardDeckSlotKind.DRAW, leftDeckX, yPositions, cardSize)
        addColumnSlots(leftDeckTypes, CardDeckSlotKind.DISCARD, leftDiscardX, yPositions, cardSize)
        addColumnSlots(rightDeckTypes, CardDeckSlotKind.DISCARD, rightDiscardX, yPositions, cardSize)
        addColumnSlots(rightDeckTypes, CardDeckSlotKind.DRAW, rightDeckX, yPositions, cardSize)
    }
}

private fun MutableList<CardDeckSlot>.addRowSlots(
    types: List<BoardCardType>,
    kind: CardDeckSlotKind,
    xPositions: List<Dp>,
    y: Dp,
    size: DpSize,
) {
    types.forEachIndexed { index, type ->
        add(CardDeckSlot(type, kind, DpOffset(xPositions[index], y), size))
    }
}

private fun MutableList<CardDeckSlot>.addColumnSlots(
    types: List<BoardCardType>,
    kind: CardDeckSlotKind,
    x: Dp,
    yPositions: List<Dp>,
    size: DpSize,
) {
    types.forEachIndexed { index, type ->
        add(CardDeckSlot(type, kind, DpOffset(x, yPositions[index]), size))
    }
}

private fun spacedPositions(
    totalSize: Dp,
    itemSize: Dp,
    count: Int,
): List<Dp> {
    if (count == 1) return listOf((totalSize - itemSize) / 2)

    val gap = (totalSize - itemSize * count) / (count - 1)
    return List(count) { index ->
        (itemSize + gap) * index
    }
}

private val PlaceType.isSalary: Boolean
    get() = this == PlaceType.Salary
