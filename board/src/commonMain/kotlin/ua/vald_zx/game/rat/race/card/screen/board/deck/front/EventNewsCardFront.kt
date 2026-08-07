package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.sectorOf
import ua.vald_zx.game.rat.race.card.formatAmount

@Composable
fun BoxWithConstraintsScope.ReelectionCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Reelection,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    NewsCard(
        cardLink = cardLink,
        title = card.name.ifBlank { stringResource(Res.string.reelection) },
        description = card.description,
        footer = stringResource(Res.string.deputies_owned, state.player.deputies),
        vm = vm,
        onConfirm = { vm.reelection() },
    )
}

@Composable
fun BoxWithConstraintsScope.AnnouncementCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.Announcement,
    vm: BoardViewModel,
) {
    NewsCard(
        cardLink = cardLink,
        title = card.name.ifBlank { stringResource(Res.string.market_news) },
        description = card.description,
        footer = null,
        vm = vm,
        onConfirm = { vm.pass() },
    )
}

@Composable
fun BoxWithConstraintsScope.MarketCrashCardFront(
    cardLink: CardLink,
    card: BoardCard.EventStore.MarketCrash,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    val exposure = state.player.sharesList
        .filter { state.board.sectorOf(it.type) == card.sector }
        .sumOf { it.price }
    NewsCard(
        cardLink = cardLink,
        title = card.name.ifBlank { stringResource(Res.string.market_crash) },
        description = card.description,
        footer = stringResource(
            Res.string.market_crash_exposure,
            card.sectorDropPercentage.toString(),
            card.marketDropPercentage.toString(),
            exposure.formatAmount(),
        ),
        vm = vm,
        onConfirm = { vm.applyMarketCrash(card) },
    )
}

@Composable
private fun BoxWithConstraintsScope.NewsCard(
    cardLink: CardLink,
    title: String,
    description: String,
    footer: String?,
    vm: BoardViewModel,
    onConfirm: () -> Unit,
) {
    val state by vm.uiState.collectAsState()
    val density = LocalDensity.current
    val cardWidth = max(maxWidth, 100.dp)
    val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
    val unitDp = cardWidth / 300
    val padding = unitDp * 10
    val smallPadding = unitDp * 6

    Column(modifier = Modifier.padding(padding)) {
        Row {
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(end = padding, top = smallPadding),
                fontSize = unitTS * 14,
                lineHeight = unitTS * 19,
                fontWeight = FontWeight.Bold,
            )
            CardStamp(
                glyph = stringResource(Res.string.deputy_short),
                unitTS = unitTS,
                unitDp = unitDp,
                id = cardLink.id,
                glyphSize = 20f,
            )
        }
        Text(
            modifier = Modifier.padding(top = smallPadding),
            text = description,
            fontSize = unitTS * 11,
            lineHeight = unitTS * 15,
        )
        if (footer != null) {
            Text(
                modifier = Modifier.padding(top = padding).align(Alignment.CenterHorizontally),
                text = footer,
                fontSize = unitTS * 12,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        if (state.currentPlayerIsActive) {
            EButton(
                kind = DesignButtonKind.Filled,
                enabled = !state.isProgress,
                modifier = Modifier.padding(top = smallPadding).align(Alignment.CenterHorizontally),
                onClick = onConfirm,
                title = stringResource(Res.string.ok),
                unitTS = unitTS,
                unitDp = unitDp,
            )
        }
    }
}
