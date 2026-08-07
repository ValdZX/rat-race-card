package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BoardSnapshotMigratorTest {
    @Test
    fun newerSchemaIsRejectedBeforeDeserialization() {
        val error = assertFailsWith<IllegalArgumentException> {
            BoardSnapshotMigrator.decode("""{"schemaVersion":2}""")
        }

        assertTrue(error.message.orEmpty().contains("newer than supported"))
    }

    @Test
    fun incompatibleRulesAreRejectedBeforeBoardOpens() {
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 10_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            contentPackVersions = coreContentPackVersions(),
            rulesVersion = CURRENT_RULES_VERSION + 1,
        )

        val error = assertFailsWith<IllegalArgumentException> { board.requireValidFeatures() }

        assertTrue(error.message.orEmpty().contains("rules"))
    }
}
