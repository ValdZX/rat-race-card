package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CellRuleRegistryTest {
    @Test
    fun everyLegacyPlaceHasARegisteredRuleAndRoundTrips() {
        val registry = legacyCellRuleRegistry()
        val cells = (inPlaces + outPlaces).mapIndexed { index, place -> place.toCellInstance("cell-$index") }

        assertEquals(ValidationResult.Valid, registry.validate(cells))
        assertTrue(CoreCellTypes.all.all { it in registry.registeredTypes })
        assertEquals(inPlaces + outPlaces, cells.map(CellInstance::toPlaceType))
    }

    @Test
    fun externalCellConnectsThroughRegistrationOnly() {
        val bonusType = CellTypeId("test.bonus")
        val bonusRule = object : CellRule {
            override val type = bonusType

            override fun onPass(context: TurnContext, cell: CellInstance): RuleResult {
                return context.updatePlayer { it.copy(cash = it.cash + 10) }.result
            }

            override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
                return context.updatePlayer { it.copy(cash = it.cash + 777) }.endTurn().result
            }
        }
        val presentation = CellPresentation(
            type = bonusType,
            titleKey = "test.bonus.title",
            iconKey = "test_bonus",
            colorToken = "test.bonus",
            soundId = "bonus",
        )
        val registry = legacyCellRuleRegistry() + bonusRule
        val presentationRegistry = legacyCellPresentationRegistry + presentation
        val initial = snapshotWithBonusCell(bonusType)
        val engine = GameEngine(FixedGameRandom(3), cellRules = registry)

        val rolled = engine.execute(
            initial,
            command(initial, "roll", GameCommand.RollDice("nonce")),
        ).applied().snapshot
        val completed = engine.execute(
            rolled,
            command(rolled, "complete", GameCommand.CompleteRoll),
        ).applied().snapshot

        assertEquals(787, completed.players.first { it.id == "first" }.cash)
        assertEquals("second", completed.board.activePlayerId)
        assertEquals(presentation, presentationRegistry.presentation(bonusType))
    }

    @Test
    fun definitionsAndCustomParametersRoundTripThroughJson() {
        val definition = BoardDefinition(
            id = "definition",
            rulesVersion = CURRENT_RULES_VERSION,
            tracks = listOf(BoardLayer.INNER.defaultTrackDefinition()),
        )

        val encoded = Json.encodeToString(definition)

        assertEquals(definition, Json.decodeFromString<BoardDefinition>(encoded))
    }

    @Test
    fun missingRuleIsReportedBeforeGameStarts() {
        val cell = CellInstance("missing", CellTypeId("test.missing"))

        val invalid = assertIs<ValidationResult.Invalid>(legacyCellRuleRegistry().validate(listOf(cell)))

        assertTrue(invalid.errors.single().contains("not registered"))
    }

    private fun snapshotWithBonusCell(type: CellTypeId): GameSnapshot {
        val track = BoardLayer.INNER.defaultTrackDefinition()
        val cells = track.cells.toMutableList().apply {
            this[4] = CellInstance("bonus", type)
        }
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 10_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("first", "second"),
            activePlayerId = "first",
            trackDefinitions = mapOf(BoardLayer.INNER to track.copy(cells = cells)),
        )
        return GameSnapshot(
            board,
            listOf(
                player("first", position = 1),
                player("second", position = 1),
            ),
        )
    }

    private fun player(id: String, position: Int) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(0),
        location = PlayerLocation(position),
    )

    private fun command(snapshot: GameSnapshot, id: String, command: GameCommand) = GameCommandEnvelope(
        commandId = id,
        boardId = snapshot.board.id,
        playerId = snapshot.board.activePlayerId,
        expectedRevision = snapshot.board.revision,
        command = command,
    )

    private fun GameExecution.applied(): RuleResult = assertIs<GameExecution.Applied>(this).result

    private class FixedGameRandom(private val value: Int) : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = value
    }
}
