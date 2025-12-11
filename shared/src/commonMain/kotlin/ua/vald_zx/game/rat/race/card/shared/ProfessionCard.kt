package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
data class ProfessionCard(
    val id: Int,
    val name: String,
    val salary: Long,
    val rent: Long,
    val food: Long,
    val cloth: Long,
    val transport: Long,
    val phone: Long,
    val gender: Gender,
)

