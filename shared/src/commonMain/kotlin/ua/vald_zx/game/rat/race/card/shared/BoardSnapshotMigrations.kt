package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

const val CURRENT_SCHEMA_VERSION = 1

data class BoardSnapshotMigrationStep(
    val fromVersion: Int,
    val toVersion: Int,
    val transform: (JsonObject) -> JsonObject,
)

data class BoardSnapshotMigrationResult(
    val board: Board,
    val sourceSchemaVersion: Int,
    val payload: JsonObject,
) {
    val migrated: Boolean
        get() = sourceSchemaVersion != board.schemaVersion
}

object BoardSnapshotMigrator {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val migrations = listOf(
        BoardSnapshotMigrationStep(0, 1, ::migrateV0ToV1),
    )

    fun encode(board: Board): String {
        val versioned = board.copy(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            contentPackVersions = board.resolvedContentPackVersions(),
        )
        return json.encodeToString(Board.serializer(), versioned)
    }

    fun decode(payload: String): BoardSnapshotMigrationResult {
        val source = json.parseToJsonElement(payload) as? JsonObject
            ?: error("Board snapshot must be a JSON object")
        val sourceVersion = source["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 0
        require(sourceVersion <= CURRENT_SCHEMA_VERSION) {
            "Board schema $sourceVersion is newer than supported $CURRENT_SCHEMA_VERSION"
        }
        var version = sourceVersion
        var migrated = source
        while (version < CURRENT_SCHEMA_VERSION) {
            val migration = migrations.singleOrNull { it.fromVersion == version && it.toVersion == version + 1 }
                ?: error("Missing board schema migration: $version -> ${version + 1}")
            migrated = migration.transform(migrated)
            version++
        }
        val board = json.decodeFromJsonElement(Board.serializer(), migrated)
        require(board.schemaVersion == CURRENT_SCHEMA_VERSION)
        return BoardSnapshotMigrationResult(board, sourceVersion, migrated)
    }

    private fun migrateV0ToV1(source: JsonObject): JsonObject = buildJsonObject {
        source.forEach { (key, value) -> put(key, value) }
        put("schemaVersion", JsonPrimitive(1))
        if ("rulesVersion" !in source) put("rulesVersion", JsonPrimitive(CURRENT_RULES_VERSION))
        if ("contentPackVersions" !in source) {
            put(
                "contentPackVersions",
                buildJsonObject {
                    standardContentPackVersions().forEach { (featureId, version) ->
                        put(featureId.value, JsonPrimitive(version))
                    }
                },
            )
        }
        if ("revision" !in source) put("revision", JsonPrimitive(0))
    }
}
