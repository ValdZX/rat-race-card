package ua.vald_zx.game.rat.race.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoldenSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun legacyBoardFixtureLoadsWithRulesVersionOne() {
        val migration = BoardSnapshotMigrator.decode(fixture("v0/board-v0.json"))
        val board = migration.board

        assertEquals("fixture-board", board.id)
        assertEquals(listOf(1, 2), board.cards[BoardCardType.Chance])
        assertEquals(CURRENT_SCHEMA_VERSION, board.schemaVersion)
        assertEquals(CURRENT_RULES_VERSION, board.rulesVersion)
        assertEquals(standardContentPackVersions(), board.contentPackVersions)
        assertEquals(0, board.revision)
        assertTrue(migration.migrated)
        assertEquals(
            json.decodeFromString<JsonObject>(fixture("v1/board-v1.json")),
            migration.payload,
        )
    }

    @Test
    fun currentBoardSnapshotRoundTripsWithoutMigration() {
        val migrated = BoardSnapshotMigrator.decode(fixture("v0/board-v0.json")).board

        val restored = BoardSnapshotMigrator.decode(BoardSnapshotMigrator.encode(migrated))

        assertFalse(restored.migrated)
        assertEquals(migrated, restored.board)
    }

    @Test
    fun legacyPlayerFixtureStillLoads() {
        val player = json.decodeFromString(Player.serializer(), fixture("v0/player-v0.json"))

        assertEquals("fixture-player", player.id)
        assertEquals(12_000, player.cash)
        assertEquals(BusinessType.WORK, player.businesses.single().type)
    }

    @Test
    fun legacyCardFixtureStillLoads() {
        val card = json.decodeFromString(BoardCard.serializer(), fixture("v0/card-v0.json"))

        assertIs<BoardCard.Chance.Estate>(card)
        assertEquals(75_000, card.price)
    }

    @Test
    fun legacyEventFixtureStillLoads() {
        val event = json.decodeFromString(Event.serializer(), fixture("v0/event-v0.json"))

        assertEquals(Event.SubCash(2_500), event)
    }

    private fun fixture(name: String): String = checkNotNull(
        javaClass.getResource("/fixtures/$name")
    ).readText()
}
