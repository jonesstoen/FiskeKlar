package no.uio.ifi.in2000.team46.map.layers

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.grib.WindDataViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun WindLayerComponent(
    windDataViewModel: WindDataViewModel,
    mapView: MapView
) {
    val context = LocalContext.current
    val windDataState = windDataViewModel.windDataState.collectAsState()

    LaunchedEffect(windDataState.value) {
        windDataState.value?.let { result ->
            when (result) {
                is no.uio.ifi.in2000.team46.data.repository.Result.Success -> {
                    // Here you can add logic to overlay wind data on your MapLibre map.
                    // In this example, a Toast is shown indicating the wind data was received.
                    Toast.makeText(
                        context,
                        "Wind data received: ${result.data.rawData.length} characters",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is no.uio.ifi.in2000.team46.data.repository.Result.Error -> {
                    Toast.makeText(context, "Error fetching wind data", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}