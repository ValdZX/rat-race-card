package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DynamicTracksTest {
    @Test
    fun legacyLevelsMigrateToStableTrackIds() {
        val inner = Json.decodeFromString<PlayerLocation>("""{"position":4,"level":0}""")
        val outer = Json.decodeFromString<PlayerLocation>("""{"position":7,"level":1}""")

        assertEquals(CoreTrackIds.Inner, inner.trackId)
        assertEquals(CoreTrackIds.Outer, outer.trackId)
        assertEquals("""{"position":4,"trackId":"inner"}""", Json.encodeToString(inner))
        assertFailsWith<IllegalStateException> {
            Json.decodeFromString<PlayerLocation>("""{"position":1,"level":9}""")
        }
    }

    @Test
    fun threeTracksCompleteLapsAndTwoDeclarativeTransitions() {
        val tracks = listOf(
            track("inner", 0, 7),
            track("outer", 1, 11),
            track("elite", 2, 5),
        )
        val board = board(tracks).copy(
            transitions = listOf(
                TrackTransition("inner-outer", TrackId("inner"), TrackId("outer"), entryCellIndex = 2),
                TrackTransition("outer-elite", TrackId("outer"), TrackId("elite"), entryCellIndex = 1),
            ),
        )
        val engine = GameEngine(FixedRandom)
        var snapshot = GameSnapshot(board, listOf(player()))

        tracks.forEach { track ->
            val positioned = snapshot.player("player").copy(location = PlayerLocation(3 % track.cells.size, track.id))
            snapshot = snapshot.copy(players = listOf(positioned))
            assertEquals(positioned.location.position, moveTo(positioned.location.position, track.cells.size, track.cells.size))
        }

        snapshot = snapshot.copy(players = listOf(player()))
        snapshot = engine.execute(
            snapshot,
            command(snapshot, "transition-1", GameCommand.EnterTransition("inner-outer")),
        ).applied().snapshot
        assertEquals(PlayerLocation(2, TrackId("outer")), snapshot.player("player").location)

        snapshot = engine.execute(
            snapshot,
            command(snapshot, "transition-2", GameCommand.EnterTransition("outer-elite")),
        ).applied().snapshot
        assertEquals(PlayerLocation(1, TrackId("elite")), snapshot.player("player").location)
    }

    @Test
    fun unknownTrackIsCompatibilityError() {
        val board = board(listOf(track("inner", 0, 7)))

        assertFailsWith<IllegalStateException> { board.track(TrackId("missing")) }
        assertFailsWith<IllegalStateException> { 42.toLayer() }
    }

    private fun board(tracks: List<TrackDefinition>) = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf("player"),
        activePlayerId = "player",
        tracks = tracks,
    )

    private fun track(id: String, order: Int, cells: Int) = TrackDefinition(
        id = TrackId(id),
        order = order,
        cells = List(cells) { index -> PlaceType.Start.toCellInstance("$id-$index") },
        visual = TrackVisualHint(horizontalCells = cells + 4, verticalCells = cells + 2),
    )

    private fun player() = Player(
        id = "player",
        boardId = "board",
        attrs = PlayerAttributes(0),
        location = PlayerLocation(1, TrackId("inner")),
    )

    private fun command(snapshot: GameSnapshot, id: String, value: GameCommand) = GameCommandEnvelope(
        commandId = id,
        boardId = "board",
        playerId = "player",
        expectedRevision = snapshot.board.revision,
        command = value,
    )

    private fun GameExecution.applied() = assertIs<GameExecution.Applied>(this).result
    private fun GameSnapshot.player(id: String) = players.single { it.id == id }

    private data object FixedRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = from
    }
}
