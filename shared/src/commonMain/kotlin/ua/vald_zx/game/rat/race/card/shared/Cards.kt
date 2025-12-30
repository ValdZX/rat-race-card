package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
enum class BoardCardType {
    Chance,
    SmallBusiness,
    MediumBusiness,
    BigBusiness,
    Expenses,
    EventStore,
    Shopping,
    Deputy,
}

@Serializable
data class CardLink(val type: BoardCardType, val id: Int)

@Serializable
sealed class BoardCard(val type: BoardCardType) {
    @Serializable
    data class SmallBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.SmallBusiness)

    @Serializable
    data class MediumBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.MediumBusiness)

    @Serializable
    data class BigBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.BigBusiness)

    @Serializable
    data class Shopping(
        val description: String,
        val price: Long,
        val shopType: ShopType,
        val credit: String,//TODO
    ) : BoardCard(BoardCardType.Shopping)

    @Serializable
    data class EventStore(
        val description: String,
    ) : BoardCard(BoardCardType.EventStore)

    @Serializable
    data class Deputy(
        val description: String,
    ) : BoardCard(BoardCardType.Deputy)

    @Serializable
    sealed class Chance : BoardCard(BoardCardType.Chance) {
        @Serializable
        data class RandomJob(
            val description: String,
            val cost: Int
        ) : Chance()
        @Serializable
        data class Land(
            val description: String,
            val price: Int,
            val area: Int,
            val profit: Int,
            val probability: Int
        ) : Chance()
        @Serializable
        data class Shares(
            val description: String,
            val price: Int,
            val maxShares: Int,
            val sharesType: SharesType
        ) : Chance()
        @Serializable
        data class Estate(
            val description: String,
            val price: Int
        ) : Chance()
    }

    @Serializable
    data class Expenses(
        val description: String,
        val priceTitle: String,
        val price: Long,
        val payer: PayerType,
    ) : BoardCard(BoardCardType.Expenses)
}

enum class ShopType {
    AUTO,
    HOUSE,
    APARTMENT,
    YACHT,
    FLY
}

enum class PayerType {
    ALL,
    FREE_W_OR_MARRIED_M,
    AUTO_OWNER,
    MEN,
    PARENT,
    MARRIED_M,
    APARTMENT_OWNER,
    APARTMENT_OR_HOUSE_OWNER,
    ANIMAL_OWNER,
}