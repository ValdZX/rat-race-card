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

        @Serializable
        data class Reelection(
            val description: String,
        ) : EventStore()

        @Serializable
        data class Announcement(
            val description: String,
        ) : EventStore()
    }

    @Serializable
    data class Deputy(
        val description: String,
        val corrupt: Boolean,
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

        @Serializable
        data class CorruptBusiness(
            val description: String,
            val price: Long,
            val profit: Long,
            val oneTimeProfit: Long,
            val deputies: Int,
        ) : Chance()

        @Serializable
        data class CorruptLand(
            val description: String,
            val price: Long,
            val area: Long,
            val deputies: Int,
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

fun BoardCard.withText(text: CardText): BoardCard {
    val description = text.description.ifBlank { null }
    val name = text.name.ifBlank { null }
    return when (this) {
        is BoardCard.SmallBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.MediumBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.BigBusiness -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Shopping -> copy(description = description ?: this.description)
        is BoardCard.Deputy -> copy(description = description ?: this.description)
        is BoardCard.Expenses -> copy(description = description ?: this.description)
        is BoardCard.EventStore.Shares -> copy(description = description ?: this.description)
        is BoardCard.EventStore.Land -> copy(description = description ?: this.description)
        is BoardCard.EventStore.Estate -> copy(description = description ?: this.description)
        is BoardCard.EventStore.BusinessExtending -> copy(description = description ?: this.description)
        is BoardCard.EventStore.Reelection -> copy(description = description ?: this.description)
        is BoardCard.EventStore.Announcement -> copy(description = description ?: this.description)
        is BoardCard.Chance.RandomJob -> copy(description = description ?: this.description)
        is BoardCard.Chance.Land -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Estate -> copy(name = name ?: this.name, description = description ?: this.description)
        is BoardCard.Chance.Shares -> copy(description = description ?: this.description)
        is BoardCard.Chance.CorruptBusiness -> copy(description = description ?: this.description)
        is BoardCard.Chance.CorruptLand -> copy(description = description ?: this.description)
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
