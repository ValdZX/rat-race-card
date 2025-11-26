package ua.vald_zx.game.rat.race.card.screen.board

import ua.vald_zx.game.rat.race.card.shared.BoardCardType

sealed class BoardCard(val type: BoardCardType) {
    data class SmallBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.SmallBusiness)

    data class MediumBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.MediumBusiness)

    data class BigBusiness(
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.BigBusiness)

    data class Shopping(
        val description: String,
        val price: Long,
        val shopType: ShopType,
        val credit: String,//TODO
    ) : BoardCard(BoardCardType.Shopping)

    data class EventStore(
        val description: String,
    ) : BoardCard(BoardCardType.EventStore)

    data class Deputy(
        val description: String,
    ) : BoardCard(BoardCardType.Deputy)

    data class Chance(
        val description: String,
    ) : BoardCard(BoardCardType.Chance)

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