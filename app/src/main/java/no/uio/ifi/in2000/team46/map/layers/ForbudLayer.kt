package no.uio.ifi.in2000.team46.map.layers

import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun ForbudLayer(
    mapView: MapView,
    viewModel: ForbudViewModel
) {
    val geoJson by viewModel.geoJson.collectAsState()
    val isVisible by viewModel.isLayerVisible.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val TAG = "ForbudLayerComponent"
    val coroutineScope = rememberCoroutineScope()

    // Oppdater lag når data eller synlighet endres
    LaunchedEffect(geoJson, isVisible) {
        if (isVisible && geoJson != null) {
            updateForbudLayer(mapView, geoJson!!)
        } else {
            removeForbudLayer(mapView)
        }
    }

    // Logg feil
    LaunchedEffect(error) {
        error?.let {
            Log.e(TAG, "Feil: $it")
        }
    }

    // logg lastestatus
    LaunchedEffect(isLoading) {
        if (isLoading) {
            Log.d(TAG, "Laster forbudsområder...")
        }
    }
}

private fun updateForbudLayer(mapView: MapView, geoJson: String) {
    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                // Fjern gamle lag hvis de finnes
                style.getLayer("forbud-layer")?.let { style.removeLayer(it) }
                style.getSource("forbud-source")?.let { style.removeSource(it) }

                // Opprett ny GeoJSON-kilde
                val source = GeoJsonSource("forbud-source", geoJson)
                style.addSource(source)

                // Tegn
                val fillLayer = FillLayer("forbud-layer", "forbud-source").withProperties(
                    PropertyFactory.fillColor(Color.RED),
                    PropertyFactory.fillOpacity(0.5f)
                )
                style.addLayer(fillLayer)

                Log.d("ForbudLayer", "Forbudsområder lagt til i kart")

            } catch (e: Exception) {
                Log.e("ForbudLayer", "Feil ved visning av forbudsområder", e)
            }
        }
    }
}

private fun removeForbudLayer(mapView: MapView) {
    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                style.getLayer("forbud-layer")?.let { style.removeLayer(it) }
                style.getSource("forbud-source")?.let { style.removeSource(it) }
                Log.d("ForbudLayer", "Forbudslag fjernet")
            } catch (e: Exception) {
                Log.e("ForbudLayer", "Feil ved fjerning av forbudslag", e)
            }
        }
    }
}
