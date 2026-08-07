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
import ua.vald_zx.game.rat.race.card.shared.CoreTrackIds
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.TrackVisualHint
import ua.vald_zx.game.rat.race.card.shared.standardContentPackVersions
import ua.vald_zx.game.rat.race.card.shared.standardFeatureRegistry
import ua.vald_zx.game.rat.race.card.shared.legacyLayerOrNull
import ua.vald_zx.game.rat.race.card.shared.toCellInstance

data class BoardLayout(
    val routes: List<RouteLayout>,
    val cardDecks: CardDeckLayout,
) {
    val outerRoute: RouteLayout
        get() = routes.first { it.trackId == CoreTrackIds.Outer }
    val innerRoute: RouteLayout
        get() = routes.first { it.trackId == CoreTrackIds.Inner }
}

data class RouteLayout(
    val trackId: TrackId,
    val order: Int,
    val isOutermost: Boolean,
    val route: BoardRoute,
    val size: DpSize,
    val cellSize: DpSize,
    val places: List<BoardPlace>,
) {
    val layer: BoardLayer
        get() = trackId.legacyLayerOrNull() ?: error("Track $trackId has no legacy layer")
}

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

private const val INNER_CARD_AREA_MARGIN_CELLS = 4
private const val CARD_AREA_PADDING_CELLS = 1
private const val CARD_AREA_DIVIDER = 5
private const val CARD_ASPECT_RATIO = 3f / 2f

private val defaultDeckTypes = standardFeatureRegistry()
    .runtime(standardContentPackVersions())
    .decks
    .map { it.type }

fun calculateBoardLayout(
    boardSize: DpSize,
    isVertical: Boolean,
    layers: BoardLayers = boardLayers,
    deckTypes: List<BoardCardType> = defaultDeckTypes,
): BoardLayout? {
    val ordered = layers.layers.entries.sortedByDescending { layers.order[it.key] ?: 0 }
    if (ordered.isEmpty()) return null
    val definitions = ordered.map { (trackId, route) ->
        TrackDefinition(
            id = trackId,
            order = layers.order[trackId] ?: 0,
            cells = route.places.mapIndexed { index, place -> place.toCellInstance("${trackId.value}-$index") },
            visual = TrackVisualHint(route.horizontalCells, route.verticalCells),
        )
    }
    val frames = TrackLayoutEngine().layout(
        tracks = definitions,
        viewport = TrackViewport(boardSize.width.value, boardSize.height.value),
        portrait = isVertical,
    )
    val routes = frames.mapIndexed { index, frame ->
        val trackId = frame.trackId
        val route = layers.layers.getValue(trackId)
        val actualRoute = if (isVertical) route.rotate() else route
        val size = DpSize(frame.width.dp, frame.height.dp)
        val cell = size.cellSize(actualRoute)
        routeLayout(
            trackId = trackId,
            order = layers.order[trackId] ?: 0,
            isOutermost = index == 0,
            route = actualRoute,
            size = size,
            cellSize = cell,
        )
    }
    val innermost = routes.last()
    val cardAreaSize = cardAreaSize(innermost.size, innermost.cellSize.width)

    return BoardLayout(
        routes = routes,
        cardDecks = CardDeckLayout(
            size = cardAreaSize,
            slots = cardDeckSlots(cardAreaSize, deckTypes),
        )
    )
}

fun Modifier.fitBoardFrame(
    maxWidth: Dp,
    maxHeight: Dp,
    isVertical: Boolean,
    layers: BoardLayers = boardLayers,
): Modifier {
    val outerTrack = layers.order.maxByOrNull { it.value }?.key
    val outRoute = outerTrack?.let(layers.layers::get) ?: return this
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
    trackId: TrackId,
    order: Int,
    isOutermost: Boolean,
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
        trackId = trackId,
        order = order,
        isOutermost = isOutermost,
        route = route,
        size = size,
        cellSize = cellSize,
        places = places,
    )
}

private fun cardDeckSlots(areaSize: DpSize, deckTypes: List<BoardCardType>): List<CardDeckSlot> {
    val middle = (deckTypes.size + 1) / 2
    val first = deckTypes.take(middle)
    val second = deckTypes.drop(middle)
    return if (areaSize.width < areaSize.height) {
        verticalCardDeckSlots(areaSize, first, second)
    } else {
        horizontalCardDeckSlots(areaSize, first, second)
    }
}

private fun verticalCardDeckSlots(
    areaSize: DpSize,
    first: List<BoardCardType>,
    second: List<BoardCardType>,
): List<CardDeckSlot> {
    val cardWidth = areaSize.width / CARD_AREA_DIVIDER
    val cardSize = DpSize(cardWidth, cardWidth * CARD_ASPECT_RATIO)
    val rowGap = cardWidth / 2
    val topDeckY = 0.dp
    val topDiscardY = cardSize.height + rowGap
    val bottomDiscardY = areaSize.height - cardSize.height * 2 - rowGap
    val bottomDeckY = areaSize.height - cardSize.height

    return buildList {
        addRowSlots(first, CardDeckSlotKind.DRAW, spacedPositions(areaSize.width, cardSize.width, first.size), topDeckY, cardSize)
        addRowSlots(first, CardDeckSlotKind.DISCARD, spacedPositions(areaSize.width, cardSize.width, first.size), topDiscardY, cardSize)
        addRowSlots(second, CardDeckSlotKind.DISCARD, spacedPositions(areaSize.width, cardSize.width, second.size), bottomDiscardY, cardSize)
        addRowSlots(second, CardDeckSlotKind.DRAW, spacedPositions(areaSize.width, cardSize.width, second.size), bottomDeckY, cardSize)
    }
}

private fun horizontalCardDeckSlots(
    areaSize: DpSize,
    first: List<BoardCardType>,
    second: List<BoardCardType>,
): List<CardDeckSlot> {
    val cardHeight = areaSize.height / CARD_AREA_DIVIDER
    val cardSize = DpSize(cardHeight * CARD_ASPECT_RATIO, cardHeight)
    val columnGap = cardHeight / 2
    val leftDeckX = 0.dp
    val leftDiscardX = cardSize.width + columnGap
    val rightDiscardX = areaSize.width - cardSize.width * 2 - columnGap
    val rightDeckX = areaSize.width - cardSize.width

    return buildList {
        addColumnSlots(first, CardDeckSlotKind.DRAW, leftDeckX, spacedPositions(areaSize.height, cardSize.height, first.size), cardSize)
        addColumnSlots(first, CardDeckSlotKind.DISCARD, leftDiscardX, spacedPositions(areaSize.height, cardSize.height, first.size), cardSize)
        addColumnSlots(second, CardDeckSlotKind.DISCARD, rightDiscardX, spacedPositions(areaSize.height, cardSize.height, second.size), cardSize)
        addColumnSlots(second, CardDeckSlotKind.DRAW, rightDeckX, spacedPositions(areaSize.height, cardSize.height, second.size), cardSize)
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
    if (count == 0) return emptyList()
    if (count == 1) return listOf((totalSize - itemSize) / 2)

    val gap = (totalSize - itemSize * count) / (count - 1)
    return List(count) { index ->
        (itemSize + gap) * index
    }
}

private val PlaceType.isSalary: Boolean
    get() = this == PlaceType.Salary
