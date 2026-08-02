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
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.components.TextButton
import ua.vald_zx.game.rat.race.card.dateFullDotsFormat
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.BoardId
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
) {
        Box {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { onBack() },
                content = {
                    Icon(Images.Back, contentDescription = null)
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onOpen(board) }
                                .padding(8.dp)
                        ) {
                            Text(text = board.name)
                            Text(text = board.createDateTime.format(dateFullDotsFormat))
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
    onCreate: (String, Long, Long, Boolean, OuterCircleConditions, VictoryConditions) -> Unit,
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
                    OutlinedTextField(
                        value = boardName,
                        singleLine = true,
                        label = { Text(stringResource(Res.string.table_name)) },
                        onValueChange = { boardName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val loanLimit = remember { mutableStateOf(TextFieldValue("10000")) }
                    NumberTextField(
                        input = loanLimit,
                        inputLabel = stringResource(Res.string.loanLimit),
                    )
                    val businessLimit = remember { mutableStateOf(TextFieldValue("10")) }
                    NumberTextField(
                        input = businessLimit,
                        inputLabel = stringResource(Res.string.businessLimit),
                    )
                    var transportMovementBonusEnabled by remember { mutableStateOf(true) }
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
                    val minimumCashFlow = remember { mutableStateOf(TextFieldValue("50000")) }
                    NumberTextField(
                        input = minimumCashFlow,
                        inputLabel = stringResource(Res.string.minimum_cash_flow),
                    )
                    val minimumAccountBalance = remember { mutableStateOf(TextFieldValue("200000")) }
                    NumberTextField(
                        input = minimumAccountBalance,
                        inputLabel = stringResource(Res.string.minimum_account_balance),
                    )
                    var apartmentRequired by remember { mutableStateOf(true) }
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
                    var carRequired by remember { mutableStateOf(true) }
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
                    val victoryAccountBalance = remember {
                        mutableStateOf(TextFieldValue("10000000"))
                    }
                    NumberTextField(
                        input = victoryAccountBalance,
                        inputLabel = stringResource(Res.string.victory_account_balance),
                    )
                    var dreamRequired by remember { mutableStateOf(true) }
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
                    var planeRequired by remember { mutableStateOf(true) }
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
                    var estateRequired by remember { mutableStateOf(true) }
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
                                    && loanLimit.value.text.isNotEmpty()
                                    && businessLimit.value.text.isNotEmpty()
                                    && minimumCashFlow.value.text.isNotEmpty()
                                    && minimumAccountBalance.value.text.isNotEmpty()
                                    && victoryAccountBalance.value.text.isNotEmpty()
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
                            )
                        }
                    }
                }
            }
}
