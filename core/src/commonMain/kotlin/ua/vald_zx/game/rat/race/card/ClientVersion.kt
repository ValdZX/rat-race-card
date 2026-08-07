package ua.vald_zx.game.rat.race.card

data class ClientVersion(
    val version: String,
    val commit: String,
    val buildTime: String,
) {
    val label: String = "v$version ($commit)"

    val details: String = "$label · $buildTime"
}

var clientVersion = ClientVersion(version = "unknown", commit = "unknown", buildTime = "unknown")
