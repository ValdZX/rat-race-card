package ua.vald_zx.game.rat.race.server.data

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonString
import org.bson.BsonDateTime
import org.bson.types.ObjectId
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.LedgerEntry
import ua.vald_zx.game.rat.race.card.shared.BoardSnapshotMigrator
import ua.vald_zx.game.rat.race.card.shared.CURRENT_SCHEMA_VERSION
import ua.vald_zx.game.rat.race.card.shared.resolvedContentPackVersions
import ua.vald_zx.game.rat.race.card.shared.Player

fun connectToDatabase(): MongoDatabase {
    val connectionString = Env.require("MONGODB_URI")
    val databaseName = Env["MONGODB_DATABASE"] ?: "board"

    val serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .serverApi(serverApi)
        .codecRegistry(MongoClientSettings.getDefaultCodecRegistry())
        .build()
    return MongoClient.create(mongoClientSettings).getDatabase(databaseName)
}

private val legacyCodecRegistry = CodecRegistries.fromRegistries(
    MongoClientSettings.getDefaultCodecRegistry(),
    CodecRegistries.fromProviders(
        PojoCodecProvider.builder()
            .automatic(true)
            .register(Board::class.java)
            .register(Player::class.java)
            .build()
    ),
)

private val persistenceJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

fun MongoDatabase.boardCollection() = getCollection<BsonDocument>("board")
fun MongoDatabase.boardSnapshotBackupCollection() = getCollection<BsonDocument>("board_snapshot_backup")
fun MongoDatabase.playerCollection() = getCollection<BsonDocument>("player")
fun MongoDatabase.ledgerCollection() = getCollection<BsonDocument>("board_ledger")

suspend fun MongoDatabase.boards(): List<Board> =
    boardCollection().find().toList().map(::decodeBoardDocument)

suspend fun MongoDatabase.removeBoard(id: String) {
    boardCollection().deleteOne(Filters.eq("_id", id))
}

suspend fun MongoDatabase.removePlayer(id: String) {
    playerCollection().deleteOne(Filters.eq("_id", id))
}

suspend fun MongoDatabase.newBoard(board: Board) {
    boardCollection().insertOne(encodeBoardDocument(board))
}

suspend fun MongoDatabase.updateBoard(board: Board) {
    boardCollection().replaceOne(
        filter = Filters.eq("_id", board.id),
        replacement = encodeBoardDocument(board),
        options = ReplaceOptions().upsert(true),
    )
}

suspend fun MongoDatabase.newPlayer(player: Player) {
    playerCollection().replaceOne(
        filter = Filters.eq("_id", player.id),
        replacement = encodePlayerDocument(player),
        options = ReplaceOptions().upsert(true),
    )
}

suspend fun MongoDatabase.updatePlayer(player: Player) {
    playerCollection().replaceOne(
        filter = Filters.eq("_id", player.id),
        replacement = encodePlayerDocument(player),
        options = ReplaceOptions().upsert(true),
    )
}

suspend fun MongoDatabase.appendLedger(boardId: String, entries: List<LedgerEntry>) {
    if (entries.isEmpty()) return
    ledgerCollection().insertMany(entries.map { encodeLedgerDocument(boardId, it) })
}

suspend fun MongoDatabase.ledger(boardId: String): List<LedgerEntry> =
    ledgerCollection()
        .find(Filters.eq("boardId", boardId))
        .toList()
        .mapNotNull { decodeLedgerDocumentOrNull(it) }
        .sortedBy { it.sequence }

suspend fun MongoDatabase.removeLedger(boardId: String) {
    ledgerCollection().deleteMany(Filters.eq("boardId", boardId))
}

suspend fun MongoDatabase.observePlayers(boardId: String): Flow<Player> =
    playerCollection().watch(
        listOf(
            Aggregates.match(
                Filters.eq("fullDocument.boardId", boardId)
            )
        )
    ).mapNotNull { change -> change.fullDocument?.let(::decodePlayerDocument) }

suspend fun MongoDatabase.getBoard(id: String): Board? =
    boardCollection()
        .find(Filters.eq("_id", id))
        .firstOrNull()
        ?.let(::decodeBoardDocument)

