package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.PlaceType

/**
 * Знак клітинки. У комірку 15dp слово не влазить за жодного кегля, тому тип
 * несе піктограма. SVG лежать у composeResources як є — вони на
 * `fill="currentColor"`, тож перефарбовуються тоном комірки.
 */
@Composable
fun PlaceType.icon(): Painter = painterResource(
    when (this) {
        PlaceType.Salary -> Res.drawable.cell_salary
        PlaceType.Start -> Res.drawable.cell_start
        PlaceType.Chance -> Res.drawable.cell_chance
        PlaceType.Store -> Res.drawable.cell_store
        PlaceType.Shopping -> Res.drawable.cell_shopping
        PlaceType.Expenses -> Res.drawable.cell_expenses
        PlaceType.Business -> Res.drawable.cell_business
        PlaceType.BigBusiness -> Res.drawable.cell_big_business
        PlaceType.Deputy -> Res.drawable.cell_deputy
        PlaceType.Rest -> Res.drawable.cell_rest
        PlaceType.Resignation -> Res.drawable.cell_exaltation
        PlaceType.Divorce -> Res.drawable.cell_divorce
        PlaceType.Bankruptcy -> Res.drawable.cell_bankruptcy
        PlaceType.Child -> Res.drawable.cell_child
        PlaceType.Love -> Res.drawable.cell_love
        PlaceType.TaxInspection -> Res.drawable.cell_tax
        is PlaceType.Desire -> Res.drawable.cell_dream
    }
)

/**
 * Знак колоди. Та сама мова, що й у клітинках: колода й поле одного роду
 * несуть однакову піктограму, три рівні бізнесу різняться своїм знаком.
 */
@Composable
fun BoardCardType.icon(): Painter = painterResource(
    when (this) {
        BoardCardType.Chance -> Res.drawable.cell_chance
        BoardCardType.Expenses -> Res.drawable.cell_expenses
        BoardCardType.Shopping -> Res.drawable.cell_shopping
        BoardCardType.EventStore -> Res.drawable.cell_store
        BoardCardType.Deputy -> Res.drawable.cell_deputy
        BoardCardType.SmallBusiness -> Res.drawable.cell_small_business
        BoardCardType.MediumBusiness -> Res.drawable.cell_business
        BoardCardType.BigBusiness -> Res.drawable.cell_big_business
    }
)
