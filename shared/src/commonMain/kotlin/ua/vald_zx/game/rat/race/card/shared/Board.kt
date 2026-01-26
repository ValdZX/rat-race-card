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
    val canTakeCard: BoardCardType? = null,
    val takenCard: CardLink? = null,
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