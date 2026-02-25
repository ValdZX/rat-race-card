package ua.vald_zx.game.rat.race.server.data

fun generateStableDbId(str1: String, str2: String): String {
    val combined = "$str1|$str2"
    val bytes = combined.encodeToByteArray()

    var h1 = 0x9368e53c2f6af274UL
    var h2 = 0x586dcd208f7cd3fdUL

    for (b in bytes) {
        val k = b.toULong()

        h1 = (h1 xor k) * 0xff51afd7ed558ccdUL
        h2 = (h2 xor k) * 0xc4ceb9fe1a85ec53UL

        h1 = h1.rotateLeft(31)
        h2 = h2.rotateLeft(27)

        h1 += h2
        h2 += h1
    }

    return toUuidLikeString(h1, h2)
}

private fun ULong.rotateLeft(bits: Int): ULong =
    (this shl bits) or (this shr (64 - bits))

private fun toUuidLikeString(high: ULong, low: ULong): String {
    val hex = high.toString(16).padStart(16, '0') +
            low.toString(16).padStart(16, '0')

    return buildString {
        append(hex.substring(0, 8))
        append("-")
        append(hex.substring(8, 12))
        append("-")
        append(hex.substring(12, 16))
        append("-")
        append(hex.substring(16, 20))
        append("-")
        append(hex.substring(20, 32))
    }
}