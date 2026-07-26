package ua.vald_zx.game.rat.race.card

import ua.vald_zx.game.rat.race.card.beans.SharesType

fun SharesType.label(): String {
    return name.replace("SCT", "CST").replace("GS", "GC")
}
