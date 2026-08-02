package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
internal fun AssetRow(
    title: String,
    subtitle: String?,
    amount: Long,
    tone: SemanticTone,
    profit: Long? = null,
) {
    val colors = Design.colors
    val type = Design.type
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(start = 4.dp, top = 10.dp, bottom = 10.dp)
                .width(4.dp)
                .height(44.dp)
                .clip(DesignShapes.xs)
                .background(tone.edge)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = type.subtitle,
                color = colors.scaffold.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = type.monoMeta,
                    color = colors.scaffold.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 12.dp),
        ) {
            Text(
                text = amount.splitDecimal(),
                style = type.amountMd,
                color = colors.scaffold.onSurface,
                maxLines = 1,
                softWrap = false,
            )
            if (profit != null) {
                Text(
                    text = profit.formatSigned(),
                    style = type.monoMeta,
                    color = if (profit >= 0) Design.semantic.positive.edge else Design.semantic.negative.edge,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Text(
        text = stringResource(Res.string.no_assets),
        style = Design.type.body,
        color = Design.scaffold.onSurfaceMuted,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

@Composable
private fun AssetList(isEmpty: Boolean, content: LazyListScopeContent) {
    if (isEmpty) {
        Column(modifier = Modifier.fillMaxSize()) { EmptyState() }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

private typealias LazyListScopeContent = androidx.compose.foundation.lazy.LazyListScope.() -> Unit

@Composable
internal fun DesignBusinessPage(player: Player) {
    val businesses = player.businesses.filter { it.type != BusinessType.WORK }
    AssetList(businesses.isEmpty()) {
        items(businesses) { business ->
            AssetRow(
                title = business.name,
                subtitle = business.typeLabel(),
                amount = business.price,
                tone = business.tone(),
                profit = business.profit + business.extentions.sum(),
            )
        }
    }
}

@Composable
private fun Business.typeLabel(): String {
    val name = when (type) {
        BusinessType.WORK -> ""
        BusinessType.SMALL -> stringResource(Res.string.small_business)
        BusinessType.MEDIUM -> stringResource(Res.string.medium_business)
        BusinessType.LARGE -> stringResource(Res.string.large_business)
        BusinessType.CORRUPTION -> stringResource(Res.string.corruption_business)
    }
    return if (extentions.isEmpty()) name else "$name · +${extentions.size}"
}

@Composable
private fun Business.tone(): SemanticTone = when {
    alarmed -> Design.semantic.negative
    type == BusinessType.CORRUPTION -> Design.semantic.bankruptcy
    type == BusinessType.LARGE -> Design.semantic.bigBusiness
    type == BusinessType.SMALL -> Design.semantic.smallBusiness
    else -> Design.semantic.business
}

@Composable
internal fun DesignSharesPage(player: Player) {
    val quantityWord = stringResource(Res.string.quantity)
    AssetList(player.sharesList.isEmpty()) {
        items(player.sharesList) { shares ->
            AssetRow(
                title = shares.type.name.replace("ShchHP", "ЩГП"),
                subtitle = "$quantityWord ${shares.count} × ${shares.buyPrice.splitDecimal()}",
                amount = shares.price,
                tone = Design.semantic.funds,
            )
        }
    }
}

@Composable
internal fun DesignLandPage(player: Player) {
    val areaWord = stringResource(Res.string.area)
    AssetList(player.landList.isEmpty()) {
        items(player.landList) { land ->
            AssetRow(
                title = land.name,
                subtitle = "$areaWord ${land.area}",
                amount = land.price,
                tone = Design.semantic.buy,
            )
        }
    }
}

@Composable
internal fun DesignEstatePage(player: Player) {
    AssetList(player.estateList.isEmpty()) {
        items(player.estateList) { estate ->
            AssetRow(
                title = estate.name,
                subtitle = null,
                amount = estate.price,
                tone = Design.semantic.shopping,
            )
        }
    }
}

@Composable
internal fun DesignFundsPage(player: Player) {
    val fundWord = stringResource(Res.string.fund_at)
    AssetList(player.funds.isEmpty()) {
        items(player.funds) { fund ->
            AssetRow(
                title = "$fundWord ${fund.rate}%",
                subtitle = null,
                amount = fund.amount,
                tone = Design.semantic.funds,
                profit = (fund.rate / 100.0 * fund.amount).toLong(),
            )
        }
    }
}
