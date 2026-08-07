package ua.vald_zx.game.rat.race.card.shared

enum class GameCommandOutcome {
    APPLIED,
    DUPLICATE,
    REJECTED,
    BOARD_NOT_FOUND,
}

data class GameCommandLogEntry(
    val boardId: String,
    val commandId: String,
    val playerId: String,
    val command: String,
    val outcome: GameCommandOutcome,
    val rejection: GameCommandRejection? = null,
    val revisionBefore: Long = -1,
    val revisionAfter: Long = -1,
    val schemaVersion: Int = -1,
    val rulesVersion: Int = -1,
    val contentPackVersions: Map<FeatureId, Int> = emptyMap(),
    val domainEvents: List<String> = emptyList(),
    val loadMillis: Long = 0,
    val engineMillis: Long = 0,
    val commitMillis: Long = 0,
) {
    fun format(): String = buildString {
        append("game.command")
        appendField("boardId", boardId)
        appendField("commandId", commandId)
        appendField("playerId", playerId)
        appendField("command", command)
        appendField("outcome", outcome.name)
        rejection?.let { appendField("rejection", it.name) }
        appendField("revisionBefore", revisionBefore)
        appendField("revisionAfter", revisionAfter)
        appendField("schemaVersion", schemaVersion)
        appendField("rulesVersion", rulesVersion)
        appendField("contentPacks", contentPackVersions.entries.joinToString(",") { "${it.key.value}=${it.value}" })
        appendField("events", domainEvents.joinToString(","))
        appendField("loadMs", loadMillis)
        appendField("engineMs", engineMillis)
        appendField("commitMs", commitMillis)
    }

    private fun StringBuilder.appendField(name: String, value: Any?) {
        append(' ').append(name).append('=').append(value)
    }
}

fun interface GameCommandLog {
    fun record(entry: GameCommandLogEntry)

    companion object {
        val None = GameCommandLog { }
    }
}
