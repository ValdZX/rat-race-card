package ua.vald_zx.game.rat.race.card.beans

import kotlinx.serialization.Serializable

@Serializable
data class ProfessionCard(
    val profession: String = "",
    val salary: Int = 0,
    val rent: Int = 0,
    val food: Int = 0,
    val cloth: Int = 0,
    val transport: Int = 0,
    val phone: Int = 0,
)

enum class BusinessType {
    WORK,
    SMALL,
    MEDIUM,
    LARGE
}

@Serializable
data class Business(
    val type: BusinessType,
    val name: String,
    val price: Int,
    val profit: Int,
    val extentions: List<Int> = emptyList()
)

enum class SharesType {
    GS,
    ЩГП,
    TO,
    SCT
}

@Serializable
data class Shares(
    val type: SharesType,
    val count: Int,
    val price: Int,
)
