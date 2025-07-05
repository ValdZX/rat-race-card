package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class BoardId(
    val id: String,
    val name: String,
    val createDateTime: LocalDateTime,
)
@Serializable
data class Board(
    val id: String,
    val createDateTime: LocalDateTime,
    val name: String,
    val cards: Map<BoardCardType, List<Int>>,
    val discard: Map<BoardCardType, List<Int>>,
    val players: Set<String>,
    val activePlayer: String,
    val takenCard: CardLink?,
)
