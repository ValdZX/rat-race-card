package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.sebastianneubauer.jsontree.JsonTree
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.apartment
import ua.vald_zx.game.rat.race.card.resources.big_business
import ua.vald_zx.game.rat.race.card.resources.car
import ua.vald_zx.game.rat.race.card.resources.cash
import ua.vald_zx.game.rat.race.card.resources.chance
import ua.vald_zx.game.rat.race.card.resources.debug_apply_changes
import ua.vald_zx.game.rat.race.card.resources.debug_assets
import ua.vald_zx.game.rat.race.card.resources.debug_card_auto_move
import ua.vald_zx.game.rat.race.card.resources.debug_cards
import ua.vald_zx.game.rat.race.card.resources.debug_finances
import ua.vald_zx.game.rat.race.card.resources.debug_go_to_salary
import ua.vald_zx.game.rat.race.card.resources.debug_hide_board_state
import ua.vald_zx.game.rat.race.card.resources.debug_inner_circle
import ua.vald_zx.game.rat.race.card.resources.debug_outer_circle
import ua.vald_zx.game.rat.race.card.resources.debug_player_position
import ua.vald_zx.game.rat.race.card.resources.debug_pets
import ua.vald_zx.game.rat.race.card.resources.debug_show_board_state
import ua.vald_zx.game.rat.race.card.resources.debug_tools
import ua.vald_zx.game.rat.race.card.resources.deposit
import ua.vald_zx.game.rat.race.card.resources.deputies
import ua.vald_zx.game.rat.race.card.resources.estate
import ua.vald_zx.game.rat.race.card.resources.expenses
import ua.vald_zx.game.rat.race.card.resources.go_to
import ua.vald_zx.game.rat.race.card.resources.kids
import ua.vald_zx.game.rat.race.card.resources.loan
import ua.vald_zx.game.rat.race.card.resources.market
import ua.vald_zx.game.rat.race.card.resources.medium_business
import ua.vald_zx.game.rat.race.card.resources.plane
import ua.vald_zx.game.rat.race.card.resources.select_action
import ua.vald_zx.game.rat.race.card.resources.shopping
import ua.vald_zx.game.rat.race.card.resources.small_business
import ua.vald_zx.game.rat.race.card.resources.yacht
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.DebugPlayerValues
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.dreamById
import ua.vald_zx.game.rat.race.card.shared.hasPlaceFor
import ua.vald_zx.game.rat.race.card.shared.placesOf
import ua.vald_zx.game.rat.race.card.shared.nextPositionOf
import ua.vald_zx.game.rat.race.card.shared.resolvedTracks
import kotlin.math.roundToInt
import androidx.compose.ui.text.intl.Locale

private val debugJson = Json { prettyPrint = true }

class DebugScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val player = state.player
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val cash = remember(player.id) { mutableStateOf(TextFieldValue(player.cash.toString())) }
        val deposit = remember(player.id) {
            mutableStateOf(TextFieldValue(player.deposit.toString()))
        }
        val loan = remember(player.id) { mutableStateOf(TextFieldValue(player.loan.toString())) }
        var babies by remember(player.id) { mutableStateOf(player.babies) }
        var cars by remember(player.id) { mutableStateOf(player.cars) }
        var apartments by remember(player.id) { mutableStateOf(player.apartment) }
        var estates by remember(player.id) { mutableStateOf(player.cottage) }
        var yachts by remember(player.id) { mutableStateOf(player.yacht) }
        var planes by remember(player.id) { mutableStateOf(player.flight) }
        var animals by remember(player.id) { mutableStateOf(player.animal) }
        var selectedTrackId by remember(player.id) { mutableStateOf(state.trackId) }
        var selectedPosition by remember(player.id) {
            mutableStateOf(player.location.position.coerceIn(0, state.places.lastIndex))
        }
        var selectedCardType by remember {
            mutableStateOf(state.board.canTakeCard.firstOrNull() ?: BoardCardType.Chance)
        }
        var selectedCardId by remember {
            mutableStateOf(decks.getValue(selectedCardType).keys.minOrNull() ?: 1)
        }
        var showBoardState by remember { mutableStateOf(false) }

        ClosableBottomSheetContainer {
            Text(
                text = stringResource(Res.string.debug_tools),
                style = Design.type.title,
                color = Design.scaffold.onSurface,
            )

            DebugSectionTitle(stringResource(Res.string.debug_finances))
            NumberTextField(input = cash, inputLabel = stringResource(Res.string.cash))
            NumberTextField(input = deposit, inputLabel = stringResource(Res.string.deposit))
            NumberTextField(input = loan, inputLabel = stringResource(Res.string.loan))

            DebugSectionTitle(stringResource(Res.string.debug_assets))
            DebugCounter(stringResource(Res.string.car), cars) { cars = it }
            DebugCounter(stringResource(Res.string.apartment), apartments) { apartments = it }
            DebugCounter(stringResource(Res.string.estate), estates) { estates = it }
            DebugCounter(stringResource(Res.string.yacht), yachts) { yachts = it }
            DebugCounter(stringResource(Res.string.plane), planes) { planes = it }
            DebugCounter(stringResource(Res.string.kids), babies) { babies = it }
            DebugCounter(stringResource(Res.string.debug_pets), animals) { animals = it }

            val financeValues = listOf(cash, deposit, loan).map { it.value.text.toLongOrNull() }
            DesignButton(
                text = stringResource(Res.string.debug_apply_changes),
                modifier = Modifier.fillMaxWidth(),
                enabled = financeValues.all { it != null } && state.currentPlayerIsActive,
            ) {
                vm.debugUpdatePlayer(
                    DebugPlayerValues(
                        cash = financeValues[0] ?: 0,
                        deposit = financeValues[1] ?: 0,
                        loan = financeValues[2] ?: 0,
                        babies = babies,
                        cars = cars,
                        apartment = apartments,
                        cottage = estates,
                        yacht = yachts,
                        flight = planes,
                        animal = animals,
                    )
                )
            }

            DebugSectionTitle(stringResource(Res.string.debug_player_position))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.board.resolvedTracks()) { track ->
                    FilterChip(
                        selected = selectedTrackId == track.id,
                        colors = debugChipColors(),
                        onClick = {
                            selectedTrackId = track.id
                            selectedPosition = if (track.id == state.trackId) {
                                player.location.position.coerceIn(0, track.cells.lastIndex)
                            } else {
                                0
                            }
                        },
                        label = {
                            Text(track.id.value)
                        },
                    )
                }
            }
            PositionSelector(
                board = state.board,
                trackId = selectedTrackId,
                position = selectedPosition,
                onPositionChanged = { selectedPosition = it },
            )
            DesignButton(
                text = stringResource(Res.string.go_to),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canRoll,
            ) {
                vm.debugChangePosition(selectedTrackId, selectedPosition)
            }
            DesignButton(
                text = stringResource(Res.string.debug_go_to_salary),
                modifier = Modifier.fillMaxWidth(),
                kind = DesignButtonKind.Tonal,
                enabled = state.canRoll,
            ) {
                val from = if (selectedTrackId == state.trackId) player.location.position else 0
                val salaryPosition = state.board.placesOf(selectedTrackId).nextPositionOf(PlaceType.Salary, from)
                selectedPosition = salaryPosition
                vm.debugChangePosition(selectedTrackId, salaryPosition)
            }

            DebugSectionTitle(stringResource(Res.string.debug_cards))
            val availableCardTypes = if (state.canRoll) {
                BoardCardType.entries.filter(state.places::hasPlaceFor)
            } else {
                BoardCardType.entries
            }
            LaunchedEffect(availableCardTypes) {
                if (selectedCardType !in availableCardTypes) {
                    selectedCardType = availableCardTypes.first()
                    selectedCardId = decks.getValue(selectedCardType).keys.minOrNull() ?: 1
                }
            }
            if (state.canRoll) {
                Text(
                    text = stringResource(Res.string.debug_card_auto_move),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    style = Design.type.body,
                    color = Design.scaffold.onSurfaceMuted,
                )
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(availableCardTypes) { cardType ->
                    FilterChip(
                        selected = selectedCardType == cardType,
                        colors = debugChipColors(),
                        onClick = {
                            selectedCardType = cardType
                            selectedCardId = decks.getValue(cardType).keys.minOrNull() ?: 1
                        },
                        label = { Text(cardType.localizedName()) },
                    )
                }
            }
            val selectedDeck = decks.getValue(selectedCardType)
            val cardIds = selectedDeck.keys.sorted()
            val selectedCard = selectedDeck[selectedCardId]
            ValueSelector(
                value = selectedCardId,
                previousEnabled = cardIds.indexOf(selectedCardId) > 0,
                nextEnabled = cardIds.indexOf(selectedCardId) in 0..<cardIds.lastIndex,
                onPrevious = {
                    val index = cardIds.indexOf(selectedCardId)
                    if (index > 0) selectedCardId = cardIds[index - 1]
                },
                onNext = {
                    val index = cardIds.indexOf(selectedCardId)
                    if (index in 0..<cardIds.lastIndex) selectedCardId = cardIds[index + 1]
                },
            )
            selectedCard?.let {
                Text(
                    text = it.debugDescription(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    style = Design.type.body,
                    color = Design.scaffold.onSurfaceMuted,
                )
            }
            DesignButton(
                text = stringResource(Res.string.select_action),
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCard != null && state.currentPlayerIsActive,
            ) {
                vm.selectCardByNo(selectedCardId, selectedCardType)
                bottomSheetNavigator.hide()
            }

            HorizontalDivider(color = Design.scaffold.outline)
            DesignButton(
                text = stringResource(
                    if (showBoardState) {
                        Res.string.debug_hide_board_state
                    } else {
                        Res.string.debug_show_board_state
                    }
                ),
                kind = DesignButtonKind.Text,
                onClick = { showBoardState = !showBoardState },
            )
            if (showBoardState) {
                val boardText = remember(state.board) {
                    debugJson.encodeToString(state.board)
                }
                JsonTree(
                    json = boardText,
                    onLoading = {
                        Text(
                            text = "Loading...",
                            style = Design.type.body,
                            color = Design.scaffold.onSurfaceMuted,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DebugSectionTitle(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = Design.scaffold.outline,
        )
        Text(
            text = text,
            style = Design.type.subtitle,
            color = Design.scaffold.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun DebugCounter(
    label: String,
    value: Long,
    onValueChanged: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = Design.type.body, color = Design.scaffold.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperButton("−", enabled = value > 0) {
                onValueChanged((value - 1).coerceAtLeast(0))
            }
            Text(
                text = value.toString(),
                style = Design.type.amountMd,
                color = Design.scaffold.onSurface,
            )
            StepperButton("+", enabled = value < Long.MAX_VALUE) {
                onValueChanged(value + 1)
            }
        }
    }
}

@Composable
private fun debugChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Design.scaffold.surface2,
    labelColor = Design.scaffold.onSurfaceMuted,
    selectedContainerColor = Design.scaffold.accent,
    selectedLabelColor = Design.scaffold.onAccent,
)

@Composable
private fun StepperButton(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(enabled = enabled, onClick = onClick) {
        Text(
            text = glyph,
            style = Design.type.amountMd,
            color = if (enabled) Design.scaffold.accent else Design.scaffold.onSurfaceMuted,
        )
    }
}

@Composable
private fun PositionSelector(
    board: Board,
    trackId: TrackId,
    position: Int,
    onPositionChanged: (Int) -> Unit,
) {
    val places = board.placesOf(trackId)
    val lastPosition = places.lastIndex
    val place = places[position.coerceIn(0, lastPosition)]
    val placeName = when (place) {
        is PlaceType.Desire -> board.dreamById(place.dreamId, Locale.current.language)?.name ?: place.name
        else -> place.name
    }
    Text(
        text = "#$position / $lastPosition · $placeName",
        style = Design.type.label,
        color = Design.scaffold.onSurface,
    )
    Slider(
        value = position.toFloat(),
        onValueChange = { onPositionChanged(it.roundToInt().coerceIn(0, lastPosition)) },
        valueRange = 0f..lastPosition.toFloat(),
        steps = (lastPosition - 1).coerceAtLeast(0),
        colors = SliderDefaults.colors(
            thumbColor = Design.scaffold.accent,
            activeTrackColor = Design.scaffold.accentDim,
            inactiveTrackColor = Design.scaffold.surface3,
            activeTickColor = Design.scaffold.onAccent,
            inactiveTickColor = Design.scaffold.outlineStrong,
        ),
    )
    ValueSelector(
        value = position,
        previousEnabled = position > 0,
        nextEnabled = position < lastPosition,
        onPrevious = { onPositionChanged((position - 1).coerceAtLeast(0)) },
        onNext = { onPositionChanged((position + 1).coerceAtMost(lastPosition)) },
    )
}

@Composable
private fun ValueSelector(
    value: Int,
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton("−", enabled = previousEnabled, onClick = onPrevious)
        Text(
            text = "#$value",
            style = Design.type.amountMd,
            color = Design.scaffold.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        StepperButton("+", enabled = nextEnabled, onClick = onNext)
    }
}

@Composable
private fun BoardCardType.localizedName(): String {
    return stringResource(
        when (this) {
            BoardCardType.Chance -> Res.string.chance
            BoardCardType.SmallBusiness -> Res.string.small_business
            BoardCardType.MediumBusiness -> Res.string.medium_business
            BoardCardType.BigBusiness -> Res.string.big_business
            BoardCardType.Expenses -> Res.string.expenses
            BoardCardType.EventStore -> Res.string.market
            BoardCardType.Shopping -> Res.string.shopping
            BoardCardType.Deputy -> Res.string.deputies
        }
    )
}

private fun BoardCard.debugDescription(): String {
    return when (this) {
        is BoardCard.SmallBusiness -> name
        is BoardCard.MediumBusiness -> name
        is BoardCard.BigBusiness -> name
        is BoardCard.Shopping -> description
        is BoardCard.EventStore.Shares -> description
        is BoardCard.EventStore.Land -> description
        is BoardCard.EventStore.Estate -> description
        is BoardCard.EventStore.BusinessExtending -> description
        is BoardCard.EventStore.Reelection -> description
        is BoardCard.EventStore.Announcement -> description
        is BoardCard.EventStore.CorruptBusiness -> description
        is BoardCard.EventStore.CorruptLand -> description
        is BoardCard.Deputy -> description
        is BoardCard.Chance.RandomJob -> description
        is BoardCard.Chance.Land -> "$name · $description"
        is BoardCard.Chance.Shares -> description
        is BoardCard.Chance.Estate -> "$name · $description"
        is BoardCard.Chance.CorruptBusiness -> description
        is BoardCard.Chance.CorruptLand -> description
        is BoardCard.Expenses -> description
    }
}
