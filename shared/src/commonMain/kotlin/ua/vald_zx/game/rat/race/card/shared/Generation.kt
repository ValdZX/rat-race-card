package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
data class BoardGeneration(
    val enabled: Boolean = false,
    val theme: String = "",
    val locality: String = "",
    val epoch: String = "",
    val seed: Long = 0,
) {
    val describesWorld: Boolean
        get() = theme.isNotBlank() || locality.isNotBlank() || epoch.isNotBlank()
}

fun BoardGeneration.seedFor(type: BoardCardType, cardId: Int): Long {
    var mixed = seed
    mixed = mixed * 31 + type.ordinal
    mixed = mixed * 31 + cardId
    mixed = mixed * 31 + theme.hashCode()
    mixed = mixed * 31 + locality.hashCode()
    mixed = mixed * 31 + epoch.hashCode()
    return mixed
}
