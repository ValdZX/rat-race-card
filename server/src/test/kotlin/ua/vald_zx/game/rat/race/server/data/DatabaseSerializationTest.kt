package ua.vald_zx.game.rat.race.server.data

import com.mongodb.MongoClientSettings
import kotlinx.datetime.LocalDateTime
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationProgress
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationStage
import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.CURRENT_SCHEMA_VERSION
import ua.vald_zx.game.rat.race.card.shared.standardContentPackVersions
import ua.vald_zx.game.rat.race.server.generation.BoardGenerator
import ua.vald_zx.game.rat.race.server.testBalance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseSerializationTest {

    @Test
    fun readyGeneratedBoardSurvivesMongoCodecRoundTrip() {
        val world = BoardGeneration(
            enabled = true,
            theme = "космічна колонія",
            locality = "Марс",
            epoch = "2140",
            seed = 42,
        )
        val balance = testBalance()
        val generator = BoardGenerator(world, balance)
        val board = Board(
            id = "board",
            name = "Generated board",
            loanLimit = balance.loanLimit,
            businessLimit = balance.businessLimit,
            createDateTime = LocalDateTime(2026, 1, 1, 12, 0),
            cards = BoardCardType.entries.associateWith { (1..12).toList() },
            playerIds = setOf("player"),
            activePlayerId = "player",
            moveCount = 37,
            generation = world,
            generatedCards = generator.generate(BoardCardType.entries.associateWith { 12 }),
            generatedProfessions = generator.generateProfessions(),
            generatedPlaces = generator.generatePlaces(),
            trackDefinitions = generator.generateTracks(),
            generatedBalance = balance,
            generationProgress = BoardGenerationProgress(
                stage = BoardGenerationStage.READY,
                completed = 1,
                total = 1,
                elapsedMillis = 25_000,
                totalTokens = 4_200,
                requestCount = 17,
                quotaType = GenerationQuotaType.REQUESTS_PER_MINUTE,
                quotaLimit = 20,
                quotaUsed = 17,
                quotaResetAtEpochMs = 1_800_000_000_000,
            ),
        )

        val document = encodeBoardDocument(board)
        val decoded = decodeBoardDocument(document)

        assertEquals(
            board.copy(contentPackVersions = standardContentPackVersions()),
            decoded,
        )
        assertEquals(1, document.getInt32("schemaVersion").value)
        assertTrue(document.getString("payload").value.isNotBlank())
        assertTrue(decoded.generationProgress.isReady)
        assertEquals(37, decoded.moveCount)
        assertTrue(decoded.generatedCards.isNotEmpty())
    }

    @Test
    fun boardSavedByThePreviousPojoCodecCanStillBeLoaded() {
        val legacy = Board(
            id = "legacy",
            name = "Legacy board",
            loanLimit = 10_000,
            businessLimit = 10,
            createDateTime = LocalDateTime(2026, 1, 1, 12, 0),
            cards = BoardCardType.entries.associateWith { (1..3).toList() },
            generation = BoardGeneration(
                enabled = true,
                theme = "місто",
                locality = "Київ",
                epoch = "сьогодення",
                seed = 7,
            ),
            generationProgress = BoardGenerationProgress(
                stage = BoardGenerationStage.PREPARING,
                completed = 0,
                total = 1,
            ),
        )
        val registry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(
                PojoCodecProvider.builder().automatic(true).register(Board::class.java).build()
            ),
        )
        val document = BsonDocument()
        registry.get(Board::class.java).encode(
            BsonDocumentWriter(document),
            legacy,
            EncoderContext.builder().isEncodingCollectibleDocument(true).build(),
        )

        assertEquals(
            legacy.copy(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                contentPackVersions = standardContentPackVersions(),
            ),
            decodeBoardDocument(document),
        )
    }

    @Test
    fun playerSavedByThePreviousPojoCodecCanStillBeLoaded() {
        val legacy = Player(
            id = "player",
            boardId = "legacy",
            attrs = PlayerAttributes(color = 42),
            cash = 12_000,
            deposit = 5_000,
        )
        val registry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(
                PojoCodecProvider.builder().automatic(true).register(Player::class.java).build()
            ),
        )
        val document = BsonDocument()
        registry.get(Player::class.java).encode(
            BsonDocumentWriter(document),
            legacy,
            EncoderContext.builder().isEncodingCollectibleDocument(true).build(),
        )

        assertEquals(legacy, decodePlayerDocument(document))
    }
}
