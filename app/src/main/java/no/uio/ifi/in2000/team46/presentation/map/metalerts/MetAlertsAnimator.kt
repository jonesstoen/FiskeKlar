package no.uio.ifi.in2000.team46.presentation.map.metalerts


import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.maps.Style

object MetAlertsAnimator {
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    fun start(style: Style) {
        if (handler != null) return

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                try {
                    // Ikke gå videre før stilen er ferdig lastet
                    if (!style.isFullyLoaded) {
                        handler?.postDelayed(this, 1000L)  // Prøv igjen om 1 sekund
                        return
                    }

                    val layer = style.getLayerAs<SymbolLayer>("metalerts-icons")
                    if (layer != null) {
                        val animator = ValueAnimator.ofFloat(0.3f, 0.45f, 0.3f)
                        animator.duration = 1000L
                        animator.addUpdateListener { valueAnimator ->
                            val size = valueAnimator.animatedValue as Float
                            try {
                                layer.setProperties(iconSize(size))
                            } catch (e: Exception) {
                                e.printStackTrace() // Beskytt mot native crash
                            }
                        }
                        animator.start()
                    }
                } catch (e: Exception) {
                    Log.e("MetAlertsAnimator", "Feil i animasjon: ${e.message}")
                }

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


