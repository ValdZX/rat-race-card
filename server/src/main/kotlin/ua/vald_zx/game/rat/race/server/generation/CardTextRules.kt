package ua.vald_zx.game.rat.race.server.generation

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.seedFor
import kotlin.math.absoluteValue

internal fun CardText.isUsable(): Boolean = name.isNotBlank() && description.isNotBlank()

internal fun CardText.isSuitableFor(card: BoardCard, locale: String): Boolean =
    isUsable() && hasSpecificName(locale) &&
            (card !is BoardCard.Expenses || expensePurposeWords(locale).isNotEmpty())

internal fun CardText.hasSpecificName(locale: String): Boolean {
    val genericNames = if (locale.startsWith("en")) GENERIC_EN_CARD_NAMES else GENERIC_UK_CARD_NAMES
    return name.qualityNormalized() !in genericNames
}

internal fun CardText.expensePurposeWords(locale: String): List<String> {
    val ignored = if (locale.startsWith("en")) EN_EXPENSE_MECHANIC_WORDS else UK_EXPENSE_MECHANIC_WORDS
    return EXPENSE_WORD_PATTERN.findAll("$name $description".lowercase().replace("'", "").replace("’", ""))
        .map { it.value }
        .filterNot { it in ignored }
        .toList()
}

internal fun String.qualityNormalized(): String = lowercase()
    .replace("'", "")
    .replace("’", "")
    .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
    .trim()

internal fun CardText.collidesWith(other: CardText): Boolean =
    name.normalized() == other.name.normalized() || description.normalized() == other.description.normalized()

internal fun String.normalized(): String = trim().lowercase()

internal fun BoardCard.usesGeneratedName(): Boolean = true
internal val EXPENSE_WORD_PATTERN = Regex("\\p{L}+")
internal val UK_EXPENSE_MECHANIC_WORDS = setOf(
    "без", "винятку", "всі", "витрата", "витрати", "гравець", "гравці", "гравців", "для", "за",
    "заплатити", "кожен", "лише", "має", "обовязкова", "обовязковий", "оплата", "платіж", "платять",
    "повинен", "повинні", "сплата", "сплатити", "сплачують", "сума", "суму", "тільки", "умовою", "усі",
    "хто",
)
internal val EN_EXPENSE_MECHANIC_WORDS = setOf(
    "all", "amount", "everyone", "expense", "for", "mandatory", "must", "only", "pay", "pays", "payment",
    "players", "required", "the", "those", "who", "without",
)
internal val GENERIC_UK_CARD_NAMES = setOf(
    "акції", "бізнес", "великий бізнес", "випадковий заробіток", "випадок", "витрати", "депутат", "депутати",
    "земля", "корупційна земля", "корупційний бізнес", "малий бізнес", "нерухомість", "обовязкова витрата",
    "подія", "подія ринку", "покупка", "перевибори", "ринкова новина", "ринкова подія", "розширення бізнесу",
    "середній бізнес", "шанс",
)
internal val GENERIC_EN_CARD_NAMES = setOf(
    "big business", "business", "chance", "corrupt business", "corrupt land", "deputies", "deputy", "event",
    "expenses", "land", "mandatory expense", "market event", "market news", "medium business", "random job",
    "real estate", "reelection", "shares", "shopping", "small business",
)
internal fun deckName(type: BoardCardType) = when (type) {
    BoardCardType.SmallBusiness -> "small business"
    BoardCardType.MediumBusiness -> "medium business"
    BoardCardType.BigBusiness -> "big business"
    BoardCardType.Shopping -> "shopping"
    BoardCardType.Expenses -> "expense"
    BoardCardType.Chance -> "chance"
    BoardCardType.EventStore -> "market event"
    BoardCardType.Deputy -> "deputy"
}

internal fun BoardCard.narrativeAngle(world: BoardGeneration, id: Int): String? {
    val angles = when (this) {
        is BoardCard.Deputy -> DEPUTY_ANGLES
        is BoardCard.EventStore.Announcement -> ANNOUNCEMENT_ANGLES
        is BoardCard.EventStore.Reelection -> REELECTION_ANGLES
        else -> return null
    }
    val offset = (world.seedFor("angle", 0) % angles.size).toInt().absoluteValue
    return angles[(id + offset) % angles.size]
}

internal val DEPUTY_ANGLES = listOf(
    "how they came to power",
    "what the public knows them for",
    "the habit that gives them away",
    "who stands behind them",
    "which promise they broke",
    "where they spend their free time",
    "how they address voters",
    "what they fear most",
)

