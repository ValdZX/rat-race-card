package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.roundToInt

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
        val textMeasurer = rememberTextMeasurer()
        Box(modifier = Modifier.fillMaxSize().zoomable(zoomState)) {
            Canvas(modifier = Modifier.fillMaxSize().padding(24.dp).clip(RectangleShape)) {
                val outSpotWidth = size.width / cellOutX
                val outSpotHeight = size.width / cellOutX
                drawPlaces(
                    places = outPlaces,
                    spotWidth = outSpotWidth,
                    spotHeight = outSpotHeight,
                    cellX = cellOutX,
                    cellY = cellY,
                    textMeasurer = textMeasurer,
                )
                val inPadding = outSpotWidth / 3
                val inSpotWidth = (size.width - outSpotWidth * 4 - inPadding * 2) / cellInX
                val inSpotHeight =
                    ((outSpotWidth * cellY) - outSpotWidth * 4 - inPadding * 2) / cellY
                val inLineOffset = (outSpotWidth * 2) + inPadding
                drawPlaces(
                    places = inPlaces,
                    spotWidth = inSpotWidth,
                    spotHeight = inSpotHeight,
                    cellX = cellInX,
                    cellY = cellY,
                    offset = Offset(inLineOffset, inLineOffset),
                    textMeasurer = textMeasurer,
                )
            }
        }
    }

    private fun DrawScope.drawPlaces(
        places: List<Place>,
        spotWidth: Float,
        spotHeight: Float,
        cellX: Int,
        cellY: Int,
        textMeasurer: TextMeasurer,
        offset: Offset = Offset(0f, 0f),
    ) {
        var placeOffset = 0
        places.forEach { place ->
            val location = place.getLocationOnBoard(placeOffset, cellX, cellY)
            val cellSize = place.getSize(location, spotWidth, spotHeight)
            val cellOffset = place.offset(location, spotWidth, spotHeight, cellX, cellY, offset)
            drawRect(
                color = place.color,
                size = cellSize,
                topLeft = cellOffset
            )
            if (location.side.isHorizontal) {
                drawVerticalPlaceText(place, textMeasurer, cellSize, cellOffset)
            } else {
                drawHorizontalPlaceText(place, textMeasurer, cellSize, cellOffset)
            }
            placeOffset += if (place.isBig) 2 else 1
        }
    }

    private fun DrawScope.drawVerticalPlaceText(
        place: Place,
        measurer: TextMeasurer,
        cellSize: Size,
        cellOffset: Offset
    ) {
        val layoutResult = measurer.measure(
            text = place.name,
            style = TextStyle(
                color = Color.White,
                fontSize = (cellSize.width / 3).toSp(),
                textAlign = TextAlign.Center,
            ),
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            maxLines = 1,
            constraints = Constraints(
                maxWidth = cellSize.height.roundToInt(),
                maxHeight = cellSize.width.roundToInt()
            )
        )
        rotate(90f, cellOffset + Offset(cellSize.width / 2f, cellSize.height / 2f)) {
            drawText(
                textLayoutResult = layoutResult,
                topLeft = cellOffset + Offset(
                    x = (cellSize.width - layoutResult.size.width) / 2f,
                    y = (cellSize.height - layoutResult.size.height) / 2f
                ),
            )
        }
    }

    private fun DrawScope.drawHorizontalPlaceText(
        place: Place,
        measurer: TextMeasurer,
        cellSize: Size,
        cellOffset: Offset
    ) {
        val layoutResult = measurer.measure(
            text = place.name,
            style = TextStyle(
                color = Color.White,
                fontSize = (cellSize.height / 2).toSp(),
                textAlign = TextAlign.Center,
            ),
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            maxLines = 1,
            constraints = Constraints(
                maxWidth = cellSize.width.roundToInt(), maxHeight = cellSize.height.roundToInt()
            )
        )
        drawText(
            textLayoutResult = layoutResult,
            topLeft = cellOffset + Offset(
                x = (cellSize.width - layoutResult.size.width) / 2f,
                y = (cellSize.height - layoutResult.size.height) / 2f
            )
        )
    }

    private fun Place.getSize(
        location: Location,
        spotWidth: Float,
        spotHeight: Float,
    ): Size {
        return if (isBig) {
            Size(spotWidth * 2, spotHeight * 2)
        } else {
            if (location.side.isHorizontal) {
                Size(spotWidth, spotHeight * 2)
            } else {
                Size(spotWidth * 2, spotHeight)
            }
        }
    }

    private fun Place.offset(
        location: Location,
        spotWidth: Float,
        spotHeight: Float,
        cellX: Int,
        cellY: Int,
        offset: Offset
    ): Offset {
        return offset + when (location.side) {
            Side.TOP -> Offset(
                x = spotWidth * location.position,
                y = 0f
            )

            Side.LEFT -> Offset(
                x = (cellX - 2) * spotWidth,
                y = spotHeight * location.position
            )

            Side.BOTTOM -> Offset(
                x = spotWidth * (location.position - if (isBig) 1 else 0),
                y = (cellY - 2) * spotHeight,
            )

            Side.RIGHT -> Offset(
                x = 0f,
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


private val inPlaces = listOf<Place>(
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

private val outPlaces = listOf<Place>(
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