package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import rat_race_card.composeapp.generated.resources.Bubbleboddy
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.business_failure
import rat_race_card.composeapp.generated.resources.child
import rat_race_card.composeapp.generated.resources.detective
import rat_race_card.composeapp.generated.resources.divorce
import rat_race_card.composeapp.generated.resources.love
import rat_race_card.composeapp.generated.resources.vacation
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.moveTo
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Money
import ua.vald_zx.game.rat.race.card.theme.AppTheme


sealed class PlaceType(val name: String, val isBig: Boolean = false) {
    data object Start : PlaceType("Start")
    data object Salary : PlaceType("Salary", isBig = true)

    data object Business : PlaceType("Business")
    data object BigBusiness : PlaceType("BigBusiness")
    data object Shopping : PlaceType("Shopping")

    data object Chance : PlaceType("Chance")
    data object Expenses : PlaceType("Expenses")

    data object Store : PlaceType("Store")
    data object Bankruptcy : PlaceType("Bankruptcy", isBig = true)
    data object Child : PlaceType("Child", isBig = true)
    data object Love : PlaceType("Love")
    data object Rest : PlaceType("Rest")
    data object Divorce : PlaceType("Divorce")
    data object Desire : PlaceType("Desire")
    data object Deputy : PlaceType("Deputy")
    data object TaxInspection : PlaceType("TaxInspection", isBig = true)
    data object Exaltation : PlaceType("Exaltation")
}


val inPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Business,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Business,
    PlaceType.Bankruptcy,
    PlaceType.Store,
    PlaceType.Expenses,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Love,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Rest,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Child,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Divorce,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Expenses,

    PlaceType.Salary,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Exaltation,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Love,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Expenses,
)

val outPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.TaxInspection,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.BigBusiness,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
)

private val PlaceType.text: String
    get() {
        return when (this) {
            PlaceType.Salary -> "Прибуток"
            PlaceType.Chance -> "Шанс!"
            PlaceType.Store -> "Ринок"
            PlaceType.Business -> "Бізнес"
            PlaceType.BigBusiness -> "Бізнес"
            PlaceType.Deputy -> "Депутат"
            PlaceType.Expenses -> "Витрати"
            PlaceType.Shopping -> "Покупки"
            PlaceType.Rest -> "Відпустка"
            PlaceType.Desire -> "Мрія"
            PlaceType.Start -> "Старт"
            PlaceType.Exaltation -> "Звільнення"
            PlaceType.Divorce -> "Розлучення"
            PlaceType.Bankruptcy -> "Банкрутство"
            else -> name
        }
    }

@Composable
private fun PlaceType.color(): Color {
    return when (this) {
        PlaceType.Start -> AppTheme.colors.start
        PlaceType.Salary -> AppTheme.colors.salary
        PlaceType.Business -> AppTheme.colors.business
        PlaceType.BigBusiness -> AppTheme.colors.bigBusiness
        PlaceType.Shopping -> AppTheme.colors.shopping
        PlaceType.Chance -> AppTheme.colors.chance
        PlaceType.Expenses -> AppTheme.colors.expenses
        PlaceType.Store -> AppTheme.colors.store
        PlaceType.Bankruptcy -> AppTheme.colors.bankruptcy
        PlaceType.Child -> AppTheme.colors.desire
        PlaceType.Love -> AppTheme.colors.love
        PlaceType.Rest -> AppTheme.colors.rest
        PlaceType.Divorce -> AppTheme.colors.divorce
        PlaceType.Desire -> AppTheme.colors.desire
        PlaceType.Deputy -> AppTheme.colors.deputy
        PlaceType.TaxInspection -> AppTheme.colors.inspection
        PlaceType.Exaltation -> AppTheme.colors.exaltation
    }
}


