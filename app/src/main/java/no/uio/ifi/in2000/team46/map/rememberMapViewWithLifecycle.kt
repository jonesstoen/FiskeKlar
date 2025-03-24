package no.uio.ifi.in2000.team46.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.maps.MapView

/**rememberMapViewWithLifecycle sørger for at MapView integreres med Compose og håndterer livssyklusendringer.
 *
 * Denne filen inneholder en Composable-funksjon som oppretter og håndterer et MapView-objekt
 * med integrasjon til Androids livssyklus. Dette sikrer korrekt ressursstyring når MapView
 * brukes i en Jetpack Compose-applikasjon.
 *
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    // Henter gjeldende Context fra Compose-miljøet
    val context = LocalContext.current
    // Oppretter og "husker" et MapView-objekt slik at det ikke opprettes på nytt ved recomposition
    val mapView = remember { MapView(context) }
    // Henter den nåværende Lifecycle fra den lokale LifecycleOwner (main activity) i dette tilfellet
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // DisposableEffect kobler MapView til livssyklusen og sørger for at observeren fjernes
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        // Legger til observeren i livssyklusen slik at MapView reagerer på hendelser
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    // Returnerer det konfigurerte MapView-objektet til bruk i UI
    return mapView
}
