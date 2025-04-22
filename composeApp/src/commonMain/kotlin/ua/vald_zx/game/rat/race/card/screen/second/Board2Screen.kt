package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import cafe.adriel.voyager.core.screen.Screen
import io.github.aakira.napier.Napier
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import ua.vald_zx.game.rat.race.card.dpToSp

enum class Side(val isHorizontal: Boolean) {
    TOP(true), LEFT(false), BOTTOM(true), RIGHT(false)
}

data class Location(
    val side: Side,
    val position: Int,
)

sealed class Place(val name: String, val color: Color, val isBig: Boolean = false) {
    data object Start : Place("Start", Color.Yellow)
    data object Salary : Place("Salary", Color(0xffa1e64c), isBig = true)
    data object Business : Place("Business", Color(0xff00ba87))
    data object Shopping : Place("Shopping", Color.Cyan)
    data object Chance : Place("Chance", Color(0xfff36d4e))
    data object Expenses : Place("Expenses", Color.Red)
    data object Store : Place("Store", Color(0xff0788e8))
    data object Bankruptcy : Place("Bankruptcy", Color(0xff94a5dd), isBig = true)
    data object Child : Place("Child", Color.Black, isBig = true)
    data object Love : Place("Love", Color.Magenta)
    data object Rest : Place("Rest", Color.White)
    data object Divorce : Place("Divorce", Color.Red)
    data object Desire : Place("Desire", Color(0xffde9bc2))
    data object Deputy : Place("Deputy", Color(0xff8a8fdc))
    data object TaxInspection : Place("TaxInspection", Color(0xffc5dcc7), isBig = true)
    data object Exaltation : Place("Exaltation", Color.Black)
}

class Board2Screen : Screen {
    private val cellOutX = 26
    private val cellInX = 28
    private val cellOutY = 18
    private val cellInY = 18

