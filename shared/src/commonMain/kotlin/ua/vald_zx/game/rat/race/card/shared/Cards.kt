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
        val name: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.SmallBusiness)

    @Serializable
    data class MediumBusiness(
        val name: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.MediumBusiness)

    @Serializable
    data class BigBusiness(
        val name: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.BigBusiness)

    @Serializable
    data class Shopping(
        val description: String,
        val price: Long,
        val shopType: ShopType,
        val credit: String,
    ) : BoardCard(BoardCardType.Shopping)

    @Serializable
    sealed class EventStore : BoardCard(BoardCardType.EventStore) {
        @Serializable
        data class Shares(
            val sharesType: SharesType,
            val description: String,
            val price: Long
        ) : EventStore()

        @Serializable
        data class Land(
            val description: String,
            val price: Long
        ) : EventStore()

        @Serializable
        data class Estate(
            val description: String,
            val price: Long
        ) : EventStore()

        @Serializable
        data class BusinessExtending(
            val description: String,
            val profit: Long
        ) : EventStore()
    }

    @Serializable
    data class Deputy(
        val description: String,
    ) : BoardCard(BoardCardType.Deputy)

    @Serializable
    sealed class Chance : BoardCard(BoardCardType.Chance) {
        @Serializable
        data class RandomJob(
            val description: String,
            val profit: Long
        ) : Chance()

        @Serializable
        data class Land(
            val name: String,
            val description: String,
            val price: Long,
            val area: Long,
        ) : Chance()

        @Serializable
        data class Shares(
            val description: String,
            val price: Long,
            val maxCount: Long,
            val sharesType: SharesType
        ) : Chance()

        @Serializable
        data class Estate(
            val name: String,
            val description: String,
            val price: Long
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

fun BoardCardType.matches(placeType: PlaceType): Boolean {
    return when (this) {
        BoardCardType.Chance -> placeType == PlaceType.Chance
        BoardCardType.SmallBusiness,
        BoardCardType.MediumBusiness -> placeType == PlaceType.Business

        BoardCardType.BigBusiness -> placeType == PlaceType.BigBusiness
        BoardCardType.Expenses -> placeType == PlaceType.Expenses
        BoardCardType.EventStore -> placeType == PlaceType.Store
        BoardCardType.Shopping -> placeType == PlaceType.Shopping
        BoardCardType.Deputy -> placeType == PlaceType.Deputy
    }
}

fun BoardLayer.hasPlaceFor(cardType: BoardCardType): Boolean {
    return places.any(cardType::matches)
}

fun BoardLayer.nearestPlacePosition(
    currentPosition: Int,
    cardType: BoardCardType,
): Int? {
    return (1..cellCount)
        .map { offset -> moveTo(currentPosition, cellCount, offset) }
        .firstOrNull { position -> cardType.matches(places[position]) }
}