internal val ANNOUNCEMENT_ANGLES = listOf(
    "weather or a natural force",
    "new technology",
    "a rumor spreading through the world",
    "dry report figures",
    "a major scandal",
    "a festival or anniversary",
    "transport trouble",
    "supply disruption",
)

internal val REELECTION_ANGLES = listOf(
    "the term expired",
    "the council dissolved itself",
    "a scandal swept out the old team",
    "a court annulled the previous result",
    "voters took to the streets",
    "the old council resigned voluntarily",
)

internal fun BoardCard.brief(shareNames: Map<String, String>): String = when (this) {
    is BoardCard.SmallBusiness -> "buy a small business for $price; it adds $profit recurring income"
    is BoardCard.MediumBusiness -> "buy a medium business for $price; it adds $profit recurring income"
    is BoardCard.BigBusiness -> "buy a big business for $price; it adds $profit recurring income"
    is BoardCard.Shopping -> "buy ${shopType.promptAsset()} for $price; gain one such asset"
    is BoardCard.Expenses -> if (grantsAnimal) {
        "adopt a stray or rescued animal: pay $price, gain a pet permanently, then pay its recurring care cost"
    } else {
        "mandatory $price expense only for this condition: ${payer.promptRule()}"
    }
    is BoardCard.Deputy -> if (corrupt) {
        "a corrupt official joins the player as a deputy"
    } else {
        "an honest official does not join the player"
    }
    is BoardCard.EventStore.Shares -> if (forcedSale) {
        "every owner must sell all ${shareNames[sharesType] ?: sharesType} shares at the unfavorable price $price; no refusal or partial sale"
    } else {
        "${shareNames[sharesType] ?: sharesType} owners may sell any quantity at $price per share or decline"
    }
    is BoardCard.EventStore.Land -> "landowners may sell any area at $price per unit or decline"
    is BoardCard.EventStore.Estate -> "property owners may sell selected properties at $price each or decline"
    is BoardCard.EventStore.BusinessExtending -> "the active player adds $profit recurring income to one random small business; no effect without one"
    is BoardCard.EventStore.Reelection -> "reelection: every player loses all deputies"
    is BoardCard.EventStore.Announcement -> "market news with no direct financial effect"
    is BoardCard.EventStore.CorruptBusiness ->
        "owners may sell one previously acquired recurring corrupt business for $salePercentage percent of its purchase price or decline"
    is BoardCard.EventStore.CorruptLand ->
        "owners may sell any amount of previously acquired corrupt land at $price per area unit or decline"
    is BoardCard.Chance.RandomJob -> "the player immediately receives $profit one-time income"
    is BoardCard.Chance.Land -> "buy land of area $area for $price"
    is BoardCard.Chance.Estate -> "buy one property for $price"
    is BoardCard.Chance.Shares -> "buy up to $maxCount ${shareNames[sharesType] ?: sharesType} shares at $price each"
    is BoardCard.Chance.CorruptBusiness -> if (oneTimeProfit > 0) {
        "spend $deputies deputies and $price for an immediate $oneTimeProfit payout; no recurring income"
    } else {
        "spend $deputies deputies and $price for a corrupt business with $profit recurring income"
    }
    is BoardCard.Chance.CorruptLand -> "spend $deputies deputies and $price for corrupt land of area $area"
}

internal fun ShopType.promptAsset(): String = when (this) {
    ShopType.AUTO -> "a car"
    ShopType.HOUSE -> "a house"
    ShopType.APARTMENT -> "an apartment"
    ShopType.YACHT -> "a yacht"
    ShopType.FLY -> "a plane"
    ShopType.ANIMAL -> "a pet"
}

internal fun PayerType.promptRule(): String = when (this) {
    PayerType.ALL -> "everyone pays"
    PayerType.FREE_W_OR_MARRIED_M -> "only unmarried women and married men pay"
    PayerType.AUTO_OWNER -> "only car owners pay"
    PayerType.MEN -> "only men pay"
    PayerType.PARENT -> "only players with children pay"
    PayerType.MARRIED_M -> "only married men pay"
    PayerType.APARTMENT_OWNER -> "only apartment owners pay"
    PayerType.APARTMENT_OR_HOUSE_OWNER -> "only apartment or house owners pay"
    PayerType.ANIMAL_OWNER -> "only animal owners pay"
}
