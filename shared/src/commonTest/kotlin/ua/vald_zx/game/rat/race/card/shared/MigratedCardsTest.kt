package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MigratedCardsTest {
    private val engine = GameEngine(FixedRandom)

    @Test
    fun reelectionClearsEveryDeputyThroughTheEngine() {
        val initial = snapshot(
            player("first").copy(deputies = 3),
            player("second").copy(deputies = 5),
        )
        val definition = assertNotNull(
            BoardCard.EventStore.Reelection("Перевибори").toCardDefinition(link()),
            "перевибори мають мати CardDefinition",
        )

        val applied = execute(initial, GameCommand.StartCard(definition))

        assertTrue(applied.players.all { it.deputies == 0 })
    }

    @Test
    fun businessExtendingRaisesRecurringIncomeThroughTheEngine() {
        val initial = snapshot(
            player("first").copy(
                businesses = listOf(
                    Business(BusinessType.WORK, "Робота", 0, 4_000),
                    Business(BusinessType.SMALL, "Кав'ярня", 20_000, 2_000),
                ),
            ),
            player("second"),
        )
        val definition = assertNotNull(
            BoardCard.EventStore.BusinessExtending("Розширення", 800).toCardDefinition(link()),
        )

        val applied = execute(initial, GameCommand.StartCard(definition))

        val owner = applied.players.first { it.id == "first" }
        assertEquals(listOf(800L), owner.businesses.first { it.type == BusinessType.SMALL }.extentions)
        assertEquals(emptyList(), owner.businesses.first { it.type == BusinessType.WORK }.extentions)
    }

    @Test
    fun anAnnouncementJustPassesTheTurnOn() {
        val initial = snapshot(player("first"), player("second"))
        val definition = assertNotNull(
            BoardCard.EventStore.Announcement("Новина").toCardDefinition(link()),
        )

        val applied = execute(initial, GameCommand.StartCard(definition))

        assertEquals("second", applied.board.activePlayerId, "інформаційна картка має передати хід")
    }

    @Test
    fun everyMigratedCardPassesDefinitionValidation() {
        val validator = CardDefinitionEngine(standardEffectHandlerRegistry())
        val cards = listOf(
            BoardCard.EventStore.Reelection("Перевибори"),
            BoardCard.EventStore.BusinessExtending("Розширення", 800),
            BoardCard.EventStore.Announcement("Новина"),
            BoardCard.SmallBusiness("Малий", "опис", 10_000, 1_000),
            BoardCard.Shopping("опис", 5_000, ShopType.AUTO, ""),
        )

        cards.forEach { card ->
            val definition = assertNotNull(card.toCardDefinition(link()), "${card::class.simpleName} без definition")
            assertIs<ValidationResult.Valid>(
                validator.validate(definition),
                "${card::class.simpleName} не проходить валідацію",
            )
        }
    }

    private fun execute(initial: GameSnapshot, command: GameCommand): GameSnapshot {
        val execution = engine.execute(
            initial,
            GameCommandEnvelope(
                commandId = "card-1",
                boardId = initial.board.id,
                playerId = initial.board.activePlayerId,
                expectedRevision = initial.board.revision,
                command = command,
            ),
        )
        return assertIs<GameExecution.Applied>(execution).snapshot
    }

    private fun link() = CardLink(BoardCardType.EventStore, 1)

    private fun snapshot(vararg players: Player) = GameSnapshot(
        board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 5,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = players.map { it.id }.toSet(),
            activePlayerId = players.first().id,
        ),
        players = players.toList(),
    )

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 1),
    )

    private object FixedRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = from
    }
}
