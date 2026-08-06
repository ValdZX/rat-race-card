package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.*

internal fun legacyCardOptions(place: PlaceType, player: Player): List<BoardCardType> = when (place) {
    PlaceType.BigBusiness -> listOf(BoardCardType.BigBusiness)
    PlaceType.Business -> when {
        player.businesses.any { it.type == BusinessType.LARGE } -> listOf(BoardCardType.BigBusiness)
        player.businesses.any { it.type == BusinessType.MEDIUM } ->
            listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness)

        player.businesses.any { it.type == BusinessType.SMALL } ->
            listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness)

        else -> listOf(BoardCardType.SmallBusiness)
    }

    PlaceType.Chance -> listOf(BoardCardType.Chance)
    PlaceType.Deputy -> listOf(BoardCardType.Deputy)
    PlaceType.Expenses -> listOf(BoardCardType.Expenses)
    PlaceType.Shopping -> listOf(BoardCardType.Shopping)
    PlaceType.Store -> listOf(BoardCardType.EventStore)
    else -> emptyList()
}

internal fun Player.afterChildCell(): Player {
    val eligible = card.gender == Gender.FEMALE || card.gender == Gender.MALE && isMarried
    return if (eligible) copy(babies = babies + 1, cash = cash + config.childBenefit) else this
}

internal fun Player.afterDivorceCell(): Player {
    if (!isMarried) return this
    if (card.gender == Gender.FEMALE) return copy(isMarried = false)
    val retained = config.divorceAssetRetentionPercentage
    return copy(
        isMarried = false,
        babies = 0,
        cash = cash * retained / 100,
        deposit = deposit * retained / 100,
    )
}

internal fun Player.afterResignationCell(): Player {
    val work = businesses.firstOrNull { it.type == BusinessType.WORK } ?: return this
    return copy(businesses = businesses - work)
}

internal fun Player.afterLoveCell(): Player = if (isMarried) this else copy(isMarried = true)

internal fun Player.afterRestCell(): Player = copy(inRest = config.restTurnCount)

internal fun Player.afterBankruptcyCell(random: GameRandom): Pair<Player, Business?> {
    val removed = random.choose(businesses.filter { it.type != BusinessType.WORK }) ?: return this to null
    return copy(businesses = businesses - removed) to removed
}

internal fun Board.nextActivePlayer(players: List<Player>): Player? {
    val active = activePlayers(players)
    if (active.isEmpty()) return null
    val currentIndex = active.indexOfFirst { it.id == activePlayerId }
    return active[if (currentIndex < 0 || currentIndex == active.lastIndex) 0 else currentIndex + 1]
}

internal fun marketEventIsComplete(processedPlayerIds: Set<String>, ownerIds: Set<String>): Boolean {
    return ownerIds.isEmpty() || processedPlayerIds.containsAll(ownerIds)
}
