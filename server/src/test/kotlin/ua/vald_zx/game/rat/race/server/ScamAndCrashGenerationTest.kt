package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.ShareSectors
import ua.vald_zx.game.rat.race.server.generation.BoardGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScamAndCrashGenerationTest {
    private val decks = mapOf(
        BoardCardType.Chance to 138,
        BoardCardType.EventStore to 124,
    )

    private fun world() = BoardGeneration(enabled = true, theme = "Марс", locality = "колонія", seed = 42)

    @Test
    fun chanceDeckContainsScamOffers() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        val scams = generated.getValue(BoardCardType.Chance).values.filterIsInstance<BoardCard.Chance.Scam>()

        assertTrue(scams.isNotEmpty(), "колода шансу має містити шахрайські пропозиції")
        scams.forEach { scam ->
            assertTrue(scam.price in testBalance().scamPrices, "внесок ${scam.price} поза балансом")
            assertTrue(
                scam.promisedProfit > scam.price,
                "обіцянка ${scam.promisedProfit} має перевищувати внесок ${scam.price}, інакше пастка не спокуслива",
            )
            assertEquals(testBalance().scamSuccessPercentage, scam.successPercentage)
        }
    }

    @Test
    fun scamsAreALosingBetOnAverage() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        val scams = generated.getValue(BoardCardType.Chance).values.filterIsInstance<BoardCard.Chance.Scam>()

        scams.forEach { scam ->
            val expectedReturn = scam.promisedProfit * scam.successPercentage / 100
            assertTrue(
                expectedReturn < scam.price,
                "очікувана віддача $expectedReturn не має перевищувати внесок ${scam.price}",
            )
        }
    }

    @Test
    fun marketDeckContainsCrashesLimitedToKnownSectors() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        val crashes = generated.getValue(BoardCardType.EventStore).values
            .filterIsInstance<BoardCard.EventStore.MarketCrash>()

        assertTrue(crashes.isNotEmpty(), "колода ринку має містити обвали")
        crashes.forEach { crash ->
            assertTrue(crash.sector in ShareSectors.all, "невідомий сектор ${crash.sector}")
            assertTrue(
                crash.sectorDropPercentage > crash.marketDropPercentage,
                "обвал має бити по своєму сектору сильніше за ринок",
            )
        }
    }

    @Test
    fun crashesOnlyStrikeSectorsThatCompaniesActuallyBelongTo() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        val crashSectors = generated.getValue(BoardCardType.EventStore).values
            .filterIsInstance<BoardCard.EventStore.MarketCrash>()
            .map { it.sector }
            .toSet()
        val companySectors = testBalance().shares.map { it.sector }.toSet()

        assertTrue(
            companySectors.containsAll(crashSectors),
            "обвал ударив по секторах $crashSectors, яких немає серед компаній $companySectors",
        )
    }

    @Test
    fun generationStaysDeterministicForTheSameSeed() {
        val first = BoardGenerator(world(), testBalance()).generate(decks)
        val second = BoardGenerator(world(), testBalance()).generate(decks)

        assertEquals(first, second)
    }
}
