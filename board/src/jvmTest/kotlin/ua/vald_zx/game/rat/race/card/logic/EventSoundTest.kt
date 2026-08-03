package ua.vald_zx.game.rat.race.card.logic

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EventSoundTest {

    private fun player(position: Int, id: String = "me") = Player(
        id = id,
        boardId = "b",
        attrs = PlayerAttributes(0, 0),
        location = PlayerLocation(position = position, level = 0),
    )

    private fun board(taken: CardLink? = null) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        takenCard = taken,
    )

    @Test
    fun standingStillMakesNoSound() {
        val same = player(4)
        assertNull(moveSound(old = same, changed = same))
    }

    @Test
    fun aPlayerAppearingForTheFirstTimeIsSilent() {
        assertNull(moveSound(old = null, changed = player(4)))
    }

    @Test
    fun everyTokenMoveKnocksTheSameWay() {
        assertEquals(
            GameSound.TokenStep,
            moveSound(old = player(4, "rival"), changed = player(9, "rival")),
            "чужий хід має бути чутно, інакше стіл здається порожнім",
        )
        assertEquals(
            GameSound.TokenStep,
            moveSound(old = player(4), changed = player(9)),
        )
    }

    @Test
    fun takingACardFromTheDeckSoundsOnce() {
        val empty = board()
        val taken = board(CardLink(BoardCardType.Chance, 1))

        assertEquals(GameSound.PlaceCard, cardTakenSound(empty, taken))
        assertNull(cardTakenSound(taken, taken), "звук колоди повторився без нової карти")
        assertNull(cardTakenSound(taken, empty), "карта пішла у відбій — це не взяття")
    }
}
