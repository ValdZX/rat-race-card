package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.player_name_label
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.GenderSelector
import ua.vald_zx.game.rat.race.card.screen.board.cards.menProfessionCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.womenProfessionCards
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Gender

class InitPlayerScreen(private val board: Board) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val colorState = remember { mutableStateOf(0L) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ColorsSelector(colorState)
            }
            var playerName by remember { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.player_name_label)) },
                value = playerName,
                onValueChange = { playerName = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            var currentGender by remember { mutableStateOf(Gender.MALE) }
            GenderSelector(
                selected = currentGender,
                iconSize = 100.dp,
                femaleStyle = GenderOptionStyle.FemaleDefault.copy(
                    effectOrigin = TransformOrigin(1f, 0.5f),
                    fillColor = Color(colorState.value)
                ),
                maleStyle = GenderOptionStyle.MaleDefault.copy(
                    effectOrigin = TransformOrigin(0f, 0.5f),
                    fillColor = Color(colorState.value)
                ),
                onGenderChange = {
                    currentGender = it
                }
            )
            Button("Далі", enabled = playerName.isNotEmpty()) {
                coroutineScope.launch {
                    val card = when (currentGender) {
                        Gender.MALE -> menProfessionCards.random()
                        Gender.FEMALE -> womenProfessionCards.random()
                    }
                    navigator.push(ProfessionScreen(board, card, playerName, colorState.value))
                }
            }
        }
    }
}