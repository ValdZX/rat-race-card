package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import dev.lennartegb.shadows.boxShadow
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Money
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.moveTo
import ua.vald_zx.game.rat.race.card.theme.AppTheme


@Composable
private fun PlaceType.text(): String {
    return when (this) {
        PlaceType.Salary -> stringResource(Res.string.salary)
        PlaceType.Chance -> stringResource(Res.string.chance)
        PlaceType.Store -> stringResource(Res.string.store)
        PlaceType.Business -> stringResource(Res.string.business)
        PlaceType.BigBusiness -> stringResource(Res.string.business)
        PlaceType.Deputy -> stringResource(Res.string.deputy)
        PlaceType.Expenses -> stringResource(Res.string.expenses)
        PlaceType.Shopping -> stringResource(Res.string.shopping)
        PlaceType.Rest -> stringResource(Res.string.rest)
        PlaceType.Desire -> stringResource(Res.string.desire)
        PlaceType.Start -> stringResource(Res.string.start)
        PlaceType.Resignation -> stringResource(Res.string.exaltation)
        PlaceType.Divorce -> stringResource(Res.string.divorce)
        PlaceType.Bankruptcy -> stringResource(Res.string.bankruptcy)
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
        PlaceType.Resignation -> AppTheme.colors.exaltation
    }
}


@Composable
fun BoxScope.PlaceContent(
    place: Place,
    vm: BoardViewModel,
    layer: BoardLayer,
    index: Int,
    route: BoardRoute,
) {
    val minSide = min(place.size.height, place.size.width)
    val maxSide = max(place.size.height, place.size.width)
    val density = LocalDensity.current
    val state by vm.uiState.collectAsState()
    when (place.type) {
        PlaceType.Salary -> {
            val blurRadius = min(place.size.width, place.size.height) / 5
            val spreadRadius = min(place.size.width, place.size.height) / 15
            val salaryPosition = state.player.salaryPosition
            val canTakeSalary = remember(salaryPosition) {
                salaryPosition != null &&
                        layer.level == state.layer.level &&
                        index == moveTo(
                    position = salaryPosition,
                    cellCount = layer.cellCount,
                    toMove = route.offset
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .optionalModifier(canTakeSalary) {
                        boxShadow(
                            blurRadius = blurRadius,
                            spreadRadius = spreadRadius,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    .background(place.type.color())
                    .optionalModifier(canTakeSalary) {
                        clickable {
                            vm.takeSalary()
                        }
                    }
            ) {
                Image(Images.Money, contentDescription = null)
            }
        }

        PlaceType.Child -> {
            Box(
                Modifier.fillMaxSize()
                    .background(place.type.color())
            ) {
                Image(
                    painterResource(Res.drawable.child),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 14)
                )
            }
        }

        PlaceType.Bankruptcy -> {
            Box(Modifier.fillMaxSize().background(place.type.color())) {
                Image(
                    painterResource(Res.drawable.business_failure),
                    contentDescription = null,
                    modifier = Modifier.padding(minSide / 14)
                )
            }
        }

        PlaceType.TaxInspection -> {
            Box(Modifier.fillMaxSize().background(place.type.color())) {
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
                        1.0f to place.type.color(),
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
                        1.0f to place.type.color(),
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
                        1.0f to place.type.color(),
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
                        1.0f to place.type.color(),
                        radius = with(density) { maxSide.toPx() },
                        tileMode = TileMode.Repeated
                    )
                )
            ) {
                OutlinedText(
                    text = place.type.text(),
                    autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                    fontFamily = FontFamily(
                        Font(
                            Res.font.Bubbleboddy,
                            weight = FontWeight.Medium
                        )
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(minSide / 14)
                        .optionalModifier(place.isVertical) {
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
            Box(Modifier.fillMaxSize().background(place.type.color())) {
                OutlinedText(
                    text = place.type.text(),
                    autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                    fontFamily = FontFamily(
                        Font(
                            Res.font.Bubbleboddy,
                            weight = FontWeight.Medium
                        )
                    ),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(minSide / 14)
                        .optionalModifier(place.isVertical) {
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
    vm: BoardViewModel,
    layer: BoardLayer,
    size: DpSize,
    route: BoardRoute,
) {
    val spotWidth = size.width / route.horizontalCells
    val spotHeight = size.height / route.verticalCells
    val places = remember(route.places, size) {
        var placeOffset = 0
        route.places.mapIndexed { index, type ->
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
            index to Place(type, location, cellOffset, cellSize)
        }.sortedBy { (_, place) -> place.type == PlaceType.Salary }
    }
    val state by vm.uiState.collectAsState()
    val alpha by animateFloatAsState(if (state.layer == layer) 1f else 0.7f)
    Box(
        modifier = Modifier.align(Alignment.Center).size(size).alpha(alpha)
    ) {
        places.forEach { (index, place) ->
            Box(
                modifier = Modifier
                    .size(width = place.size.width, height = place.size.height)
                    .offset(x = place.offset.x, y = place.offset.y)
            ) {
                PlaceContent(
                    index = index,
                    place = place,
                    route = route,
                    layer = layer,
                    vm = vm,
                )
            }
        }
        val players by players.collectAsState()
        val points = remember(players, route, state.board.activePlayer) {
            players.filter { layer.level == it.location.level }.map {
                PlayerPointState(
                    position = moveTo(it.location.position, layer.cellCount, route.offset),
                    color = it.attrs.color,
                    level = it.location.level,
                    name = it.card.profession,
                    player = it,
                    isCurrentPlayer = it.id == currentPlayerId,
                    isActivePlayer = it.id == state.board.activePlayer,
                )
            }
        }
        points.groupBy { it.position }.forEach { (_, gPoints) ->
            gPoints.forEachIndexed { index, pointerState ->
                PlayerPoint(
                    places = places.toMap(),
                    pointerState = pointerState,
                    vm = vm,
                    spotSize = DpSize(spotWidth, spotHeight),
                    index = index,
                    count = gPoints.size
                )
            }
        }
    }
}