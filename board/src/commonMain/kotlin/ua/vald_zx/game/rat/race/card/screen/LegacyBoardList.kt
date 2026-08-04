package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.NavigationBackButton
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.components.TextButton
import ua.vald_zx.game.rat.race.card.dateFullDotsFormat
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions

@Composable
internal fun LegacyBoardList(
    boards: List<BoardId>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (BoardId) -> Unit,
    onDelete: (BoardId) -> Unit = {},
) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavigationBackButton(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = onBack,
                )
                Button(
                    enabled = !isLoading,
                    text = stringResource(Res.string.new_table)
                ) {
                    onCreate()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(boards) { board ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpen(board) }
                            ) {
                                Text(text = board.name)
                                Text(text = board.createDateTime.format(dateFullDotsFormat))
                                Text(
                                    text = stringResource(
                                        Res.string.active_and_inactive_players_count,
                                        board.activePlayerCount,
                                        board.inactivePlayerCount,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (board.canDelete) {
                                TextButton(stringResource(Res.string.delete_table)) { onDelete(board) }
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
}

@Composable
internal fun LegacyNewBoardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long, Long, Boolean, OuterCircleConditions, VictoryConditions, BoardGeneration) -> Unit,
) {
            Dialog(onDismissRequest = { onDismiss() }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    var boardName by remember { mutableStateOf("") }
                    val loanLimit = remember { mutableStateOf(TextFieldValue("10000")) }
                    val businessLimit = remember { mutableStateOf(TextFieldValue("10")) }
                    var transportMovementBonusEnabled by remember { mutableStateOf(true) }
                    val minimumCashFlow = remember { mutableStateOf(TextFieldValue("50000")) }
                    val minimumAccountBalance = remember { mutableStateOf(TextFieldValue("200000")) }
                    var apartmentRequired by remember { mutableStateOf(true) }
                    var carRequired by remember { mutableStateOf(true) }
                    val victoryAccountBalance = remember { mutableStateOf(TextFieldValue("10000000")) }
                    var dreamRequired by remember { mutableStateOf(true) }
                    var planeRequired by remember { mutableStateOf(true) }
                    var estateRequired by remember { mutableStateOf(true) }
                    var generateBoard by remember { mutableStateOf(false) }
                    var worldTheme by remember { mutableStateOf("") }
                    var worldLocality by remember { mutableStateOf("") }
                    var worldEpoch by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = boardName,
                        singleLine = true,
                        label = { Text(stringResource(Res.string.table_name)) },
                        onValueChange = { boardName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(Res.string.generated_deck),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = generateBoard,
                            onCheckedChange = { generateBoard = it },
                        )
                        Text(
                            text = stringResource(Res.string.generate_cards),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    if (generateBoard) {
                        OutlinedTextField(
                            value = worldTheme,
                            onValueChange = { worldTheme = it },
                            label = { Text(stringResource(Res.string.world_theme)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = worldLocality,
                            onValueChange = { worldLocality = it },
                            label = { Text(stringResource(Res.string.world_locality)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = worldEpoch,
                            onValueChange = { worldEpoch = it },
                            label = { Text(stringResource(Res.string.world_epoch)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (!generateBoard) {
                    NumberTextField(
                        input = loanLimit,
                        inputLabel = stringResource(Res.string.loanLimit),
                    )
                    NumberTextField(
                        input = businessLimit,
                        inputLabel = stringResource(Res.string.businessLimit),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = transportMovementBonusEnabled,
                            onCheckedChange = { transportMovementBonusEnabled = it },
                        )
                        Text(
                            text = stringResource(Res.string.transport_movement_bonus),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        text = stringResource(Res.string.outer_circle_conditions),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    NumberTextField(
                        input = minimumCashFlow,
                        inputLabel = stringResource(Res.string.minimum_cash_flow),
                    )
                    NumberTextField(
                        input = minimumAccountBalance,
                        inputLabel = stringResource(Res.string.minimum_account_balance),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = apartmentRequired,
                            onCheckedChange = { apartmentRequired = it },
                        )
                        Text(stringResource(Res.string.apartment_required))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = carRequired,
                            onCheckedChange = { carRequired = it },
                        )
                        Text(stringResource(Res.string.car_required))
                    }
                    Text(
                        text = stringResource(Res.string.victory_conditions),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    NumberTextField(
                        input = victoryAccountBalance,
                        inputLabel = stringResource(Res.string.victory_account_balance),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = dreamRequired,
                            onCheckedChange = { dreamRequired = it },
                        )
                        Text(stringResource(Res.string.dream_required))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = planeRequired,
                            onCheckedChange = { planeRequired = it },
                        )
                        Text(stringResource(Res.string.plane_required))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = estateRequired,
                            onCheckedChange = { estateRequired = it },
                        )
                        Text(stringResource(Res.string.estate_required))
                    }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(stringResource(Res.string.cancel)) {
                            onDismiss()
                        }
                        TextButton(
                            stringResource(Res.string.create_table),
                            enabled = boardName.isNotEmpty()
                                    && (generateBoard || listOf(
                                        loanLimit,
                                        businessLimit,
                                        minimumCashFlow,
                                        minimumAccountBalance,
                                        victoryAccountBalance,
                                    ).all { it.value.text.isNotEmpty() })
                                    && (!generateBoard || listOf(worldTheme, worldLocality, worldEpoch).all { it.isNotBlank() })
                        ) {
                            onCreate(
                                boardName,
                                loanLimit.value.text.toLong(),
                                businessLimit.value.text.toLong(),
                                transportMovementBonusEnabled,
                                OuterCircleConditions(
                                    minimumCashFlow = minimumCashFlow.value.text.toLong(),
                                    apartmentRequired = apartmentRequired,
                                    carRequired = carRequired,
                                    minimumAccountBalance = minimumAccountBalance.value.text.toLong(),
                                ),
                                VictoryConditions(
                                    dreamRequired = dreamRequired,
                                    planeRequired = planeRequired,
                                    estateRequired = estateRequired,
                                    minimumAccountBalance = victoryAccountBalance.value.text.toLong(),
                                ),
                                BoardGeneration(
                                    enabled = generateBoard,
                                    theme = worldTheme.trim(),
                                    locality = worldLocality.trim(),
                                    epoch = worldEpoch.trim(),
                                ),
                            )
                        }
                    }
                }
            }
}
