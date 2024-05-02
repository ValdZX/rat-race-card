package ua.vald_zx.game.rat.race.card.libgdx

import android.graphics.PixelFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

class LibgdxLayerFragment : AndroidFragmentApplication() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = initializeForView(Main()).apply {
        val glView  = (graphics.view as SurfaceView)
        glView.setZOrderOnTop(true)
        glView.holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun initializeForView(
        listener: ApplicationListener?,
        config: AndroidApplicationConfiguration?
    ): View {
        config?.a = 8
        config?.r = 8
        config?.g = 8
        config?.b = 8
        return super.initializeForView(listener, config)
    }
}