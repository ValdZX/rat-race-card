package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.GenderSelector
import ua.vald_zx.game.rat.race.card.logic.CardAction
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.Gender

class InitPlayerScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val state by raceRate2BoardStore.observeState().collectAsState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ColorsSelector(state, raceRate2BoardStore::dispatch)
            }
            var playerName by remember { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Імʼя гравця") },
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
                    fillColor = Color(state.color)
                ),
                maleStyle = GenderOptionStyle.MaleDefault.copy(
                    effectOrigin = TransformOrigin(0f, 0.5f),
                    fillColor = Color(state.color)
                ),
                onGenderChange = {
                    currentGender = it
                }
            )
            Button("Далі", enabled = playerName.isNotEmpty()) {
                coroutineScope.launch {
                    state.currentPlayer?.playerCard?.let { card ->
                        val professionCard = card.copy(
                            name = playerName,
                            salary = 1234,
                            profession = "work"
                        )
                        service?.updatePlayerCard(professionCard)
                        raceRate2BoardStore.card.dispatch(
                            CardAction.BuyBusiness(
                                Business(
                                    type = BusinessType.WORK,
                                    name = professionCard.profession,
                                    price = 0,
                                    profit = professionCard.salary
                                )
                            )
                        )
                    }
                    navigator.push(Board2Screen())
                }
            }
        }
    }
}