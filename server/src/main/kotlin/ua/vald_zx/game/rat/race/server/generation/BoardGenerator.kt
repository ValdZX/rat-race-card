package ua.vald_zx.game.rat.race.server.generation

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.code
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds
import ua.vald_zx.game.rat.race.card.shared.seedFor
import ua.vald_zx.game.rat.race.card.shared.shopTiers
import kotlin.random.Random

internal class BoardGenerator(
    private val world: BoardGeneration,
    private val balance: GeneratedBalance,
) {

    companion object {
        val professionCount: Int = Gender.entries.size * PROFESSIONS_PER_GENDER
    }

    fun generate(decks: Map<BoardCardType, Int>): Map<BoardCardType, Map<Int, BoardCard>> {
        if (!world.enabled) return emptyMap()
        return decks.mapValues { (type, size) ->
            (1..size).associateWith { id -> card(type, id) }
        }
    }

    fun generateProfessions(): List<ProfessionCard> {
        if (!world.enabled) return emptyList()
        return Gender.entries.flatMapIndexed { genderIndex, gender ->
            (1..PROFESSIONS_PER_GENDER).map { index ->
                profession(gender, genderIndex * PROFESSIONS_PER_GENDER + index)
            }
        }
    }

    fun generatePlaces(): Map<BoardLayer, List<String>> {
        if (!world.enabled) return emptyMap()
        return BoardLayer.entries.associateWith { layer -> shuffledPlaces(layer).map { it.code() } }
    }

    fun generateDreams(): List<Dream> {
        if (!world.enabled) return emptyList()
        val ids = dreamSlotIds
        val step = (balance.dreamMaxPrice - balance.dreamMinPrice) / (ids.size - 1)
        return ids.mapIndexed { index, id ->
            Dream(
                id = id,
                name = "",
                description = "",
                price = balance.dreamMinPrice + step * index,
            )
        }
    }

    private fun profession(gender: Gender, id: Int): ProfessionCard {
        val random = Random(world.seedFor("profession", id))
        val salary = balance.salaries.pick(random)
        val rent = salary.share(balance.rentPercentages.pick(random))
        val food = salary.share(balance.foodPercentages.pick(random))
        val cloth = salary.share(balance.clothPercentages.pick(random))
        val transport = salary.share(balance.transportPercentages.pick(random))
        val phone = salary.share(balance.phonePercentages.pick(random))
        return ProfessionCard(
            id = id,
            name = "",
            salary = salary,
            rent = rent,
            food = food,
            cloth = cloth,
            transport = transport,
            phone = phone,
            gender = gender,
            description = "",
        )
    }

    private fun shuffledPlaces(layer: BoardLayer): List<PlaceType> {
        val random = Random(world.seedFor("layout", layer.level))
        val places = layer.places
        val shuffled = places.toMutableList()

        val movableIndices = places.indices.filter { places[it].isMovable() }
        val movable = movableIndices.map { places[it] }.shuffled(random)
        movableIndices.forEachIndexed { slot, index -> shuffled[index] = movable[slot] }

        val desireIndices = places.indices.filter { places[it] is PlaceType.Desire }
        val desires = desireIndices.map { places[it] }.shuffled(random)
        desireIndices.forEachIndexed { slot, index -> shuffled[index] = desires[slot] }

        return shuffled
    }

    private fun PlaceType.isMovable(): Boolean =
        !isBig && this != PlaceType.Start && this !is PlaceType.Desire

    private fun Long.share(percent: Long): Long = expenseShare(percent)

    private fun card(type: BoardCardType, id: Int): BoardCard {
        val random = Random(world.seedFor(type, id))
        return when (type) {
            BoardCardType.SmallBusiness -> business(
                type,
                id,
                random,
                balance.smallBusinessPrices,
                balance.smallBusinessReturnPercentages,
            )
            BoardCardType.MediumBusiness -> business(
                type,
                id,
                random,
                balance.mediumBusinessPrices,
                balance.mediumBusinessReturnPercentages,
            )
            BoardCardType.BigBusiness -> business(
                type,
                id,
                random,
                balance.bigBusinessPrices,
                balance.bigBusinessReturnPercentages,
            )
            BoardCardType.Shopping -> shopping(type, id, random)
            BoardCardType.Expenses -> expenses(type, id, random)
            BoardCardType.Chance -> chance(type, id, random)
            BoardCardType.EventStore -> eventStore(type, id, random)
            BoardCardType.Deputy -> deputy(type, id, random)
        }
    }

    private fun business(
        type: BoardCardType,
        id: Int,
        random: Random,
        prices: List<Long>,
        rates: List<Long>,
    ): BoardCard {
        val price = prices.pick(random)
        val rate = rates.pick(random)
        val profit = ((price * rate + 99) / 100).coerceAtLeast(1)
        return when (type) {
            BoardCardType.SmallBusiness -> BoardCard.SmallBusiness("", "", price, profit)
            BoardCardType.MediumBusiness -> BoardCard.MediumBusiness("", "", price, profit)
            else -> BoardCard.BigBusiness("", "", price, profit)
        }
    }

    private fun shopping(type: BoardCardType, id: Int, random: Random): BoardCard {
        val shopType = weighted(random, shopTiers.associateWith { balance.shopWeights.getValue(it) })
        return BoardCard.Shopping(
            description = "",
            price = balance.shoppingPrices.getValue(shopType).pick(random),
            credit = "",
            shopType = shopType,
        )
    }

    private fun expenses(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = balance.expensePrices.pick(random)
        val strayAnimal = random.nextInt(100) < balance.strayAnimalPercentage
        return BoardCard.Expenses(
            description = "",
            priceTitle = price.generatedPriceTitle(),
            price = price,
            payer = if (strayAnimal) PayerType.ALL else PayerTypes.pick(random),
            grantsAnimal = strayAnimal,
        )
    }

    private fun chance(type: BoardCardType, id: Int, random: Random): BoardCard {
        return when (chanceKind(id, random)) {
            ChanceKind.RANDOM_JOB -> BoardCard.Chance.RandomJob("", balance.randomJobProfits.pick(random))
            ChanceKind.ESTATE -> BoardCard.Chance.Estate(
                name = "",
                description = "",
                price = balance.estatePrices.pick(random),
            )

            ChanceKind.LAND -> {
                val area = balance.landAreas.pick(random)
                BoardCard.Chance.Land(
                    name = "",
                    description = "",
                    price = area * balance.landPricePerUnit.pick(random),
                    area = area,
                )
            }

            ChanceKind.SHARES -> BoardCard.Chance.Shares(
                description = "",
                price = balance.sharePrices.pick(random),
                maxCount = balance.shareCounts.pick(random),
                sharesType = balance.shares.pick(random).id,
            )

            ChanceKind.CORRUPT_BUSINESS -> corruptBusiness(random)
            ChanceKind.CORRUPT_LAND -> corruptLand(random)
        }
    }

    private fun corruptBusiness(random: Random): BoardCard {
        val price = balance.corruptBusinessPrices.pick(random)
        val oneTime = random.nextInt(100) < balance.corruptOneTimePercentage
        val profit = if (oneTime) 0 else price * balance.corruptBusinessReturnPercentages.pick(random) / 100
        val oneTimeProfit = if (oneTime) price * balance.corruptOneTimeReturnPercentages.pick(random) / 100 else 0
        val deputies = balance.corruptBusinessDeputies.pick(random)
        return BoardCard.Chance.CorruptBusiness("", price, profit, oneTimeProfit, deputies)
    }

    private fun corruptLand(random: Random): BoardCard {
        val area = balance.corruptLandAreas.pick(random)
        val price = area * balance.corruptLandPricePerUnit.pick(random)
        val deputies = balance.corruptLandDeputies.pick(random)
        return BoardCard.Chance.CorruptLand("", price, area, deputies)
    }

    private fun eventStore(type: BoardCardType, id: Int, random: Random): BoardCard {
        return when (eventKind(id, random)) {
            EventKind.LAND -> BoardCard.EventStore.Land(
                description = "",
                price = (balance.landPricePerUnit.pick(random) *
                        balance.eventLandPricePercentages.pick(random) / 100).coerceAtLeast(1),
            )
            EventKind.ESTATE -> BoardCard.EventStore.Estate(
                description = "",
                price = (balance.estatePrices.pick(random) *
                        balance.estateSalePercentages.pick(random) / 100).coerceAtLeast(1),
            )
            EventKind.SHARES -> {
                val forcedSaleAvailable = balance.forcedShareSalePrices.isNotEmpty()
                val forcedSale = forcedSaleAvailable && (
                        id == EventKind.SHARES.ordinal + 1 ||
                                random.nextInt(100) < balance.forcedShareSalePercentage
                        )
                BoardCard.EventStore.Shares(
                    sharesType = balance.shares.pick(random).id,
                    description = "",
                    price = if (forcedSale) {
                        balance.forcedShareSalePrices.pick(random)
                    } else {
                        balance.sharePrices.pick(random)
                    },
                    forcedSale = forcedSale,
                )
            }

            EventKind.BUSINESS_EXTENDING -> BoardCard.EventStore.BusinessExtending(
                description = "",
                profit = balance.businessExtensionProfits.pick(random),
            )

            EventKind.REELECTION -> BoardCard.EventStore.Reelection("")

            EventKind.ANNOUNCEMENT -> BoardCard.EventStore.Announcement("")

            EventKind.CORRUPT_BUSINESS -> BoardCard.EventStore.CorruptBusiness(
                description = "",
                salePercentage = balance.corruptBusinessSalePercentages.pick(random),
            )

            EventKind.CORRUPT_LAND -> BoardCard.EventStore.CorruptLand(
                description = "",
                price = balance.corruptLandPricePerUnit.pick(random) *
                        balance.corruptLandSalePercentages.pick(random) / 100,
            )
        }
    }

    private fun deputy(type: BoardCardType, id: Int, random: Random): BoardCard {
        val corrupt = random.nextInt(100) < balance.corruptDeputyPercentage
        return BoardCard.Deputy(
            description = "",
            corrupt = corrupt,
        )
    }

    private fun <T> List<T>.pick(random: Random): T = this[random.nextInt(size)]

    private fun chanceKind(id: Int, random: Random): ChanceKind =
        ChanceKind.entries.getOrNull(id - 1) ?: weighted(
            random,
            ChanceKind.RANDOM_JOB to balance.chanceWeights.randomJob,
            ChanceKind.ESTATE to balance.chanceWeights.estate,
            ChanceKind.LAND to balance.chanceWeights.land,
            ChanceKind.SHARES to balance.chanceWeights.shares,
            ChanceKind.CORRUPT_BUSINESS to balance.chanceWeights.corruptBusiness,
            ChanceKind.CORRUPT_LAND to balance.chanceWeights.corruptLand,
        )

    private fun eventKind(id: Int, random: Random): EventKind =
        EventKind.entries.getOrNull(id - 1) ?: weighted(
            random,
            EventKind.LAND to balance.eventWeights.land,
            EventKind.ESTATE to balance.eventWeights.estate,
            EventKind.SHARES to balance.eventWeights.shares,
            EventKind.BUSINESS_EXTENDING to balance.eventWeights.businessExtending,
            EventKind.REELECTION to balance.eventWeights.reelection,
            EventKind.ANNOUNCEMENT to balance.eventWeights.announcement,
            EventKind.CORRUPT_BUSINESS to balance.eventWeights.corruptBusiness,
            EventKind.CORRUPT_LAND to balance.eventWeights.corruptLand,
        )

    private fun <T> weighted(random: Random, vararg values: Pair<T, Int>): T = weighted(random, values.toMap())

    private fun <T> weighted(random: Random, weights: Map<T, Int>): T {
        var position = random.nextInt(weights.values.sum())
        weights.forEach { (value, weight) ->
            if (position < weight) return value
            position -= weight
        }
        return weights.keys.last()
    }

}
internal fun Long.expenseShare(percent: Long): Long = (this * percent / 100 / 10).coerceAtLeast(1) * 10

private const val PROFESSIONS_PER_GENDER = 30

private enum class ChanceKind { RANDOM_JOB, ESTATE, LAND, SHARES, CORRUPT_BUSINESS, CORRUPT_LAND }

private enum class EventKind {
    LAND,
    ESTATE,
    SHARES,
    BUSINESS_EXTENDING,
    REELECTION,
    ANNOUNCEMENT,
    CORRUPT_BUSINESS,
    CORRUPT_LAND,
}
private val PayerTypes = PayerType.entries

private fun Long.generatedPriceTitle(): String = "\$" + toString()
    .reversed()
    .chunked(3)
    .joinToString(" ")
    .reversed()
