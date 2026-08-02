package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.AmountTransformation
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions

@Composable
fun DesignNewBoardDialog(
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        transportMovementBonusEnabled: Boolean,
        outerCircleConditions: OuterCircleConditions,
        victoryConditions: VictoryConditions,
    ) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var loanLimit by remember { mutableStateOf("10000") }
    var businessLimit by remember { mutableStateOf("10") }
    var transportBonus by remember { mutableStateOf(true) }
    var minimumCashFlow by remember { mutableStateOf("50000") }
    var minimumAccountBalance by remember { mutableStateOf("200000") }
    var apartmentRequired by remember { mutableStateOf(true) }
    var carRequired by remember { mutableStateOf(true) }
    var victoryAccountBalance by remember { mutableStateOf("10000000") }
    var dreamRequired by remember { mutableStateOf(true) }
    var planeRequired by remember { mutableStateOf(true) }
    var estateRequired by remember { mutableStateOf(true) }

    val amounts = listOf(loanLimit, businessLimit, minimumCashFlow, minimumAccountBalance, victoryAccountBalance)
    val complete = name.isNotBlank() && amounts.none { it.isEmpty() }

    DesignDialog(
        title = stringResource(Res.string.new_table),
        onDismissRequest = onDismiss,
        confirmLabel = stringResource(Res.string.create_table),
        confirmEnabled = complete,
        confirmDisabledReason = stringResource(Res.string.fill_all_fields),
        onConfirm = {
            onCreate(
                name,
                loanLimit.toLong(),
                businessLimit.toLong(),
                transportBonus,
                OuterCircleConditions(
                    minimumCashFlow = minimumCashFlow.toLong(),
                    apartmentRequired = apartmentRequired,
                    carRequired = carRequired,
                    minimumAccountBalance = minimumAccountBalance.toLong(),
                ),
                VictoryConditions(
                    dreamRequired = dreamRequired,
                    planeRequired = planeRequired,
                    estateRequired = estateRequired,
                    minimumAccountBalance = victoryAccountBalance.toLong(),
                ),
            )
        },
        dismissLabel = stringResource(Res.string.cancel),
        onDismissAction = onDismiss,
    ) {
        Column(
            // ponytail: стеля висоти замість вимірювання вікна — форма довша за
            // будь-який телефон, а Dialog не віддає доступну висоту дітям.
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DesignTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.table_name),
            )
            AmountField(stringResource(Res.string.loanLimit), loanLimit) { loanLimit = it }
            AmountField(stringResource(Res.string.businessLimit), businessLimit) { businessLimit = it }
            DesignToggleRow(
                label = stringResource(Res.string.transport_movement_bonus),
                checked = transportBonus,
                onCheckedChange = { transportBonus = it },
            )

            DesignSectionTitle(stringResource(Res.string.outer_circle_conditions))
            AmountField(stringResource(Res.string.minimum_cash_flow), minimumCashFlow) { minimumCashFlow = it }
            AmountField(stringResource(Res.string.minimum_account_balance), minimumAccountBalance) {
                minimumAccountBalance = it
            }
            DesignToggleRow(
                label = stringResource(Res.string.apartment_required),
                checked = apartmentRequired,
                onCheckedChange = { apartmentRequired = it },
            )
            DesignToggleRow(
                label = stringResource(Res.string.car_required),
                checked = carRequired,
                onCheckedChange = { carRequired = it },
            )

            DesignSectionTitle(stringResource(Res.string.victory_conditions))
            AmountField(stringResource(Res.string.victory_account_balance), victoryAccountBalance) {
                victoryAccountBalance = it
            }
            DesignToggleRow(
                label = stringResource(Res.string.dream_required),
                checked = dreamRequired,
                onCheckedChange = { dreamRequired = it },
            )
            DesignToggleRow(
                label = stringResource(Res.string.plane_required),
                checked = planeRequired,
                onCheckedChange = { planeRequired = it },
            )
            DesignToggleRow(
                label = stringResource(Res.string.estate_required),
                checked = estateRequired,
                onCheckedChange = { estateRequired = it },
            )
        }
    }
}

@Composable
private fun AmountField(label: String, value: String, onValueChange: (String) -> Unit) {
    DesignTextField(
        value = value,
        onValueChange = { onValueChange(it.getDigits()) },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = AmountTransformation,
    )
}
