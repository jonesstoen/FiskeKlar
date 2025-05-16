package no.uio.ifi.in2000.team46.presentation.map.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.maps.MapView

// summary: creates and remembers a mapview integrated with android lifecycle in compose

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    // get current compose context
    val context = LocalContext.current
    // remember mapview instance across recompositions
    val mapView = remember { MapView(context) }
    // get lifecycle from local lifecycle owner
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // connect mapview lifecycle events to android lifecycle
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        // add observer to lifecycle to handle mapview callbacks
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    // return configured mapview for ui
    return mapView
}
