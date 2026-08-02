package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.design.DesignInitPlayerContent
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.GenderSelector
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.cards.menProfessionCards
import ua.vald_zx.game.rat.race.card.screen.board.cards.womenProfessionCards
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Gender

class InitPlayerScreen(private val board: Board) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val colorState = remember { mutableStateOf(0L) }
        var designName by remember { mutableStateOf("") }
        var designGender by remember { mutableStateOf(Gender.MALE) }
        if (designV2Enabled.value) {
            DesignInitPlayerContent(
                colorState = colorState,
                playerName = designName,
                onNameChange = { designName = it },
                gender = designGender,
                onGenderChange = { designGender = it },
                onBack = { navigator.pop() },
                onNext = {
                    coroutineScope.launch {
                        val card = when (designGender) {
                            Gender.MALE -> menProfessionCards.random()
                            Gender.FEMALE -> womenProfessionCards.random()
                        }
                        navigator.push(
                            ProfessionScreen(
                                board = board,
                                card = card,
                                playerName = designName,
                                color = colorState.value,
                            )
                        )
                    }
                },
            )
        } else {
            LegacyInitPlayerContent(
                board = board,
                colorState = colorState,
                onBack = { navigator.pop() },
            )
        }
    }
}