    @Composable
    override fun Content() {
        val zoomState =
            rememberZoomState(contentSize = Size.Zero, initialScale = 1f, maxScale = 20f)
        BoxWithConstraints(modifier = Modifier.fillMaxSize().zoomable(zoomState)) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(24.dp).align(Alignment.Center)
            ) {
                val minSide = min(maxWidth, maxHeight)
                val outSpotWidth = minSide / cellOutX
                LaunchedEffect(outSpotWidth) {
                    Napier.d("outSpotWidth: " + outSpotWidth.value)
                }
                val outSpotHeight = minSide / cellOutX
                val boardOffset = remember(minSide) {
                    val x = (maxWidth - outSpotWidth * cellOutX) / 2
                    val y = (maxHeight - outSpotHeight * cellOutY) / 2
                    DpOffset(x, y)
                }
                Places(
                    places = outPlaces,
                    spotWidth = outSpotWidth,
                    spotHeight = outSpotHeight,
                    cellX = cellOutX,
                    cellY = cellOutY,
                    offset = boardOffset,
                )
                val inPadding = outSpotWidth / 3
                val inSpotWidth = (minSide - outSpotWidth * 4 - inPadding * 2) / cellInX
                val inSpotHeight =
                    ((outSpotWidth * cellInY) - outSpotWidth * 4 - inPadding * 2) / cellInY
                val inLineOffset = (outSpotWidth * 2) + inPadding
                Places(
                    places = inPlaces,
                    spotWidth = inSpotWidth,
                    spotHeight = inSpotHeight,
                    cellX = cellInX,
                    cellY = cellInY,
                    offset = boardOffset + DpOffset(inLineOffset, inLineOffset),
                )
//                (0 until cellOutX).forEach { x ->
//                    (0 until cellOutY).forEach { y ->
//                        Box(
//                            modifier = Modifier.offset(boardOffset.x, boardOffset.y)
//                                .offset(outSpotWidth * x, outSpotHeight * y)
//                                .size(width = outSpotWidth, height = outSpotHeight)
//                                .alpha(0.4f)
//                                .background(if ((x + y) % 2 == 0) Color.Magenta else Color.Cyan)
//                        )
//                    }
//                }
            }
        }
    }

    @Composable
    private fun Places(
        places: List<Place>,
        spotWidth: Dp,
        spotHeight: Dp,
        cellX: Int,
        cellY: Int,
        offset: DpOffset = DpOffset(0.dp, 0.dp),
    ) {
        var placeOffset = 0
        places.forEach { place ->
            val location = getLocationOnBoard(placeOffset, cellX, cellY)
            val cellSize = place.getDpSize(location, spotWidth, spotHeight)
            val cellOffset = place.dpOffset(location, spotWidth, spotHeight, cellX, cellY, offset)
            val fontSize = (spotHeight / 3).dpToSp()
            PlaceItemContainer(
                place = place,
                size = cellSize,
                offset = cellOffset,
                fontSize = fontSize,
            )
            placeOffset += if (place.isBig) 2 else 1
        }
    }

    @Composable
    private fun PlaceItem(
        place: Place,
        fontSize: TextUnit,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(place.color)
        ) {
            Text(
                text = place.name,
                fontSize = fontSize,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    private fun PlaceItemContainer(
        place: Place,
        size: DpSize,
        offset: DpOffset,
        fontSize: TextUnit,
    ) {
        if (size.height > size.width) {
            Box(
                modifier = Modifier
                    .offset(x = offset.x, y = offset.y)
                    .offset(
                        y = size.height / 2f - size.width / 2f,
                        x = size.width / 2f - size.height / 2f
                    )
                    .size(width = size.height, height = size.width)
                    .rotate(90f)
                    .background(place.color)
            ) {
                PlaceItem(place, fontSize)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(width = size.width, height = size.height)
                    .offset(x = offset.x, y = offset.y)
            ) {
                PlaceItem(place, fontSize)
            }
        }
    }

    private fun Place.getDpSize(
        location: Location,
        spotWidth: Dp,
        spotHeight: Dp,
    ): DpSize {
        return if (isBig) {
            if (location.side.isHorizontal) {
                DpSize(spotHeight * 2, spotWidth * 2)
            } else {
                DpSize(spotWidth * 2, spotHeight * 2)
            }
        } else {
            if (location.side.isHorizontal) {
                DpSize(spotWidth, spotHeight * 2)
            } else {
                DpSize(spotWidth * 2, spotHeight)
            }
        }
    }

    private fun Place.dpOffset(
        location: Location,
        spotWidth: Dp,
        spotHeight: Dp,
        cellX: Int,
        cellY: Int,
        offset: DpOffset
    ): DpOffset {
        return offset + when (location.side) {
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

    private fun getLocationOnBoard(
        position: Int,
        horizontalSize: Int,
        verticalSize: Int
    ): Location {
        val leftSideMax = horizontalSize + verticalSize - 2
        val bottomSideMax = leftSideMax + horizontalSize - 2
        return if (position < horizontalSize) {
            Location(Side.TOP, position)
        } else if (position in horizontalSize..<leftSideMax) {
            Location(Side.LEFT, position - horizontalSize + 2)
        } else if (position in leftSideMax..<bottomSideMax) {
            Location(Side.BOTTOM, horizontalSize - (position - leftSideMax) - 3)
        } else {
            Location(Side.RIGHT, verticalSize - (position - bottomSideMax + 3))
        }
    }
}


private val inPlaces = listOf(
    Place.Salary,
    Place.Start,
    Place.Business,
    Place.Shopping,
    Place.Chance,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Shopping,
    Place.Expenses,
    Place.Store,
    Place.Business,
    Place.Bankruptcy,
    Place.Store,
    Place.Expenses,
    Place.Business,
    Place.Chance,
    Place.Shopping,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Business,
    Place.Expenses,
    Place.Store,

    Place.Salary,
    Place.Chance,
    Place.Business,
    Place.Expenses,
    Place.Store,
    Place.Love,
    Place.Shopping,
    Place.Chance,
    Place.Expenses,
    Place.Store,
    Place.Rest,
    Place.Chance,
    Place.Business,
    Place.Expenses,
    Place.Store,

    Place.Salary,
    Place.Shopping,
    Place.Chance,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Business,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Shopping,
    Place.Business,
    Place.Child,
    Place.Expenses,
    Place.Chance,
    Place.Business,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Divorce,
    Place.Shopping,
    Place.Expenses,
    Place.Store,
    Place.Expenses,

    Place.Salary,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Business,
    Place.Exaltation,
    Place.Expenses,
    Place.Chance,
    Place.Shopping,
    Place.Business,
    Place.Love,
    Place.Expenses,
    Place.Store,
    Place.Chance,
    Place.Expenses,
)

private val outPlaces = listOf(
    Place.Salary,
    Place.Start,
    Place.Desire,
    Place.Shopping,
    Place.Business,
    Place.Desire,
    Place.Store,
    Place.Chance,
    Place.Desire,
    Place.Business,
    Place.Store,
    Place.Bankruptcy,
    Place.Chance,
    Place.Desire,
    Place.Shopping,
    Place.Desire,
    Place.Business,
    Place.Chance,
    Place.Store,
    Place.Desire,
    Place.Shopping,
    Place.Desire,
    Place.Salary,

    Place.Desire,
    Place.Shopping,
    Place.Business,
    Place.Deputy,
    Place.Desire,
    Place.Chance,
    Place.Store,
    Place.Desire,
    Place.Shopping,
    Place.Business,
    Place.Deputy,
    Place.Desire,
    Place.Chance,
    Place.Store,
    Place.Salary,

    Place.Chance,
    Place.Desire,
    Place.Business,
    Place.Shopping,
    Place.Desire,
    Place.Store,
    Place.Chance,
    Place.Desire,
    Place.Business,
    Place.Chance,
    Place.TaxInspection,
    Place.Desire,
    Place.Shopping,
    Place.Desire,
    Place.Store,
    Place.Business,
    Place.Desire,
    Place.Chance,
    Place.Shopping,
    Place.Desire,
    Place.Store,
    Place.Salary,

    Place.Desire,
    Place.Shopping,
    Place.Business,
    Place.Deputy,
    Place.Desire,
    Place.Chance,
    Place.Store,
    Place.Desire,
    Place.Shopping,
    Place.Chance,
    Place.Deputy,
    Place.Desire,
    Place.Chance,
    Place.Store,
)