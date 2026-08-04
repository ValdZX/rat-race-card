package ua.vald_zx.game.rat.race.card.logic

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerSpeechLogTest {

    private val expiry = 1_800_000_000_000

    @Test
    fun theSameSpeechDeliveredTwiceIsLoggedOnce() {
        val once = emptyList<PlayerMessage>().plusSpeech("Привіт", expiry)
        val twice = once.plusSpeech("Привіт", expiry)

        assertEquals(listOf("Привіт"), twice.map { it.text })
    }

    @Test
    fun theSameSpeechRepeatedByEveryPlayerUpdateIsLoggedOnce() {
        var log = emptyList<PlayerMessage>()
        repeat(5) { log = log.plusSpeech("Привіт", expiry) }

        assertEquals(1, log.size, "кожне оновлення гравця додавало ту саму репліку: $log")
    }

    @Test
    fun aNewSpeechWithTheSameTextIsLoggedAgain() {
        val log = emptyList<PlayerMessage>()
            .plusSpeech("Привіт", expiry)
            .plusSpeech("Привіт", expiry + 8_000)

        assertEquals(listOf("Привіт", "Привіт"), log.map { it.text })
    }

    @Test
    fun theLogKeepsOnlyTheLastThree() {
        var log = emptyList<PlayerMessage>()
        (1..5).forEach { index -> log = log.plusSpeech("текст $index", expiry + index) }

        assertEquals(listOf("текст 3", "текст 4", "текст 5"), log.map { it.text })
    }

    @Test
    fun blankSpeechIsIgnored() {
        assertEquals(emptyList(), emptyList<PlayerMessage>().plusSpeech("   ", expiry))
    }
}
