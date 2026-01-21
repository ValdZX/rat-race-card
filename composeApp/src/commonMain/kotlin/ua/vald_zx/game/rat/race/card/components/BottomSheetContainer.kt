package ua.vald_zx.game.rat.race.card.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.noIme

@Composable
fun BottomSheetContainer(
    verticalScrollState: ScrollState? = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    if (noIme) {
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current
        val screenHeight = with(density) { windowInfo.containerSize.height.dp }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .navigationBarsPadding()
                .optionalModifier(verticalScrollState != null) {
                    verticalScroll(verticalScrollState!!)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                content()
                Spacer(Modifier.fillMaxWidth().height(screenHeight / 2))
            },
        )
    } else {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
                .optionalModifier(verticalScrollState != null) {
                    verticalScroll(verticalScrollState!!)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}