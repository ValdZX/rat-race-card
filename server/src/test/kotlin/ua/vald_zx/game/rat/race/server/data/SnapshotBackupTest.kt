package ua.vald_zx.game.rat.race.server.data

import org.bson.BsonDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SnapshotBackupTest {
    @Test
    fun backupPreservesTheOriginalDocumentBeforeMigration() {
        val original = BsonDocument.parse(
            """{"_id":"board","payload":"{\"schemaVersion\":0}"}""",
        )

        val backup = snapshotBackupDocument(original, sourceSchemaVersion = 0)

        assertNotEquals(original.get("_id"), backup.get("_id"))
        assertEquals("board", backup.getString("boardId").value)
        assertEquals(0, backup.getInt32("sourceSchemaVersion").value)
        assertEquals(original, backup.getDocument("snapshot"))
    }
}
