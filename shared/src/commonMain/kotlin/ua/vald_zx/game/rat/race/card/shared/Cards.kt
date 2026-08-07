package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.SerialName
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
sealed class BoardCard(@SerialName("cardType") val type: BoardCardType) {
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
        val name: String = "",
    ) : BoardCard(BoardCardType.Shopping)

    @Serializable
    sealed class EventStore : BoardCard(BoardCardType.EventStore) {
        @Serializable
        data class Shares(
            val sharesType: String,
            val description: String,
            val price: Long,
            val forcedSale: Boolean = false,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class Land(
            val description: String,
            val price: Long,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class Estate(
            val description: String,
            val price: Long,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class BusinessExtending(
            val description: String,
            val profit: Long,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class MarketCrash(
            val description: String,
            val sector: String,
            val sectorDropPercentage: Long,
            val marketDropPercentage: Long,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class Reelection(
            val description: String,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class Announcement(
            val description: String,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class CorruptBusiness(
            val description: String,
            val salePercentage: Long,
            val name: String = "",
        ) : EventStore()

        @Serializable
        data class CorruptLand(
            val description: String,
            val price: Long,
            val name: String = "",
        ) : EventStore()
    }

    @Serializable
    data class Deputy(
        val description: String,
        val corrupt: Boolean,
        val name: String = "",
    ) : BoardCard(BoardCardType.Deputy)

    @Serializable
    sealed class Chance : BoardCard(BoardCardType.Chance) {
        @Serializable
        data class RandomJob(
            val description: String,
            val profit: Long,
            val name: String = "",
        ) : Chance()

        @Serializable
        data class Scam(
            val description: String,
            val price: Long,
            val promisedProfit: Long,
            val successPercentage: Int,
            val name: String = "",
        ) : Chance() {
            val promisedReturnPercentage: Long
                get() = if (price <= 0) 0 else promisedProfit * 100 / price
        }

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
            val sharesType: String,
            val name: String = "",
        ) : Chance()

        @Serializable
        data class Estate(
            val name: String,
            val description: String,
            val price: Long
        ) : Chance()

        @Serializable
        data class CorruptBusiness(
            val description: String,
            val price: Long,
            val profit: Long,
            val oneTimeProfit: Long,
            val deputies: Int,
            val name: String = "",
        ) : Chance()

        @Serializable
        data class CorruptLand(
            val description: String,
            val price: Long,
            val area: Long,
            val deputies: Int,
            val name: String = "",
        ) : Chance()
    }

    @Serializable
    data class Expenses(
        val description: String,
        val priceTitle: String,
        val price: Long,
        val payer: PayerType,
        val grantsAnimal: Boolean = false,
        val name: String = "",
    ) : BoardCard(BoardCardType.Expenses)
}

enum class ShopType {
    AUTO,
    HOUSE,
    APARTMENT,
    YACHT,
    FLY,
    ANIMAL,
}

val shopTiers = listOf(
    ShopType.ANIMAL,
    ShopType.AUTO,
    ShopType.APARTMENT,
    ShopType.HOUSE,
    ShopType.YACHT,
    ShopType.FLY,
)

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

fun BoardCard.withText(text: CardText): BoardCard {
    val description = text.description.ifBlank { null }
    val name = text.name.ifBlank { null }
    return when (this) {
        is BoardCard.SmallBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.MediumBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.BigBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Shopping -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Deputy -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Expenses -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.Shares -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.Land -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.Estate -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.BusinessExtending -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.MarketCrash -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.Reelection -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.Announcement -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.CorruptBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.EventStore.CorruptLand -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.RandomJob -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Scam -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Land -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Estate -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Shares -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.CorruptBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.CorruptLand -> copy(name = name ?: this.name, description = description ?: this.description)
    }
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

fun List<PlaceType>.hasPlaceFor(cardType: BoardCardType): Boolean = any(cardType::matches)

fun List<PlaceType>.nearestPlacePosition(
    currentPosition: Int,
    cardType: BoardCardType,
): Int? {
    return (1..size)
        .map { offset -> moveTo(currentPosition, size, offset) }
        .firstOrNull { position -> cardType.matches(this[position]) }
}

fun BoardLayer.hasPlaceFor(cardType: BoardCardType): Boolean = places.hasPlaceFor(cardType)

fun BoardLayer.nearestPlacePosition(
    currentPosition: Int,
    cardType: BoardCardType,
): Int? = places.nearestPlacePosition(currentPosition, cardType)
