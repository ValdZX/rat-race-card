package ua.vald_zx.game.rat.race.card.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.AppLanguage
import ua.vald_zx.game.rat.race.card.components.clickableSingle

@Composable
fun DesignLanguagePicker(
    selected: AppLanguage,
    modifier: Modifier = Modifier,
    onSelect: (AppLanguage) -> Unit,
) {
    val colors = Design.colors
    Row(
        modifier = modifier
            .plinth(colors.scaffold.outlineStrong, 3.dp, DesignShapes.full)
            .clip(DesignShapes.full)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.full)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppLanguage.entries.forEach { language ->
            LanguageOption(
                language = language,
                selected = language == selected,
                onClick = { onSelect(language) },
            )
        }
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = Design.colors
    val fill by animateColorAsState(
        targetValue = if (selected) colors.scaffold.accent else Color.Transparent,
        animationSpec = tween(160),
        label = "LanguageFill",
    )
    val ink by animateColorAsState(
        targetValue = if (selected) colors.scaffold.onAccent else colors.scaffold.onSurfaceMuted,
        animationSpec = tween(160),
        label = "LanguageInk",
    )
    Row(
        modifier = Modifier
            .clip(DesignShapes.full)
            .background(fill)
            .clickableSingle(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = language.flag,
            contentDescription = language.title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(21.dp)
                .height(14.dp)
                .clip(DesignShapes.cell)
                .border(1.dp, colors.scaffold.outlineStrong, DesignShapes.cell),
        )
        Text(
            text = language.label,
            style = Design.type.micro,
            color = ink,
        )
    }
}
