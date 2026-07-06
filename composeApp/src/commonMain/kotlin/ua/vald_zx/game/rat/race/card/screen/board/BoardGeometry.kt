package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.inPlaces
import ua.vald_zx.game.rat.race.card.shared.outPlaces

enum class Side(val isHorizontal: Boolean) {
    TOP(true), LEFT(false), BOTTOM(true), RIGHT(false)
}

data class Location(
    val side: Side,
    val position: Int,
)

data class Place(
    val type: PlaceType,
    val location: Location,
    val offset: DpOffset,
    val size: DpSize,
) {
    val isVertical: Boolean
        get() = (location.side == Side.TOP || location.side == Side.BOTTOM) && !type.isBig
}

data class BoardLayers(
    val layers: Map<BoardLayer, BoardRoute>
)

data class BoardRoute(
    val horizontalCells: Int,
    val verticalCells: Int,
    val places: List<PlaceType>,
    val offset: Int = 0,
) {
    fun rotate(): BoardRoute {
        val offset = horizontalCells - 4
        val firstPart = places.take(offset)
        val secondPart = places.drop(offset)
        return BoardRoute(
            horizontalCells = verticalCells,
            verticalCells = horizontalCells,
            places = secondPart + firstPart,
            offset = places.size - offset
        )
    }
}

const val INNER_LAYER_SCALE = 1.2f

val boardLayers = BoardLayers(
    layers = mapOf(
        BoardLayer.OUTER to BoardRoute(26, 18, outPlaces),
        BoardLayer.INNER to BoardRoute(28, 18, inPlaces),
    )
)

fun PlaceType.getDpSize(
    location: Location,
    spotWidth: Dp,
    spotHeight: Dp,
): DpSize {
    return if (isBig) {
        DpSize(spotWidth * 2, spotHeight * 2)
    } else if (location.side.isHorizontal) {
        DpSize(spotWidth, spotHeight * 2)
    } else {
        DpSize(spotWidth * 2, spotHeight)
    }
}

fun PlaceType.dpOffset(
    location: Location,
    spotWidth: Dp,
    spotHeight: Dp,
    cellX: Int,
    cellY: Int,
): DpOffset {
    return when (location.side) {
        Side.TOP -> DpOffset(
            x = spotWidth * location.position,
            y = 0.dp
        )

        Side.LEFT -> DpOffset(
            x = spotWidth * (cellX - 2),
            y = spotHeight * location.position
        )

        Side.BOTTOM -> DpOffset(
            x = spotWidth * (location.position - if (isBig) 1 else 0),
            y = spotHeight * (cellY - 2),
        )

        Side.RIGHT -> DpOffset(
            x = 0.dp,
            y = spotHeight * (location.position - if (isBig) 1 else 0)
        )
    }
}

fun getLocationOnBoard(
    position: Int,
    cellX: Int,
    cellY: Int
): Location {
    val leftSideMax = cellX + cellY - 2
    val bottomSideMax = leftSideMax + cellX - 2
    return if (position < cellX) {
        Location(Side.TOP, position)
    } else if (position in cellX..<leftSideMax) {
        Location(Side.LEFT, position - cellX + 2)
    } else if (position in leftSideMax..<bottomSideMax) {
        Location(Side.BOTTOM, cellX - (position - leftSideMax) - 3)
    } else {
        Location(Side.RIGHT, cellY - (position - bottomSideMax + 3))
    }
}
