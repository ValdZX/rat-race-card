package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

const val DEFAULT_LOCALE = "uk"

val generatedLocales = listOf("uk", "en")

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

@Serializable
data class CardText(
    val name: String = "",
    val description: String = "",
)

@Serializable
data class GeneratedText(
    val cards: Map<BoardCardType, Map<Int, CardText>> = emptyMap(),
    val professions: Map<Int, String> = emptyMap(),
)

fun BoardGeneration.seedFor(salt: String, index: Int): Long {
    var mixed = seed
    mixed = mixed * 31 + salt.hashCode()
    mixed = mixed * 31 + index
    mixed = mixed * 31 + theme.hashCode()
    mixed = mixed * 31 + locality.hashCode()
    mixed = mixed * 31 + epoch.hashCode()
    return mixed
}

fun BoardGeneration.seedFor(type: BoardCardType, cardId: Int): Long = seedFor(type.name, cardId)
