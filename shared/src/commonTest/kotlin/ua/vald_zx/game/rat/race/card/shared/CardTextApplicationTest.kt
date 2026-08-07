package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class CardTextApplicationTest {
    @Test
    fun expenseKeepsTheGeneratedPurposeName() {
        val expense = BoardCard.Expenses(
            description = "",
            priceTitle = "4 000",
            price = 4_000,
            payer = PayerType.ALL,
        )

        val localized = expense.withText(
            CardText(
                name = "Ремонт купола",
                description = "Метеорит пошкодив захисний купол колонії.",
            )
        ) as BoardCard.Expenses

        assertEquals("Ремонт купола", localized.name)
        assertEquals("Метеорит пошкодив захисний купол колонії.", localized.description)
    }

    @Test
    fun everyGeneratedCardTitleIsKept() {
        val cards = listOf<BoardCard>(
            BoardCard.Shopping("", 100, ShopType.AUTO, ""),
            BoardCard.EventStore.Shares("shares", "", 10),
            BoardCard.EventStore.Land("", 100),
            BoardCard.EventStore.Estate("", 100),
            BoardCard.EventStore.BusinessExtending("", 100),
            BoardCard.EventStore.Reelection(""),
            BoardCard.EventStore.Announcement(""),
            BoardCard.EventStore.CorruptBusiness("", 300),
            BoardCard.EventStore.CorruptLand("", 10_000),
            BoardCard.Deputy("", false),
            BoardCard.Chance.RandomJob("", 100),
            BoardCard.Chance.Shares("", 10, 100, "shares"),
            BoardCard.Chance.CorruptBusiness("", 100, 10, 20, 1),
            BoardCard.Chance.CorruptLand("", 100, 10, 1),
            BoardCard.Expenses("", "", 100, PayerType.ALL),
        )

        cards.forEach { card ->
            val localized = card.withText(CardText("Конкретна назва", "Конкретна ситуація"))

            assertEquals("Конкретна назва", localized.generatedName())
            assertEquals("Конкретна ситуація", localized.generatedDescription())
        }
    }

    private fun BoardCard.generatedName(): String = when (this) {
        is BoardCard.BigBusiness -> name
        is BoardCard.Chance.CorruptBusiness -> name
        is BoardCard.Chance.CorruptLand -> name
        is BoardCard.Chance.Estate -> name
        is BoardCard.Chance.Land -> name
        is BoardCard.Chance.RandomJob -> name
        is BoardCard.Chance.Scam -> name
        is BoardCard.Chance.Shares -> name
        is BoardCard.Deputy -> name
        is BoardCard.EventStore.Announcement -> name
        is BoardCard.EventStore.BusinessExtending -> name
        is BoardCard.EventStore.CorruptBusiness -> name
        is BoardCard.EventStore.CorruptLand -> name
        is BoardCard.EventStore.Estate -> name
        is BoardCard.EventStore.Land -> name
        is BoardCard.EventStore.MarketCrash -> name
        is BoardCard.EventStore.Reelection -> name
        is BoardCard.EventStore.Shares -> name
        is BoardCard.Expenses -> name
        is BoardCard.MediumBusiness -> name
        is BoardCard.Shopping -> name
        is BoardCard.SmallBusiness -> name
    }

    private fun BoardCard.generatedDescription(): String = when (this) {
        is BoardCard.BigBusiness -> description
        is BoardCard.Chance.CorruptBusiness -> description
        is BoardCard.Chance.CorruptLand -> description
        is BoardCard.Chance.Estate -> description
        is BoardCard.Chance.Land -> description
        is BoardCard.Chance.RandomJob -> description
        is BoardCard.Chance.Scam -> description
        is BoardCard.Chance.Shares -> description
        is BoardCard.Deputy -> description
        is BoardCard.EventStore.Announcement -> description
        is BoardCard.EventStore.BusinessExtending -> description
        is BoardCard.EventStore.CorruptBusiness -> description
        is BoardCard.EventStore.CorruptLand -> description
        is BoardCard.EventStore.Estate -> description
        is BoardCard.EventStore.Land -> description
        is BoardCard.EventStore.MarketCrash -> description
        is BoardCard.EventStore.Reelection -> description
        is BoardCard.EventStore.Shares -> description
        is BoardCard.Expenses -> description
        is BoardCard.MediumBusiness -> description
        is BoardCard.Shopping -> description
        is BoardCard.SmallBusiness -> description
    }
}
