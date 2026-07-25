package ua.vald_zx.game.rat.race.card.beans

import kotlinx.serialization.Serializable

enum class BusinessType(val klass: Int) {
    WORK(0),
    SMALL(1),
    MEDIUM(2),
    LARGE(3),
    CORRUPTION(3)
}

@Serializable
data class Business(
    val type: BusinessType,
    val name: String,
    val price: Long,
    val profit: Long,
    val extentions: List<Long> = emptyList(),
    val alarmed: Boolean = false,
)

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

@Serializable
data class Fund(
    val rate: Long,
    val amount: Long,
)


@Serializable
data class Config(
    val depositRate: Long = 2,
    val loadRate: Long = 10,
    val babyCost: Long = 300,
    val carCost: Long = 600,
    val apartmentCost: Long = 200,
    val cottageCost: Long = 1000,
    val yachtCost: Long = 1500,
    val flightCost: Long = 5000,
    val fundBaseRate: Long = 20,
    val fundStartRate: Long = 30,
    val hasFunds: Boolean = true,
    val tts: Boolean = false,
)
