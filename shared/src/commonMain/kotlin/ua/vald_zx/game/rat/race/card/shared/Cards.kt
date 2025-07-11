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