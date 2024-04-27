package ua.vald_zx.game.rat.race.card.libgdx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

class LibgdxLayerFragment : AndroidFragmentApplication() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initializeForView(Main())
}