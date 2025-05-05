package no.uio.ifi.in2000.team46.presentation.map.metalerts


import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.maps.Style

object MetAlertsAnimator {
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    fun start(style: Style) {
        val layer = style.getLayerAs<SymbolLayer>("metalerts-icons") ?: return

        // Unngå dobbeltstart
        if (handler != null) return

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val animator = ValueAnimator.ofFloat(0.3f, 0.45f, 0.3f)
                animator.duration = 1000L
                animator.addUpdateListener { valueAnimator ->
                    val size = valueAnimator.animatedValue as Float
                    layer.setProperties(iconSize(size))
                }
                animator.start()

                handler?.postDelayed(this, 5000L)
            }
        }

        handler?.post(runnable!!)
    }

    fun stop() {
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null
    }
}
