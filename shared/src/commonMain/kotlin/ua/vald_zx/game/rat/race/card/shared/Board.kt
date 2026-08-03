package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardId(
    val id: String,
    val name: String,
    val createDateTime: LocalDateTime,
)

@Serializable
data class Board(
    @SerialName("_id")
    val id: String,
    val name: String,
    val loanLimit: Long,
    val businessLimit: Long,
    val createDateTime: LocalDateTime,
    val cards: Map<BoardCardType, List<Int>>,
    val canTakeCard: List<BoardCardType> = emptyList(),
    val takenCard: CardLink? = null,
    val sharesCount: Long? = null,
    val discard: Map<BoardCardType, List<Int>> = emptyMap(),
    val playerIds: Set<String> = emptySet(),
    val activePlayerId: String = "",
    val moveCount: Int = 0,
    val canRoll: Boolean = true,
    val dice: Int = 6,
    val diceRolling: Boolean = false,
    val processedPlayerIds: Set<String> = emptySet(),
    val auction: Auction? = null,
    val bidList: List<Bid> = emptyList(),
    val allInactiveSinceEpochMs: Long? = null,
    val outerCircleConditions: OuterCircleConditions = OuterCircleConditions(),
    val victoryConditions: VictoryConditions = VictoryConditions(),
    val transportMovementBonusEnabled: Boolean = true,
    val winnerId: String? = null,
    val dreams: List<Dream> = ratRaceDreams,
    val purchasedDreamIds: Set<String> = emptySet(),
    val generation: BoardGeneration = BoardGeneration(),
    val generatedCards: Map<BoardCardType, Map<Int, BoardCard>> = emptyMap(),
)

fun Board.cardOrNull(link: CardLink): BoardCard? = generatedCards[link.type]?.get(link.id)

@Serializable
data class OuterCircleConditions(
    val minimumCashFlow: Long = 50_000,
    val apartmentRequired: Boolean = true,
    val carRequired: Boolean = true,
    val minimumAccountBalance: Long = 200_000,
)

@Serializable
data class VictoryConditions(
    val dreamRequired: Boolean = true,
    val planeRequired: Boolean = true,
    val estateRequired: Boolean = true,
    val minimumAccountBalance: Long = 10_000_000,
)

fun Board.toBoardId(): BoardId {
    return BoardId(
        id = id,
        createDateTime = createDateTime,
        name = name,
    )
}

fun moveTo(position: Int, cellCount: Int, toMove: Int): Int {
    val nextPosition = position + toMove
    return if (nextPosition < 0) {
        cellCount + nextPosition
    } else if (cellCount <= nextPosition) {
        nextPosition - cellCount
    } else {
        nextPosition
    }
}