@Composable
fun BoxScope.PlaceContent(
    placeType: PlaceType,
    cellSize: DpSize,
    isVertical: Boolean,
) {
    val minSide = min(cellSize.height, cellSize.width)
    val maxSide = max(cellSize.height, cellSize.width)
    val density = LocalDensity.current
    when (placeType) {
        PlaceType.Salary -> {
            Box(Modifier.fillMaxSize().background(placeType.color())) {
                Image(Images.Money, contentDescription = null)
            }
        }

        PlaceType.Child -> {
            Box(Modifier.fillMaxSize().background(placeType.color())) {
                Image(
                    painterResource(Res.drawable.child),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 14)
                )
            }
        }

        PlaceType.Bankruptcy -> {
            Box(Modifier.fillMaxSize().background(placeType.color())) {
                Image(
                    painterResource(Res.drawable.business_failure),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 14)
                )
            }
        }

        PlaceType.TaxInspection -> {
            Box(Modifier.fillMaxSize().background(placeType.color())) {
                Image(
                    painterResource(Res.drawable.detective),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 14)
                )
            }
        }

        PlaceType.Love -> {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        0.0f to AppTheme.colors.rest,
                        1.0f to placeType.color(),
                        radius = with(density) { maxSide.toPx() },
                        tileMode = TileMode.Repeated
                    )
                )
            ) {
                Image(
                    painterResource(Res.drawable.love),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 5).align(Alignment.Center)
                )
            }
        }

        PlaceType.Divorce -> {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        0.0f to AppTheme.colors.rest,
                        1.0f to placeType.color(),
                        radius = with(density) { maxSide.toPx() },
                        tileMode = TileMode.Repeated
                    )
                )
            ) {
                Image(
                    painterResource(Res.drawable.divorce),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 5).align(Alignment.Center)
                )
            }
        }

        PlaceType.Rest -> {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        0.0f to AppTheme.colors.rest,
                        1.0f to placeType.color(),
                        radius = with(density) { maxSide.toPx() },
                        tileMode = TileMode.Repeated
                    )
                )
            ) {
                Image(
                    painterResource(Res.drawable.vacation),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        PlaceType.Expenses -> {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        0.0f to AppTheme.colors.rest,
                        1.0f to placeType.color(),
                        radius = with(density) { maxSide.toPx() },
                        tileMode = TileMode.Repeated
                    )
                )
            ) {
                OutlinedText(
                    text = placeType.text,
                    autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                    fontFamily = FontFamily(
                        Font(
                            Res.font.Bubbleboddy,
                            weight = FontWeight.Medium
                        )
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(minSide / 14)
                        .optionalModifier(isVertical) {
                            rotateLayout(Rotation.ROT_90)
                        },
                    fillColor = MaterialTheme.colorScheme.onPrimary,
                    outlineColor = Color(0xFF8A8A8A),
                    outlineDrawStyle = Stroke(2f),
                    maxLines = 1
                )
            }
        }

        else -> {
            Box(Modifier.fillMaxSize().background(placeType.color())) {
                OutlinedText(
                    text = placeType.text,
                    autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                    fontFamily = FontFamily(
                        Font(
                            Res.font.Bubbleboddy,
                            weight = FontWeight.Medium
                        )
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(minSide / 14)
                        .optionalModifier(isVertical) {
                            rotateLayout(Rotation.ROT_90)
                        },
                    fillColor = MaterialTheme.colorScheme.onPrimary,
                    outlineColor = Color(0xFF8A8A8A),
                    outlineDrawStyle = Stroke(2f),
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
fun BoxScope.Places(
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
    layer: BoardLayer,
    size: DpSize,
    route: BoardRoute,
) {
    val spotWidth = size.width / route.horizontalCells
    val spotHeight = size.height / route.verticalCells
    val places = remember(route.places, size) {
        var placeOffset = 0
        route.places.map { type ->
            val location =
                getLocationOnBoard(placeOffset, route.horizontalCells, route.verticalCells)
            val cellSize = type.getDpSize(location, spotWidth, spotHeight)
            val cellOffset = type.dpOffset(
                location,
                spotWidth,
                spotHeight,
                route.horizontalCells,
                route.verticalCells
            )
            placeOffset += if (type.isBig) 2 else 1
            Place(type, location, cellOffset, cellSize)
        }
    }
    val alpha by animateFloatAsState(if (state.layer == layer) 1f else 0.7f)
    Box(
        modifier = Modifier.align(Alignment.Center).size(size).alpha(alpha)
    ) {
        places.forEach { place ->
            Box(
                modifier = Modifier
                    .size(width = place.size.width, height = place.size.height)
                    .offset(x = place.offset.x, y = place.offset.y)
            ) {
                PlaceContent(place.type, place.size, place.isVertical)
            }
        }
        val players by players.collectAsState()
        val points = remember(players, route, state.board?.activePlayer) {
            players.filter { layer.level == it.state.level }.map {
                PlayerPointState(
                    position = moveTo(it.state.position, layer.cellCount, route.offset),
                    color = it.attrs.color,
                    level = it.state.level,
                    name = it.playerCard.profession,
                    player = it,
                    isCurrentPlayer = it.id == currentPlayerId,
                    isActivePlayer = it.id == state.board?.activePlayer,
                )
            }
        }
        points.groupBy { it.position }.forEach { (_, gPoints) ->
            gPoints.forEachIndexed { index, pointerState ->
                PlayerPoint(
                    places = places,
                    pointerState = pointerState,
                    state = state,
                    dispatch = dispatch,
                    spotSize = DpSize(spotWidth, spotHeight),
                    index = index,
                    count = gPoints.size
                )
            }
        }
    }
}