package ua.vald_zx.game.rat.race.card.beans

import kotlinx.serialization.Serializable

typealias BusinessType = ua.vald_zx.game.rat.race.card.shared.BusinessType
typealias Business = ua.vald_zx.game.rat.race.card.shared.Business

@Serializable
data class Land(
    val name: String,
    val area: Long,
    val priceOfUnit: Long,
) {
    val price: Long = priceOfUnit * area
}

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

typealias Fund = ua.vald_zx.game.rat.race.card.shared.Fund
typealias Config = ua.vald_zx.game.rat.race.card.shared.Config
