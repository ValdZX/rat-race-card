package ua.vald_zx.game.rat.race.card.logic

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.autoTakeCardEnabled
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalAtomicApi::class)
class AutoTakeCardWiringTest {

    private val player = Player(id = "me", boardId = "b", attrs = PlayerAttributes(0, 0))

    private fun board(canTake: List<BoardCardType>) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        canTakeCard = canTake,
        playerIds = setOf("me"),
        activePlayerId = "me",
    )

    @AfterTest
    fun resetSetting() {
        autoTakeCardEnabled.value = false
    }

    private fun serverCallsFor(canTake: List<BoardCardType>, expectDraw: Boolean): Int {
        val calls = AtomicInt(0)
        runComposeUiTest {
            BoardViewModel(
                board = board(canTake),
                player = player,
                serviceProvider = {
                    calls.fetchAndIncrement()
                    error("офлайн-тест")
                },
                reconnectService = { throw CancellationException("офлайн-тест") },
            )
            if (expectDraw) waitUntil(timeoutMillis = 2_000) { calls.load() > 0 }
            waitForIdle()
        }
        return calls.load()
    }

    @Test
    fun theSettingSendsTheDrawToTheServerOnItsOwn() {
        autoTakeCardEnabled.value = true
        assertEquals(1, serverCallsFor(listOf(BoardCardType.Chance), expectDraw = true))
    }

    @Test
    fun withTheSettingOffNothingIsDrawn() {
        autoTakeCardEnabled.value = false
        assertEquals(0, serverCallsFor(listOf(BoardCardType.Chance), expectDraw = false))
    }

    @Test
    fun aChoiceOfDecksIsNeverDrawnAutomatically() {
        autoTakeCardEnabled.value = true
        assertEquals(0, serverCallsFor(listOf(BoardCardType.Chance, BoardCardType.EventStore), expectDraw = false))
    }
}
