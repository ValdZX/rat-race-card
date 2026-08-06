package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.GoldBackground
import ua.vald_zx.game.rat.race.card.components.SilverBackground
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme

val greyScaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun StatePage(
    player: Player,
    selectedDream: Dream?,
    outerCircleConditions: OuterCircleConditions,
    victoryConditions: VictoryConditions,
    loanLimit: Long,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StateItem(
                name = stringResource(Res.string.work),
                imageVector = Images.Work,
                isPositivePrice = true,
                price = player.card.salary,
                enabled = player.businesses.any { it.type == BusinessType.WORK }
            )
            StateItem(
                name = stringResource(Res.string.marriage),
                imageVector = Images.Mariage,
                enabled = player.isMarried
            )
            StateItem(
                name = stringResource(Res.string.kids),
                imageVector = Images.Baby,
                enabled = player.babies > 0,
                count = player.babies,
                price = player.babies * player.config.babyCost
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StateItem(
                name = stringResource(Res.string.car),
                imageVector = Images.Car,
                enabled = player.cars > 0,
                count = player.cars,
                price = player.cars * player.config.carCost
            )
            StateItem(
                name = stringResource(Res.string.apartment),
                imageVector = Images.Flat,
                enabled = player.apartment > 0,
                count = player.apartment,
                price = player.apartment * player.config.apartmentCost
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StateItem(
                name = stringResource(Res.string.estate),
                imageVector = Images.Estate,
                enabled = player.cottage > 0,
                count = player.cottage,
                price = player.cottage * player.config.cottageCost
            )
            StateItem(
                name = stringResource(Res.string.yacht),
                imageVector = Images.Yacht,
                enabled = player.yacht > 0,
                count = player.yacht,
                price = player.yacht * player.config.yachtCost
            )
            StateItem(
                name = stringResource(Res.string.plane),
                imageVector = Images.Fly,
                enabled = player.flight > 0,
                count = player.flight,
                price = player.flight * player.config.flightCost
            )
            StateItem(
                name = stringResource(Res.string.pets),
                imageVector = Images.Rat,
                enabled = player.animal > 0,
                count = player.animal,
                price = player.animal * player.config.animalCost
            )
        }
        when (player.location.trackId.legacyLayerOrNull()) {
            BoardLayer.INNER -> {
                Text(
                    text = stringResource(Res.string.outer_circle_conditions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                ProgressCondition(
                    completed = player.cashFlow() >= outerCircleConditions.minimumCashFlow,
                    text = stringResource(
                        Res.string.cash_flow_progress,
                        player.cashFlow().formatAmount(),
                        outerCircleConditions.minimumCashFlow.formatAmount(),
                    ),
                )
                if (outerCircleConditions.apartmentRequired) {
                    ProgressCondition(
                        completed = player.apartment > 0,
                        text = stringResource(Res.string.apartment_required),
                    )
                }
                if (outerCircleConditions.carRequired) {
                    ProgressCondition(
                        completed = player.cars > 0,
                        text = stringResource(Res.string.car_required),
                    )
                }
                ProgressCondition(
                    completed = player.balance() >= outerCircleConditions.minimumAccountBalance,
                    text = stringResource(
                        Res.string.account_balance_progress,
                        player.balance().formatAmount(),
                        outerCircleConditions.minimumAccountBalance.formatAmount(),
                    ),
                )
            }

            BoardLayer.OUTER -> {
                Text(
                    text = stringResource(Res.string.victory_conditions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (victoryConditions.dreamRequired) {
                    ProgressCondition(
                        completed = player.selectedDreamId != null &&
                                player.selectedDreamId in player.purchasedDreamIds,
                        text = selectedDream?.name
                            ?: stringResource(Res.string.choose_dream_on_board),
                    )
                }
                if (victoryConditions.planeRequired) {
                    ProgressCondition(
                        completed = player.flight > 0,
                        text = stringResource(Res.string.plane),
                    )
                }
                if (victoryConditions.estateRequired) {
                    ProgressCondition(
                        completed = player.cottage > 0,
                        text = stringResource(Res.string.estate),
                    )
                }
                ProgressCondition(
                    completed = player.balance() >= victoryConditions.minimumAccountBalance,
                    text = stringResource(
                        Res.string.victory_account_balance_value,
                        victoryConditions.minimumAccountBalance.splitDecimal(),
                    ),
                )
            }

            null -> Unit
        }
        DetailsField(
            stringResource(Res.string.active_profit),
            player.activeProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            stringResource(Res.string.passive_profit),
            player.passiveProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            stringResource(Res.string.total_profit),
            player.totalProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            stringResource(Res.string.loanLimit),
            loanLimit.toString(),
            MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            stringResource(Res.string.credit_expenses),
            player.creditExpenses().toString(),
            MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            stringResource(Res.string.total_expenses),
            player.totalExpenses().toString(),
            MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun ProgressCondition(
    completed: Boolean,
    text: String,
) {
    Text(
        text = "${if (completed) "✓" else "○"} $text",
        color = if (completed) AppTheme.colors.cash else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
fun StateItem(
    name: String,
    imageVector: ImageVector,
    price: Long = 0,
    isPositivePrice: Boolean = false,
    count: Long = 0,
    enabled: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .composed {
                onClick?.let { clickable(onClick = onClick) } ?: this
            }
    ) {
        if (enabled) {
            GoldBackground(modifier = Modifier.padding(4.dp).clip(CircleShape).matchParentSize())
        } else {
            SilverBackground(modifier = Modifier.padding(4.dp).clip(CircleShape).matchParentSize())
        }
        Image(
            imageVector = imageVector,
            contentDescription = null,
            colorFilter = if (enabled) null else greyScaleFilter,
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
        )
        if (count > 1) {
            Text(
                count.toString(),
                fontSize = 11.sp,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterEnd),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
        Text(
            name,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.surface,
            lineHeight = 13.sp
        )
        if (price > 0) {
            Text(
                "${price.splitDecimal()} $",
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(
                        if (isPositivePrice) {
                            AppTheme.colors.cash
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                color = MaterialTheme.colorScheme.onError,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
        }
    }
}
