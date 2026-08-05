package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationProgress
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationStage
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerationProgressBroadcastTest {

    private fun board(
        progress: BoardGenerationProgress = BoardGenerationProgress(),
        texts: Map<String, GeneratedText> = emptyMap(),
    ) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = mapOf(BoardCardType.Deputy to listOf(1)),
        generationProgress = progress,
        generatedTexts = texts,
    )

    private fun running(completed: Int) = BoardGenerationProgress(
        stage = BoardGenerationStage.TEXTS,
        completed = completed,
        total = 10,
        isRunning = true,
    )

    @Test
    fun aProgressOnlyStepIsNotBroadcast() {
        val previous = board(progress = running(1))
        val next = board(progress = running(2))

        assertTrue(next.differsOnlyByGenerationProgress(previous))
    }

    @Test
    fun newTextsAreBroadcastEvenWithinTheSameStage() {
        val previous = board(progress = running(1))
        val next = board(
            progress = running(2),
            texts = mapOf("uk" to GeneratedText(cards = mapOf(BoardCardType.Deputy to mapOf(1 to CardText("н", "о"))))),
        )

        assertFalse(next.differsOnlyByGenerationProgress(previous))
    }

    @Test
    fun becomingReadyIsBroadcastEvenWhenNothingElseChanged() {
        val previous = board(progress = running(9))
        val next = board(progress = BoardGenerationProgress(stage = BoardGenerationStage.READY))

        assertFalse(next.differsOnlyByGenerationProgress(previous))
    }

    @Test
    fun failingIsBroadcastEvenWhenNothingElseChanged() {
        val previous = board(progress = running(9))
        val next = board(
            progress = BoardGenerationProgress(stage = BoardGenerationStage.FAILED, error = "quota"),
        )

        assertFalse(next.differsOnlyByGenerationProgress(previous))
    }
}
