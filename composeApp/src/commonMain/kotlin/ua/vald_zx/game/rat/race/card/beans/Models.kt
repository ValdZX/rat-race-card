package ua.vald_zx.game.rat.race.card.beans

import kotlinx.serialization.Serializable

@Serializable
data class ProfessionCard(
    val profession: String = "",
    val salary: Long = 0,
    val rent: Long = 0,
    val food: Long = 0,
    val cloth: Long = 0,
    val transport: Long = 0,
    val phone: Long = 0,
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
    val price: Long,
    val profit: Long,
    val extentions: List<Long> = emptyList()
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
    val count: Long,
    val buyPrice: Long,
) {
    val price: Long
        get() = count * buyPrice
}
