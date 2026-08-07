package ua.vald_zx.game.rat.race.card.screen.board

import ua.vald_zx.game.rat.race.card.AppRoute
import ua.vald_zx.game.rat.race.card.RoutedScreen
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
import kotlinx.coroutines.CancellationException
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.design.DesignInitPlayerContent
import ua.vald_zx.game.rat.race.card.screen.design.DesignBoardGenerationContent
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.GenderSelector
import ua.vald_zx.game.rat.race.card.resources.*
import androidx.compose.ui.text.intl.Locale
import ua.vald_zx.game.rat.race.card.screen.board.cards.professionFor
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Gender

class InitPlayerScreen(private val board: Board) : Screen, RoutedScreen {

    override val appRoute: AppRoute get() = AppRoute.Board(board.id)

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val connection = koinInject<RaceRatConnection>()
        var currentBoard by remember { mutableStateOf(board) }
        var generationProgress by remember { mutableStateOf(board.generationProgress) }
        val colorState = remember { mutableStateOf(0L) }
        var designName by remember { mutableStateOf("") }
        var designGender by remember { mutableStateOf(Gender.MALE) }
        val locale = Locale.current.language
        LaunchedEffect(board.id) {
            if (!board.generation.enabled) return@LaunchedEffect
            try {
                connection.service().observeGeneration().collect { progress ->
                    generationProgress = progress
                    if (progress.isReady) {
                        val service = connection.service()
                        val readyBoard = service.getBoard()
                        currentBoard = readyBoard
                        val clientUuid = appKStore.get()?.clientUuid.orEmpty()
                        if (clientUuid.isNotBlank()) {
                            val instance = service.hello(clientUuid, readyBoard.id)
                            instance.player?.let { restoredPlayer ->
                                navigator.replace(BoardScreen(instance.board, restoredPlayer))
                            }
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Napier.e("Observing board generation failed", error)
            }
        }
        if (currentBoard.generation.enabled && !generationProgress.isReady) {
            DesignBoardGenerationContent(
                progress = generationProgress,
                onBack = { navigator.pop() },
                onContinue = {
                    coroutineScope.launch {
                        runCatching { connection.service().continueGeneration() }
                            .onFailure { error -> Napier.e("Continuing board generation failed", error) }
                    }
                },
                onRestart = {
                    coroutineScope.launch {
                        runCatching { connection.service().restartGeneration() }
                            .onFailure { error -> Napier.e("Restarting board generation failed", error) }
                    }
                },
            )
            return
        }
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
                        val card = professionFor(currentBoard, designGender, locale)
                        navigator.push(
                            ProfessionScreen(
                                board = currentBoard,
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
                board = currentBoard,
                colorState = colorState,
                onBack = { navigator.pop() },
            )
        }
    }
}
