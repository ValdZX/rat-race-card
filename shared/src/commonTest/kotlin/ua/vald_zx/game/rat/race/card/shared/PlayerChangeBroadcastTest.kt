package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlayerChangeBroadcastTest {
    @Test
    fun movingAPlayerAnnouncesTheChange() {
        assertEveryMutatedPlayerIsAnnounced(snapshot(), GameCommand.MoveTo(3))
    }

    @Test
    fun completingARollAnnouncesTheChange() {
        val rolled = apply(snapshot(), "roll", GameCommand.RollDice("nonce"))
        assertEveryMutatedPlayerIsAnnounced(rolled, GameCommand.CompleteRoll)
    }

    @Test
    fun skippingARestingPlayerAnnouncesTheDecrement() {
        val resting = snapshot().let { initial ->
            initial.copy(players = initial.players.map { if (it.id == "q") it.copy(inRest = 2) else it })
        }
        assertEveryMutatedPlayerIsAnnounced(resting, GameCommand.AdvanceTurn)
    }

    @Test
    fun enteringTheNextTrackAnnouncesTheChange() {
        val ready = snapshot().let { initial ->
            initial.copy(
                board = initial.board.copy(
                    outerCircleConditions = OuterCircleConditions(
                        minimumCashFlow = 0,
                        minimumAccountBalance = 0,
                        apartmentRequired = false,
                        carRequired = false,
                    ),
                ),
            )
        }
        val transition = ready.board.availableTransition(ready.players.first { it.id == "p" }, ready.board.canRoll)
        if (transition != null) {
            assertEveryMutatedPlayerIsAnnounced(ready, GameCommand.EnterTransition(transition.id))
        }
    }

    @Test
    fun aCardThatTouchesEveryPlayerAnnouncesEveryOne() {
        val initial = snapshot().let { base ->
            base.copy(players = base.players.map { it.copy(deputies = 3) })
        }
        val definition = requireNotNull(
            BoardCard.EventStore.Reelection("Перевибори").toCardDefinition(CardLink(BoardCardType.EventStore, 1)),
        )

        val announced = assertEveryMutatedPlayerIsAnnounced(initial, GameCommand.StartCard(definition))

        assertEquals(
            setOf("p", "q"),
            announced,
            "перевибори змінюють усіх, тому всі мають бути оголошені",
        )
    }

    private fun assertEveryMutatedPlayerIsAnnounced(
        initial: GameSnapshot,
        command: GameCommand,
    ): Set<String> {
        val execution = GameEngine(DefaultGameRandom).execute(
            initial,
            GameCommandEnvelope("cmd", "b", initial.board.activePlayerId, initial.board.revision, command),
        )
        val applied = assertIs<GameExecution.Applied>(execution)
        val mutated = applied.snapshot.players
            .filter { updated -> initial.players.first { it.id == updated.id } != updated }
            .map { it.id }
            .toSet()
        val announced = applied.result.events
            .filterIsInstance<DomainEvent.PlayerChanged>()
            .map { it.player.id }
            .toSet()

        assertTrue(
            announced.containsAll(mutated),
            "змінено ${mutated - announced} без DomainEvent.PlayerChanged — інші гравці не дізнаються",
        )
        return announced
    }

    private fun apply(initial: GameSnapshot, id: String, command: GameCommand): GameSnapshot {
        val execution = GameEngine(DefaultGameRandom).execute(
            initial,
            GameCommandEnvelope(id, "b", initial.board.activePlayerId, initial.board.revision, command),
        )
        return assertIs<GameExecution.Applied>(execution).snapshot
    }

    private fun snapshot() = GameSnapshot(
        board = Board(
            id = "b",
            name = "b",
            loanLimit = 100_000,
            businessLimit = 5,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("p", "q"),
            activePlayerId = "p",
        ),
        players = listOf(
            Player(id = "p", boardId = "b", attrs = PlayerAttributes(color = 0)),
            Player(id = "q", boardId = "b", attrs = PlayerAttributes(color = 1)),
        ),
    )
}
