package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.dateFullDotsFormat
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.BoardId

@Composable
fun DesignBoardList(
    boards: List<BoardId>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (BoardId) -> Unit,
) {
    val colors = Design.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(DesignShapes.sm)
                        .background(colors.scaffold.surface3)
                        .border(1.dp, colors.scaffold.outline, DesignShapes.sm)
                        .clickableSingle(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Images.Back, contentDescription = null, tint = colors.scaffold.onSurface)
                }
                Text(
                    text = stringResource(Res.string.online_game),
                    style = Design.type.title,
                    color = colors.scaffold.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            DesignButton(
                text = stringResource(Res.string.new_table),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
                onClick = onCreate,
            )

            if (boards.isEmpty() && !isLoading) {
                Text(
                    text = stringResource(Res.string.no_tables_yet),
                    style = Design.type.body,
                    color = colors.scaffold.onSurfaceMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(boards) { board -> BoardRow(board) { onOpen(board) } }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colors.scaffold.accent,
            )
        }
    }
}

@Composable
private fun BoardRow(board: BoardId, onClick: () -> Unit) {
    val colors = Design.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .plinth(colors.scaffold.outlineStrong, 3.dp, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .clickableSingle(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(36.dp)
                .clip(DesignShapes.xs)
                .background(Design.semantic.business.edge)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = board.name,
                style = Design.type.subtitle,
                color = colors.scaffold.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = board.createDateTime.format(dateFullDotsFormat),
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
    }
}
