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
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.GoldBackground
import ua.vald_zx.game.rat.race.card.components.SilverBackground
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme

val greyScaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun StatePage(player: Player) {
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
        modifier = Modifier.padding(top = 8.dp)
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
            modifier = Modifier.padding(16.dp)
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