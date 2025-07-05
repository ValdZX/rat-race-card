package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.GoldBackground
import ua.vald_zx.game.rat.race.card.components.SilverBackground
import ua.vald_zx.game.rat.race.card.logic.CardState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Baby
import ua.vald_zx.game.rat.race.card.resource.images.Car
import ua.vald_zx.game.rat.race.card.resource.images.Estate
import ua.vald_zx.game.rat.race.card.resource.images.Flat
import ua.vald_zx.game.rat.race.card.resource.images.Fly
import ua.vald_zx.game.rat.race.card.resource.images.Mariage
import ua.vald_zx.game.rat.race.card.resource.images.Work
import ua.vald_zx.game.rat.race.card.resource.images.Yacht
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme

val greyScaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun StatePage(state: CardState, player: Player) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StateItem(
                name = "Робота",
                imageVector = Images.Work,
                isPositivePrice = true,
                price = player.playerCard.salary,
                enabled = state.business.any { it.type == BusinessType.WORK }
            )
            StateItem(
                name = "Шлюб",
                imageVector = Images.Mariage,
                enabled = state.isMarried
            )
            StateItem(
                name = "Діти",
                imageVector = Images.Baby,
                enabled = state.babies > 0,
                count = state.babies,
                price = state.babies * state.config.babyCost
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StateItem(
                name = "Авто",
                imageVector = Images.Car,
                enabled = state.cars > 0,
                count = state.cars,
                price = state.cars * state.config.carCost
            )
            StateItem(
                name = "Квартира",
                imageVector = Images.Flat,
                enabled = state.apartment > 0,
                count = state.apartment,
                price = state.apartment * state.config.apartmentCost
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StateItem(
                name = "Маєток",
                imageVector = Images.Estate,
                enabled = state.cottage > 0,
                count = state.cottage,
                price = state.cottage * state.config.cottageCost
            )
            StateItem(
                name = "Яхта",
                imageVector = Images.Yacht,
                enabled = state.yacht > 0,
                count = state.yacht,
                price = state.yacht * state.config.yachtCost
            )
            StateItem(
                name = "Літак",
                imageVector = Images.Fly,
                enabled = state.flight > 0,
                count = state.flight,
                price = state.flight * state.config.flightCost
            )
        }
        DetailsField(
            "Активний прибуток",
            state.activeProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Пасивний прибуток",
            state.passiveProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Загальний прибуток",
            state.totalProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Витрати по кредиту",
            state.creditExpenses().toString(),
            MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Загальні витрати",
            state.totalExpenses(player).toString(),
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