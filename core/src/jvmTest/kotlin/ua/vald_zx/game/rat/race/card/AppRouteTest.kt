package ua.vald_zx.game.rat.race.card

import kotlin.test.Test
import kotlin.test.assertEquals

class AppRouteTest {
    @Test
    fun everyRouteSurvivesAWriteAndRead() {
        listOf(
            AppRoute.Start,
            AppRoute.Card,
            AppRoute.Online,
            AppRoute.Board("2f1c8b90-1111-2222-3333-444455556666"),
        ).forEach { route ->
            assertEquals(route, appRouteOf(route.path()), "маршрут ${route.path()} не пережив round-trip")
        }
    }

    @Test
    fun theHashPrefixIsAccepted() {
        assertEquals(AppRoute.Board("abc"), appRouteOf("#/board/abc"))
        assertEquals(AppRoute.Online, appRouteOf("#/online"))
        assertEquals(AppRoute.Card, appRouteOf("#/card"))
    }

    @Test
    fun anEmptyOrUnknownAddressOpensTheStartScreen() {
        listOf(null, "", "#", "/", "#/", "#/nonsense", "/deep/unknown/path").forEach { raw ->
            assertEquals(AppRoute.Start, appRouteOf(raw), "адреса '$raw' має вести на стартовий екран")
        }
    }

    @Test
    fun aBoardAddressWithoutAnIdFallsBackToTheBoardList() {
        assertEquals(AppRoute.Online, appRouteOf("#/board"))
        assertEquals(AppRoute.Online, appRouteOf("#/board/"))
    }

    @Test
    fun straySlashesDoNotBreakParsing() {
        assertEquals(AppRoute.Board("abc"), appRouteOf("#//board//abc//"))
        assertEquals(AppRoute.Card, appRouteOf("card"))
    }

    @Test
    fun theBoardPathCarriesTheIdVerbatim() {
        assertEquals("/board/xyz-1", AppRoute.Board("xyz-1").path())
    }
}
