package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignBoardListRenderTest {

    private val boards = listOf(
        BoardId(id = "1", name = "Пʼятничні перегони", createDateTime = LocalDateTime(2026, 7, 31, 19, 40, 0)),
        BoardId(id = "2", name = "Партія з Данилом", createDateTime = LocalDateTime(2026, 8, 1, 12, 5, 0)),
    )

    @Test
    fun listShowsBoardsAndOpensTheTappedOne() = runComposeUiTest {
        var opened: BoardId? = null
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(420.dp, 520.dp).testTag("list")) {
                    DesignBoardList(
                        boards = boards,
                        isLoading = false,
                        onBack = {},
                        onCreate = {},
                        onOpen = { opened = it },
                    )
                }
            }
        }
        waitForIdle()

        onNodeWithText("Партія з Данилом").performClick()
        assertEquals("2", opened?.id)

        val image = onNodeWithTag("list").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-board-list.png"))
    }

    @Test
    fun emptyListExplainsWhatToDo() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(420.dp, 400.dp)) {
                    DesignBoardList(
                        boards = emptyList(),
                        isLoading = false,
                        onBack = {},
                        onCreate = {},
                        onOpen = {},
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithText("No tables yet", substring = true).assertExists()
    }
}
