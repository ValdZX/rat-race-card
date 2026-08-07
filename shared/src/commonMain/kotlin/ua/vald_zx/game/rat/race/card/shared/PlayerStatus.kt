package ua.vald_zx.game.rat.race.card.shared

enum class PlayerStatus {
    MAGNATE,
    MILLIONAIRE,
    BROKE,
    BUSINESSMAN,
    ENTREPRENEUR,
    RENTIER,
    INVESTOR,
    FIRED,
    EMPLOYEE,
}

fun Player.status(): PlayerStatus {
    return when {
        businesses.any { it.type == BusinessType.LARGE || it.type == BusinessType.CORRUPTION } -> PlayerStatus.MAGNATE
        total() >= 1_000_000L -> PlayerStatus.MILLIONAIRE
        total() < 0L -> PlayerStatus.BROKE
        businesses.any { it.type == BusinessType.MEDIUM } -> PlayerStatus.BUSINESSMAN
        businesses.any { it.type == BusinessType.SMALL } -> PlayerStatus.ENTREPRENEUR
        businesses.none { it.type == BusinessType.WORK } -> PlayerStatus.FIRED
        flight > 0 || yacht > 0 -> PlayerStatus.RENTIER
        fundAmount() > 0L -> PlayerStatus.INVESTOR
        else -> PlayerStatus.EMPLOYEE
    }
}
