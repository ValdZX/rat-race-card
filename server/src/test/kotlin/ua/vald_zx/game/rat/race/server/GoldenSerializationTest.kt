package ua.vald_zx.game.rat.race.server

import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GoldenSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun legacyBoardFixtureLoadsWithRulesVersionOne() {
        val board = json.decodeFromString(Board.serializer(), fixture("board-v0.json"))

        assertEquals("fixture-board", board.id)
        assertEquals(listOf(1, 2), board.cards[BoardCardType.Chance])
        assertEquals(CURRENT_RULES_VERSION, board.rulesVersion)
    }

    @Test
    fun legacyPlayerFixtureStillLoads() {
        val player = json.decodeFromString(Player.serializer(), fixture("player-v0.json"))

        assertEquals("fixture-player", player.id)
        assertEquals(12_000, player.cash)
        assertEquals(BusinessType.WORK, player.businesses.single().type)
    }

    @Test
    fun legacyCardFixtureStillLoads() {
        val card = json.decodeFromString(BoardCard.serializer(), fixture("card-v0.json"))

        assertIs<BoardCard.Chance.Estate>(card)
        assertEquals(75_000, card.price)
    }

    @Test
    fun legacyEventFixtureStillLoads() {
        val event = json.decodeFromString(Event.serializer(), fixture("event-v0.json"))

        assertEquals(Event.SubCash(2_500), event)
    }

    private fun fixture(name: String): String = checkNotNull(
        javaClass.getResource("/fixtures/v0/$name")
    ).readText()
}
