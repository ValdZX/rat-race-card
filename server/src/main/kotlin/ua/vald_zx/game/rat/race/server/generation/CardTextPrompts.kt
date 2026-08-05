package ua.vald_zx.game.rat.race.server.generation

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.generatedLocales

internal fun cardBrief(
    world: BoardGeneration,
    id: Int,
    card: BoardCard,
    shareNames: Map<String, String>,
): String {
    val angle = card.narrativeAngle(world, id)?.let { "; narrative angle: $it" }.orEmpty()
    return "$id. ${card.brief(shareNames)}$angle"
}

internal fun professionBrief(card: ProfessionCard): String =
    "${card.id}. ${if (card.gender == Gender.FEMALE) "woman" else "man"}, salary ${card.salary}"

internal fun systemPrompt() = buildString {
    append("Write card text for an economic board game. Return only a JSON array of objects: ")
    append("{\"id\":number,\"uk\":{\"name\":string,\"description\":string},")
    append("\"en\":{\"name\":string,\"description\":string}}. ")
    append("No explanations or markdown. For every ID, return natural Ukrainian and English text.")
}

internal fun deckPrompt(
    world: BoardGeneration,
    type: BoardCardType,
    briefs: String,
    acceptedNames: List<String>,
    attempt: Int,
    rejected: Map<Int, List<Map<String, CardText>>>,
) = buildString {
    append(worldPrompt(world))
    append("Create a unique name and description (max 140 characters) for each ${deckName(type)} card. ")
    append("Use light, clever humor tied to the world and card situation. ")
    append("The name is the main visible title: it must specifically identify an object, character, or event, not merely the deck type. ")
    append("Do not repeat plots, names, or descriptions. ")
    when (type) {
        BoardCardType.SmallBusiness,
        BoardCardType.MediumBusiness,
        BoardCardType.BigBusiness -> {
            append("Name the specific business; describe what it does and how it earns income in this world. ")
            append("The UI shows price and income separately; do not merely restate them. ")
        }

        BoardCardType.Shopping -> {
            append("Name the specific purchase; describe what is bought and why it matters in this world. ")
            append("The UI shows asset type and price separately; they are not the purchase's story. ")
        }

        BoardCardType.Expenses -> {
            append("Name the specific paid item, service, or event; describe what happened and why payment is due. ")
            append("The UI shows amount and payer condition separately; do not repeat them or use them as the reason. ")
            append("For an animal card, explain its rescue or adoption and ongoing care. ")
        }

        BoardCardType.Chance -> {
            append("Name the specific opportunity, side job, asset, or corrupt scheme. ")
            append("Describe the situation, then convey the row's exact effect naturally. ")
        }

        BoardCardType.EventStore -> {
            append("Use a specific market-news headline; describe its cause and exact impact on players or assets. ")
        }

        BoardCardType.Deputy -> {
            append("Name a specific official, role, or scandal; describe the situation and its honest or corrupt outcome. ")
        }
    }
    append("Each row's mechanic is exact and mandatory. Add no numbers, conditions, or effects. ")
    append("Return exactly one object per supplied ID. uk and en must convey the same meaning in their respective languages.\n")
    appendAcceptedNames(acceptedNames)
    appendRetryInstructions(attempt, rejected)
    append("Cards:\n")
    append(briefs)
}

internal fun professionPrompt(
    world: BoardGeneration,
    briefs: String,
    acceptedNames: List<String>,
    attempt: Int,
    rejected: Map<Int, List<Map<String, CardText>>>,
) = buildString {
    append(worldPrompt(world))
    append("Create a unique profession name and description for each row. ")
    append("Use light, clever humor tied to the profession and world. ")
    append("The name is the card title and must identify a specific profession or position, not the category 'Profession'. ")
    append("It must fit the gender, world, and salary level. Gender affects marriage, children, divorce, and conditional expenses, so avoid contradictions. ")
    append("The description (max 140 characters) explains the daily role and salary source in the given theme, location, and era. ")
    append("Do not repeat names or descriptions. Return exactly one object per supplied ID. ")
    append("uk and en must describe the same profession in their respective languages.\n")
    appendAcceptedNames(acceptedNames)
    appendRetryInstructions(attempt, rejected)
    append("Professions:\n")
    append(briefs)
}

internal fun dreamPrompt(
    world: BoardGeneration,
    briefs: String,
    acceptedNames: List<String>,
    attempt: Int,
    rejected: Map<Int, List<Map<String, CardText>>>,
) = buildString {
    append(worldPrompt(world))
    append("Create a unique grand-dream name and description for each row. ")
    append("A dream is not an asset or business, but why a player escapes the rat race: a deed, place, or life's work suited to this world. ")
    append("The name is the title and must identify a specific goal, not the category 'Dream'. ")
    append("Price conveys scale: more expensive dreams are grander. ")
    append("The description (max 140 characters) explains what the player accomplishes or creates without repeating the price. ")
    append("Do not repeat names or descriptions. Return exactly one object per supplied ID. ")
    append("uk and en must describe the same dream in their respective languages.\n")
    appendAcceptedNames(acceptedNames)
    appendRetryInstructions(attempt, rejected)
    append("Dreams:\n")
    append(briefs)
}

internal fun StringBuilder.appendAcceptedNames(names: List<String>) {
    if (names.isEmpty()) return
    append("Already used names; do not repeat them:\n")
    names.forEach { name -> append("- $name\n") }
}

internal fun StringBuilder.appendRetryInstructions(
    attempt: Int,
    rejected: Map<Int, List<Map<String, CardText>>>,
) {
    if (attempt == 0) return
    append("Retry: the prior response failed validation or omitted IDs. Create new variants for every ID below. ")
    append("Copy each numeric ID exactly; do not replace it with 1 or omit uk/en, name, or description.\n")
    if (rejected.isEmpty()) return
    append("Do not repeat these rejected names or descriptions:\n")
    rejected.forEach { (id, attempts) ->
        attempts.takeLast(MAX_REJECTED_TEXTS_IN_PROMPT).forEach { localized ->
            append("$id: ")
            append(generatedLocales.joinToString("; ") { locale ->
                val text = localized.getValue(locale)
                "$locale «${text.name}» — «${text.description}»"
            })
            append('\n')
        }
    }
}

internal fun worldPrompt(world: BoardGeneration) = buildString {
    append("Game world: ")
    append(listOfNotNull(
        world.theme.ifBlank { null }?.let { "theme: $it" },
        world.locality.ifBlank { null }?.let { "location: $it" },
        world.epoch.ifBlank { null }?.let { "era: $it" },
    ).joinToString(", ").ifBlank { "an ordinary modern city" })
    append(".\n")
}