suspend fun MongoDatabase.getPlayer(id: String): Player? =
    playerCollection()
        .find(Filters.eq("_id", id))
        .firstOrNull()
        ?.let(::decodePlayerDocument)

internal fun encodeBoardDocument(board: Board): BsonDocument = persistenceDocument(
    id = board.id,
    payload = BoardSnapshotMigrator.encode(board),
)

internal fun decodeBoardDocument(document: BsonDocument): Board = document.payloadOrNull()
    ?.let { BoardSnapshotMigrator.decode(it).board }
    ?: decodeLegacyDocument(document, Board::class.java).let { board ->
        board.copy(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            contentPackVersions = board.resolvedContentPackVersions(),
        )
    }

suspend fun MongoDatabase.migrateBoardSnapshot(boardId: String, confirmed: Boolean): Board {
    require(confirmed) { "Snapshot migration requires explicit confirmation" }
    val original = boardCollection().find(Filters.eq("_id", boardId)).firstOrNull()
        ?: error("Board not found: $boardId")
    val sourceVersion = original.snapshotSchemaVersion()
    if (sourceVersion == CURRENT_SCHEMA_VERSION) return decodeBoardDocument(original)
    val migrated = decodeBoardDocument(original)
    boardSnapshotBackupCollection().insertOne(snapshotBackupDocument(original, sourceVersion))
    boardCollection().replaceOne(
        filter = Filters.eq("_id", boardId),
        replacement = encodeBoardDocument(migrated),
    )
    return migrated
}

internal fun encodePlayerDocument(player: Player): BsonDocument = persistenceDocument(
    id = player.id,
    payload = persistenceJson.encodeToString(Player.serializer(), player),
    boardId = player.boardId,
)

internal fun decodePlayerDocument(document: BsonDocument): Player = document.payloadOrNull()
    ?.let { persistenceJson.decodeFromString(Player.serializer(), it) }
    ?: decodeLegacyDocument(document, Player::class.java)

internal fun encodeLedgerDocument(boardId: String, entry: LedgerEntry): BsonDocument = BsonDocument().apply {
    put("_id", org.bson.BsonObjectId(ObjectId()))
    put("boardId", BsonString(boardId))
    put("playerId", BsonString(entry.playerId))
    put("sequence", BsonInt64(entry.sequence))
    put("schemaVersion", BsonInt32(PERSISTENCE_SCHEMA_VERSION))
    put("payload", BsonString(persistenceJson.encodeToString(LedgerEntry.serializer(), entry)))
}

internal fun decodeLedgerDocumentOrNull(document: BsonDocument): LedgerEntry? = document.payloadOrNull()
    ?.let { runCatching { persistenceJson.decodeFromString(LedgerEntry.serializer(), it) }.getOrNull() }

private fun persistenceDocument(id: String, payload: String, boardId: String? = null) = BsonDocument().apply {
    put("_id", BsonString(id))
    put("schemaVersion", BsonInt32(PERSISTENCE_SCHEMA_VERSION))
    boardId?.let { put("boardId", BsonString(it)) }
    put("payload", BsonString(payload))
}

private fun BsonDocument.payloadOrNull(): String? = getString("payload", null)?.value

internal fun BsonDocument.snapshotSchemaVersion(): Int {
    val payload = payloadOrNull() ?: return 0
    return runCatching { BoardSnapshotMigrator.decode(payload).sourceSchemaVersion }.getOrDefault(0)
}

internal fun snapshotBackupDocument(source: BsonDocument, sourceSchemaVersion: Int): BsonDocument =
    BsonDocument().apply {
        put("_id", org.bson.BsonObjectId(ObjectId()))
        put("boardId", BsonString(source.getString("_id").value))
        put("sourceSchemaVersion", BsonInt32(sourceSchemaVersion))
        put("createdAt", BsonDateTime(System.currentTimeMillis()))
        put("snapshot", source.clone())
    }

private fun <T> decodeLegacyDocument(document: BsonDocument, type: Class<T>): T =
    legacyCodecRegistry.get(type).decode(
        BsonDocumentReader(document),
        DecoderContext.builder().build(),
    )

private const val PERSISTENCE_SCHEMA_VERSION = 1
