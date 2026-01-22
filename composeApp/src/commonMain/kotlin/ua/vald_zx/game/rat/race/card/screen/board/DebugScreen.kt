package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.sebastianneubauer.jsontree.JsonTree
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel

class DebugScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer(verticalScrollState = null) {
            val boardText = remember(state.board) {
                val prettyJson = Json { prettyPrint = true }
                prettyJson.encodeToString(state.board)
            }
            JsonTree(
                json = boardText,
                onLoading = { Text(text = "Loading...") }
            )
            var position by remember { mutableStateOf(state.player.location.position.toString()) }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Номер позиції") },
                value = position,
                onValueChange = { position = it.toIntOrNull()?.toString() ?: "" },
            )
            Button(text = "Перейти", enabled = position.isNotBlank()) {
                vm.changePosition(position.toInt())
                bottomSheetNavigator.hide()
            }
            if (state.currentPlayerIsActive && state.board.canTakeCard != null) {
                var cardNo by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Номер картки") },
                    value = cardNo,
                    onValueChange = { cardNo = it.toIntOrNull()?.toString() ?: "" },
                )
                Button(text = "Вибрати", enabled = cardNo.isNotBlank()) {
                    vm.selectCardByNo(cardNo.toInt())
                    bottomSheetNavigator.hide()
                }
            }
        }
    }
}