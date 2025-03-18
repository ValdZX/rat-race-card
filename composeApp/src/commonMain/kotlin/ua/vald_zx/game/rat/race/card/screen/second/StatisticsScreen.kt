package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.raceRate2store

class StatisticsScreen : Screen {
    @OptIn(ExperimentalKStoreApi::class)
    @Composable
    override fun Content() {
        val statistics = raceRate2store.statistics ?: return
        BottomSheetContainer {
            Column(modifier = Modifier.statusBarsPadding()) {
                Text("Кількість зарплат: ${statistics.salaryCount}")
                var needTotal by remember { mutableStateOf(true) }
                var needCashFlow by remember { mutableStateOf(true) }
                var needCash by remember { mutableStateOf(true) }
                var needDeposit by remember { mutableStateOf(true) }
                var needLoan by remember { mutableStateOf(true) }
                Card(
                    modifier = Modifier.height(270.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
                        LineChart(
                            data = remember(
                                needTotal,
                                needCashFlow,
                                needCash,
                                needDeposit,
                                needLoan
                            ) {
                                val lines = mutableListOf<Line>()
                                if (needTotal) {
                                    lines.add(
                                        Line(
                                            label = "Загальні статки",
                                            values = statistics.log.map { it.total().toDouble() },
                                            color = SolidColor(Color(0xFF2B8130)),
                                            firstGradientFillColor = Color(0xFF66BB6A).copy(alpha = .4f),
                                            curvedEdges = true,
                                            secondGradientFillColor = Color.Transparent,
                                            strokeAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationDelay = 0,
                                        )
                                    )
                                }
                                if (needCashFlow) {
                                    lines.add(
                                        Line(
                                            label = "CashFlow",
                                            values = statistics.log.map {
                                                it.cashFlow().toDouble()
                                            },
                                            color = SolidColor(Color(0xFFDA860C)),
                                            firstGradientFillColor = Color(0xFFFFA726).copy(alpha = .4f),
                                            curvedEdges = true,
                                            secondGradientFillColor = Color.Transparent,
                                            strokeAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationDelay = 0,
                                        )
                                    )
                                }
                                if (needCash) {
                                    lines.add(
                                        Line(
                                            label = "Готівка",
                                            values = statistics.log.map { it.cash.toDouble() },
                                            color = SolidColor(Color(0xFF23af92)),
                                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                                            curvedEdges = true,
                                            secondGradientFillColor = Color.Transparent,
                                            strokeAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationDelay = 0,
                                        )
                                    )
                                }
                                if (needDeposit) {
                                    lines.add(
                                        Line(
                                            label = "Депозит",
                                            values = statistics.log.map { it.deposit.toDouble() },
                                            color = SolidColor(Color(0xFF0F73C4)),
                                            firstGradientFillColor = Color(0xFF42A5F5).copy(alpha = .4f),
                                            curvedEdges = true,
                                            secondGradientFillColor = Color.Transparent,
                                            strokeAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationDelay = 0,
                                        )
                                    )
                                }
                                if (needLoan) {
                                    lines.add(
                                        Line(
                                            label = "Кредит",
                                            values = statistics.log.map { it.loan.toDouble() },
                                            color = SolidColor(Color.Red),
                                            firstGradientFillColor = Color.Red.copy(alpha = .5f),
                                            curvedEdges = true,
                                            secondGradientFillColor = Color.Transparent,
                                            strokeAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationSpec = spring(
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            gradientAnimationDelay = 0,
                                        )
                                    )
                                }
                                lines
                            },
                            animationDelay = 100,
                            animationMode = AnimationMode.Together(delayBuilder = { it * 300L }),
                            indicatorProperties = HorizontalIndicatorProperties(
                                textStyle = TextStyle(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                contentBuilder = {
                                    it.format(0) + " $"
                                },
                            ),
                            popupProperties = PopupProperties(
                                textStyle = TextStyle(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                contentBuilder = {
                                    it.format(0) + " $"
                                },
                                containerColor = Color(0xff414141)
                            ),
                            labelHelperProperties = LabelHelperProperties(
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Загальні статки")
                    Switch(needTotal, onCheckedChange = { needTotal = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CashFlow")
                    Switch(needCashFlow, onCheckedChange = { needCashFlow = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Готівка")
                    Switch(needCash, onCheckedChange = { needCash = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Депозит")
                    Switch(needDeposit, onCheckedChange = { needDeposit = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Кредит")
                    Switch(needLoan, onCheckedChange = { needLoan = it })
                }
            }
        }
    }
}