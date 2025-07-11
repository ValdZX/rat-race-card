package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable
@Serializable
enum class Gender { MALE, FEMALE }

@Serializable
data class Player(
    val id: String,
    val attrs: PlayerAttributes,
    val playerCard: PlayerCard = PlayerCard(),
    val state: PlayerState = PlayerState(),
    val isInactive: Boolean = false,
)

@Serializable
data class PlayerAttributes(
    val color: Long,
    val avatar: Int = 0,
)

@Serializable
data class PlayerCard(
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val profession: String = "",
    val salary: Long = 0,
    val rent: Long = 0,
    val food: Long = 0,
    val cloth: Long = 0,
    val transport: Long = 0,
    val phone: Long = 0,
)

@Serializable
data class PlayerState(
    val position: Int = 1,
    val level: Int = 0,
    val totalExpenses: Long = 0,
    val cashFlow: Long = 0,
)