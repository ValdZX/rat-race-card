package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.format
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.TextButton
import ua.vald_zx.game.rat.race.card.dateFullDotsFormat
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.BoardId

class BoardListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var boardList by remember { mutableStateOf(emptyList<BoardId>()) }
        var newBoardDialog by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            boardList = service?.getBoards().orEmpty()
            service?.observeBoards()?.onEach {
                boardList = it
            }?.launchIn(this)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button("Новий стіл") {
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
                                raceRate2BoardStore.dispatch(BoardAction.SelectBoard(board.id))
                            }.padding(8.dp)
                    ) {
                        Text(text = board.name)
                        Text(text = board.createDateTime.format(dateFullDotsFormat))
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
                        boardName,
                        label = { Text("Назва стола") },
                        onValueChange = { boardName = it })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton("Відміна") {
                            newBoardDialog = false
                        }
                        TextButton("Створити стіл", enabled = boardName.isNotEmpty()) {
                            raceRate2BoardStore.dispatch(BoardAction.CreateBoard(boardName))
                        }
                    }
                }
            }
        }
    }
}