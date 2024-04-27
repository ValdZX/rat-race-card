package ua.vald_zx.game.rat.race.card.libgdx

import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ua.vald_zx.game.rat.race.card.libgdx.BulletTest


class Main : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(BulletTest())
        setScreen<BulletTest>()
    }
}
