package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
data class Dream(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
)

val ratRaceDreams = listOf(
    Dream(
        id = "world_trip",
        name = "Навколосвітня подорож",
        description = "Рік подорожувати світом без робочих дзвінків і термінових зустрічей.",
        price = 1_000_000,
    ),
    Dream(
        id = "education_center",
        name = "Освітній центр",
        description = "Відкрити сучасний безкоштовний центр розвитку для дітей і дорослих.",
        price = 1_500_000,
    ),
    Dream(
        id = "charity_foundation",
        name = "Благодійний фонд",
        description = "Створити фонд, який системно допомагає людям змінювати своє життя.",
        price = 2_000_000,
    ),
    Dream(
        id = "film_studio",
        name = "Власна кіностудія",
        description = "Знімати історії, які надихають мільйони людей по всьому світу.",
        price = 2_500_000,
    ),
    Dream(
        id = "eco_village",
        name = "Екопоселення",
        description = "Побудувати автономне поселення з чистою енергією та власною фермою.",
        price = 3_000_000,
    ),
    Dream(
        id = "children_centers",
        name = "Мережа дитячих центрів",
        description = "Створити простори, де діти навчаються підприємництву та творчості.",
        price = 3_500_000,
    ),
    Dream(
        id = "winery",
        name = "Сімейна виноробня",
        description = "Вирощувати виноград і приймати друзів у власному маєтку.",
        price = 4_000_000,
    ),
    Dream(
        id = "football_club",
        name = "Футбольний клуб",
        description = "Придбати клуб, виховати чемпіонів і побудувати стадіон.",
        price = 5_000_000,
    ),
    Dream(
        id = "island_resort",
        name = "Курорт на острові",
        description = "Створити камерний курорт посеред океану для родини та друзів.",
        price = 6_000_000,
    ),
    Dream(
        id = "polar_expedition",
        name = "Полярна експедиція",
        description = "Організувати власну наукову експедицію до обох полюсів Землі.",
        price = 6_500_000,
    ),
    Dream(
        id = "modern_art_museum",
        name = "Музей сучасного мистецтва",
        description = "Зібрати колекцію та відкрити музей із вільним входом.",
        price = 7_000_000,
    ),
    Dream(
        id = "private_university",
        name = "Власний університет",
        description = "Заснувати університет для майбутніх підприємців і винахідників.",
        price = 8_000_000,
    ),
    Dream(
        id = "medical_foundation",
        name = "Медичний фонд",
        description = "Фінансувати складні операції та дослідження рідкісних хвороб.",
        price = 8_500_000,
    ),
    Dream(
        id = "nature_reserve",
        name = "Приватний заповідник",
        description = "Зберегти велику природну територію та повернути туди диких тварин.",
        price = 9_000_000,
    ),
    Dream(
        id = "private_island",
        name = "Приватний острів",
        description = "Придбати власний острів із будинком, причалом і злітною смугою.",
        price = 10_000_000,
    ),
    Dream(
        id = "world_regatta",
        name = "Навколосвітня регата",
        description = "Зібрати команду й пройти під вітрилами навколо світу.",
        price = 11_000_000,
    ),
    Dream(
        id = "space_trip",
        name = "Політ у космос",
        description = "Побачити Землю з орбіти та повернутися з історією на все життя.",
        price = 12_000_000,
    ),
    Dream(
        id = "research_institute",
        name = "Науковий інститут",
        description = "Створити незалежний інститут для сміливих технологічних досліджень.",
        price = 13_000_000,
    ),
    Dream(
        id = "future_city",
        name = "Місто майбутнього",
        description = "Збудувати експериментальний район без заторів і шкідливих викидів.",
        price = 14_000_000,
    ),
    Dream(
        id = "global_media",
        name = "Глобальна медіакомпанія",
        description = "Створити міжнародне медіа, якому довіряють у всьому світі.",
        price = 15_000_000,
    ),
    Dream(
        id = "ocean_station",
        name = "Океанічна станція",
        description = "Побудувати дослідницьку станцію для захисту світового океану.",
        price = 16_000_000,
    ),
    Dream(
        id = "mars_mission",
        name = "Місія на Марс",
        description = "Профінансувати приватну місію, що зробить людство міжпланетним.",
        price = 20_000_000,
    ),
)

val dreamSlotIds: List<String> = ratRaceDreams.map { it.id }

fun List<Player>.dreamSelectors(dreamId: String): List<Player> =
    filter { it.selectedDreamId == dreamId }

fun Board.canSelectDream(dreamId: String, playerId: String, players: List<Player>): Boolean =
    dreams.any { it.id == dreamId } &&
            dreamId !in purchasedDreamIds &&
            players.dreamSelectors(dreamId).none { it.id != playerId }

fun Board.dreamById(id: String?, locale: String = DEFAULT_LOCALE): Dream? {
    val dream = dreams.find { it.id == id } ?: return null
    val text = textsFor(locale).dreams[dream.id] ?: return dream
    return dream.copy(
        name = text.name.ifBlank { dream.name },
        description = text.description.ifBlank { dream.description },
    )
}
