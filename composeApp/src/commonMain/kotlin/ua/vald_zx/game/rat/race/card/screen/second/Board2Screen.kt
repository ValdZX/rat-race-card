package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.jetbrains.compose.resources.Font
import rat_race_card.composeapp.generated.resources.Bubbleboddy
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.Money

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
        BoxWithConstraints(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            val zoomState =
                rememberZoomState(contentSize = Size.Zero, initialScale = 1f, maxScale = 20f)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .zoomable(zoomState)
            ) {
                val minSide = min(maxWidth, maxHeight)
                val outSpotWidth = (minSide / cellOutX)//.value.toInt().dp
                val outSpotHeight = (minSide / cellOutX)//.value.toInt().dp
                val boardOffset = remember(minSide) {
                    val x = ((maxWidth - outSpotWidth * cellOutX) / 2)//.value.toInt().dp
                    val y = ((maxHeight - outSpotHeight * cellOutY) / 2)//.value.toInt().dp
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
                val inSpotWidth =
                    ((minSide - outSpotWidth * 4 - inPadding * 2) / cellInX)//.value.toInt().dp
                val inSpotHeight =
                    (((outSpotWidth * cellInY) - outSpotWidth * 4 - inPadding * 2) / cellInY)//.value.toInt().dp
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


            val cube1 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_1.json").decodeToString()
                )
            }
            val cube2 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_2.json").decodeToString()
                )
            }
            val cube3 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_3.json").decodeToString()
                )
            }
            val cube4 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_4.json").decodeToString()
                )
            }
            val cube5 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_5.json").decodeToString()
                )
            }
            val cube6 by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/cube_6.json").decodeToString()
                )
            }
            val animatable = rememberLottieAnimatable()
            val coroutineScope = rememberCoroutineScope()
            var composition by remember { mutableStateOf<LottieComposition?>(null) }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.BottomEnd),
                onClick = {
                    composition = when ((1..6).random()) {
                        1 -> cube1
                        2 -> cube2
                        3 -> cube3
                        4 -> cube4
                        5 -> cube5
                        else -> cube6
                    }
                    coroutineScope.launch {
                        animatable.animate(composition, iterations = 1, initialProgress = 0f)
                    }
                },
                content = { Icon(Images.Dice, contentDescription = null) }
            )
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    progress = animatable::value
                ),
                contentDescription = "Lottie animation",
                modifier = Modifier.align(Alignment.BottomCenter).size(100.dp)
            )
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
            PlaceItemContainer(
                place = place,
                size = cellSize,
                offset = cellOffset,
                isVertical = (location.side == Side.TOP || location.side == Side.BOTTOM) && !place.isBig,
            )
            placeOffset += if (place.isBig) 2 else 1
        }
    }

    @Composable
    private fun BoxScope.PlaceItem(
        place: Place,
        cellSize: DpSize,
        isVertical: Boolean,
    ) {
        when (place) {
            Place.Salary -> {
                Image(Images.Money, contentDescription = null)
            }

            else -> {
                OutlinedText(
                    text = place.text,
                    autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                    fontFamily = FontFamily(
                        Font(
                            Res.font.Bubbleboddy,
                            weight = FontWeight.Medium
                        )
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(min(cellSize.height, cellSize.width) / 14)
                        .optionalModifier(isVertical) {
                            rotateLayout(Rotation.ROT_90)
                        },
                    fillColor = Color.White,
                    outlineColor = Color(0xFF8A8A8A),
                    outlineDrawStyle = Stroke(2f),
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    private fun PlaceItemContainer(
        place: Place,
        size: DpSize,
        offset: DpOffset,
        isVertical: Boolean,
    ) {
        Box(
            modifier = Modifier
                .size(width = size.width, height = size.height)
                .offset(x = offset.x, y = offset.y)
                .background(place.color)
        ) {
            PlaceItem(place, size, isVertical)
        }
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

    private fun getLocationOnBoard(
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
}

private val Place.text: String
    get() {
        return when (this) {
            Place.Salary -> "Прибуток"
            Place.Chance -> "Шанс!"
            Place.Store -> "Ринок"
            Place.Business -> "Бізнес"
            Place.Deputy -> "Депутат"
            else -> name
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