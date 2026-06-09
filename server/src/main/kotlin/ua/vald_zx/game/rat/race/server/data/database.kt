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
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

fun connectToDatabase(): MongoDatabase {
    val connectionString = Env.require("MONGODB_URI")
    val databaseName = Env["MONGODB_DATABASE"] ?: "board"

    val serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()
    val pojoCodecProvider = PojoCodecProvider.builder()
        .automatic(true)
        .register(Board::class.java)
        .register(Player::class.java)
        .build()
    val codecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(pojoCodecProvider)
    )
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .serverApi(serverApi)
        .codecRegistry(codecRegistry)
        .build()
    return MongoClient.create(mongoClientSettings).getDatabase(databaseName)
}

fun MongoDatabase.boardCollection() = getCollection<Board>("board")
fun MongoDatabase.playerCollection() = getCollection<Player>("player")

suspend fun MongoDatabase.boards(): List<Board> {
    return boardCollection().find().toList()
}

suspend fun MongoDatabase.removeBoard(id: String) {
    boardCollection().deleteOne(Filters.eq("_id", id))
}

suspend fun MongoDatabase.removePlayer(id: String) {
    playerCollection().deleteOne(Filters.eq("_id", id))
}

suspend fun MongoDatabase.newBoard(board: Board) {
    boardCollection().insertOne(board)
}

suspend fun MongoDatabase.updateBoard(board: Board) {
    boardCollection().replaceOne(
        filter = Filters.eq("_id", board.id),
        replacement = board,
        options = ReplaceOptions().upsert(true)
    )
}

suspend fun MongoDatabase.newPlayer(player: Player) {
    playerCollection().replaceOne(
        filter = Filters.eq("_id", player.id),
        replacement = player,
        options = ReplaceOptions().upsert(true)
    )
}

suspend fun MongoDatabase.updatePlayer(player: Player) {
    playerCollection().replaceOne(
        filter = Filters.eq("_id", player.id),
        replacement = player,
        options = ReplaceOptions().upsert(true)
    )
}

suspend fun MongoDatabase.observePlayers(boardId: String): Flow<Player> =
    playerCollection().watch(
        listOf(
            Aggregates.match(
                Filters.eq("fullDocument.boardId", boardId)
            )
        )
    ).mapNotNull { it.fullDocument }

suspend fun MongoDatabase.getBoard(id: String): Board? {
    return boardCollection()
        .find(Filters.eq("_id", id))
        .firstOrNull()
}

suspend fun MongoDatabase.getPlayer(id: String): Player? {
    return playerCollection()
        .find(Filters.eq("_id", id))
        .firstOrNull()
}
