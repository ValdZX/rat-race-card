package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

enum class Side(val isHorizontal: Boolean) {
    TOP(true), LEFT(false), BOTTOM(true), RIGHT(false)
}

data class Location(
    val side: Side,
    val position: Int,
)

sealed class Place(val name: String, val color: Color, val isBig: Boolean = false) {
    object Start : Place("Start", Color.Yellow)
    object Salary : Place("Salary", Color(0xffa1e64c), isBig = true)
    object Business : Place("Business", Color(0xff00ba87))
    object Shopping : Place("Shopping", Color.Cyan)
    object Chance : Place("Chance", Color(0xfff36d4e))
    object Expenses : Place("Expenses", Color.Red)
    object Store : Place("Store", Color(0xff0788e8))
    object Bankruptcy : Place("Bankruptcy", Color(0xff94a5dd), isBig = true)
    object Child : Place("Child", Color.Black, isBig = true)
    object Love : Place("Love", Color.Magenta)
    object Rest : Place("Rest", Color.White)
    object Divorce : Place("Divorce", Color.Red)
    object Desire : Place("Desire", Color(0xffde9bc2))
    object Deputy : Place("Deputy", Color(0xff8a8fdc))
    object TaxInspection : Place("TaxInspection", Color(0xffc5dcc7), isBig = true)
    object Exaltation : Place("Exaltation", Color.Black)
}

class Board2Screen : Screen {
    private val cellOutX = 26
    private val cellInX = 28
    private val cellY = 18

    @Composable
    override fun Content() {
        val zoomState =
            rememberZoomState(contentSize = Size.Zero, initialScale = 1f, maxScale = 20f)
        Box(modifier = Modifier.fillMaxSize().zoomable(zoomState)) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                val minSide = min(maxWidth, maxHeight)
                val outSpotWidth = minSide / cellOutX
                val outSpotHeight = minSide / cellOutX
                Places(
                    places = outPlaces,
                    spotWidth = outSpotWidth,
                    spotHeight = outSpotHeight,
                    cellX = cellOutX,
                    cellY = cellY,
                )

                val inPadding = outSpotWidth / 3
                val inSpotWidth = (minSide - outSpotWidth * 4 - inPadding * 2) / cellInX
                val inSpotHeight =
                    ((outSpotWidth * cellY) - outSpotWidth * 4 - inPadding * 2) / cellY
                val inLineOffset = (outSpotWidth * 2) + inPadding
                Places(
                    places = inPlaces,
                    spotWidth = inSpotWidth,
                    spotHeight = inSpotHeight,
                    cellX = cellInX,
                    cellY = cellY,
                    offset = DpOffset(inLineOffset, inLineOffset),
                )
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
            val location = place.getLocationOnBoard(placeOffset, cellX, cellY)
            val cellSize = place.getDpSize(location, spotWidth, spotHeight)
            val cellOffset = place.dpOffset(location, spotWidth, spotHeight, cellX, cellY, offset)
            PlaceItem(
                color = place.color,
                size = cellSize,
                offset = cellOffset
            )
//            if (location.side.isHorizontal) {
//                drawVerticalPlaceText(place, textMeasurer, cellSize, cellOffset)
//            } else {
//                drawHorizontalPlaceText(place, textMeasurer, cellSize, cellOffset)
//            }
            placeOffset += if (place.isBig) 2 else 1
        }
    }

    @Composable
    private fun PlaceItem(color: Color, size: DpSize, offset: DpOffset) {
        var isSelected by remember { mutableStateOf(false) }
        val animation by animateFloatAsState(
            if (isSelected) 1f else 0f,
            animationSpec = tween(durationMillis = 300)
        )
        val width = size.width + size.width * (0.3f * animation)
        val height = size.height + size.height * (0.3f * animation)
        val offsetX = offset.x - ((width - size.width) / 2) * animation
        val offsetY = offset.y - ((height - size.height) / 2) * animation

        Box(
            modifier = Modifier
                .zIndex(if (isSelected) 1f else 0f)
                .size(height = height, width = width)
                .offset(offsetX, offsetY)
                .background(color)
                .clickable {
                    isSelected = !isSelected
                }
        )
    }

    private fun Place.getDpSize(
        location: Location,
        spotWidth: Dp,
        spotHeight: Dp,
    ): DpSize {
        return if (isBig) {
            DpSize(spotWidth * 2, spotHeight * 2)
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

    private fun Place.getLocationOnBoard(
        position: Int,
        horizontalSize: Int,
        verticalSize: Int = cellY
    ): Location {
        val leftSideMax = horizontalSize + verticalSize - 2
        val bottomSideMax = leftSideMax + horizontalSize - 2
        return if (position < horizontalSize) {
            Location(Side.TOP, position)
        } else if (position >= horizontalSize && position < leftSideMax) {
            Location(Side.LEFT, position - horizontalSize + 2)
        } else if (position >= leftSideMax && position < bottomSideMax) {
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