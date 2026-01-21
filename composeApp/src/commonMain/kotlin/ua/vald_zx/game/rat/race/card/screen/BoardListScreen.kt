package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.components.TextButton
import ua.vald_zx.game.rat.race.card.dateFullDotsFormat
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.RaceRatService

class BoardListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val service = koinInject<RaceRatService>()
        val navigator = LocalNavigator.currentOrThrow
        var boardList by remember { mutableStateOf(emptyList<BoardId>()) }
        var newBoardDialog by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            boardList = service.getBoards()
            service.observeBoards().onEach {
                boardList = it
            }.launchIn(this)
        }
        Box {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { navigator.pop() },
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
                Button(stringResource(Res.string.new_table)) {
                    newBoardDialog = true
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(boardList) { board ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    launchWithHandler({ navigator.replaceAll(LoadOnlineScreen()) }) {
                                        val selectedBoard = service.selectBoard(board.id)
                                        navigator.push(InitPlayerScreen(selectedBoard))
                                    }
                                }.padding(8.dp)
                        ) {
                            Text(text = board.name)
                            Text(text = board.createDateTime.format(dateFullDotsFormat))
                        }
                    }
                }
            }
        }
        if (newBoardDialog) {
            Dialog(onDismissRequest = { newBoardDialog = false }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(stringResource(Res.string.cancel)) {
                            newBoardDialog = false
                        }
                        TextButton(
                            stringResource(Res.string.create_table),
                            enabled = boardName.isNotEmpty()
                                    && loanLimit.value.text.isNotEmpty()
                                    && businessLimit.value.text.isNotEmpty()
                        ) {
                            launchWithHandler({ navigator.replaceAll(LoadOnlineScreen()) }) {
                                val board = service.createBoard(
                                    name = boardName,
                                    loanLimit = loanLimit.value.text.toLong(),
                                    businessLimit = businessLimit.value.text.toLong(),
                                    decks = decks.map { (type, map) ->
                                        type to map.size
                                    }.toMap()
                                )
                                navigator.push(InitPlayerScreen(board))
                            }
                        }
                    }
                }
            }
        }
    }
}