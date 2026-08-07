package ua.vald_zx.game.rat.race.card.shared

enum class PlayerStatus(val tier: Int) {
    FINALIST(1),
    MULTIMILLIONAIRE(2),
    CORRUPT(3),
    MAGNATE(4),
    MILLIONAIRE(5),
    BUSINESSMAN(6),
    ENTREPRENEUR(7),
    DEPUTY(8),
    RENTIER(9),
    INVESTOR(10),
    FAMILY(11),
    MARRIED(12),
    EMPLOYEE(13),
    FIRED(14),
    BROKE(15),
}

fun Player.status(): PlayerStatus {
    return when {
        location.trackId == CoreTrackIds.Outer -> PlayerStatus.FINALIST
        total() >= 10_000_000L -> PlayerStatus.MULTIMILLIONAIRE
        businesses.any { it.type == BusinessType.CORRUPTION } -> PlayerStatus.CORRUPT
        businesses.any { it.type == BusinessType.LARGE } -> PlayerStatus.MAGNATE
        total() >= 1_000_000L -> PlayerStatus.MILLIONAIRE
        total() < 0L -> PlayerStatus.BROKE
        businesses.any { it.type == BusinessType.MEDIUM } -> PlayerStatus.BUSINESSMAN
        businesses.any { it.type == BusinessType.SMALL } -> PlayerStatus.ENTREPRENEUR
        deputies > 0 -> PlayerStatus.DEPUTY
        businesses.none { it.type == BusinessType.WORK } -> PlayerStatus.FIRED
        babies >= 2 -> PlayerStatus.FAMILY
        flight > 0 || yacht > 0 -> PlayerStatus.RENTIER
        isMarried -> PlayerStatus.MARRIED
        fundAmount() > 0L -> PlayerStatus.INVESTOR
        else -> PlayerStatus.EMPLOYEE
    }
}
