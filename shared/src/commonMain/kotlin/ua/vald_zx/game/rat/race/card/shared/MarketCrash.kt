package ua.vald_zx.game.rat.race.card.shared

fun Board.sectorOf(shareType: String): String {
    val generated = generatedBalance?.shares?.firstOrNull { it.id == shareType }?.sector
    if (!generated.isNullOrBlank()) return generated
    return legacyShareSectors[shareType] ?: ShareSectors.INDUSTRY
}

data class ShareMarkdown(
    val shares: Shares,
    val dropPercentage: Long,
    val lostValue: Long,
)

data class CrashOutcome(
    val shares: List<Shares>,
    val markdowns: List<ShareMarkdown>,
) {
    val lostValue: Long
        get() = markdowns.sumOf { it.lostValue }
}

fun Board.applyMarketCrash(shares: List<Shares>, card: BoardCard.EventStore.MarketCrash): CrashOutcome {
    val markdowns = mutableListOf<ShareMarkdown>()
    val updated = shares.map { holding ->
        val drop = if (sectorOf(holding.type) == card.sector) {
            card.sectorDropPercentage
        } else {
            card.marketDropPercentage
        }
        if (drop <= 0) return@map holding
        val survivingPrice = holding.buyPrice * (100 - drop.coerceAtMost(100)) / 100
        val marked = holding.copy(buyPrice = survivingPrice)
        markdowns += ShareMarkdown(
            shares = marked,
            dropPercentage = drop,
            lostValue = holding.price - marked.price,
        )
        marked
    }
    return CrashOutcome(updated, markdowns)
}
