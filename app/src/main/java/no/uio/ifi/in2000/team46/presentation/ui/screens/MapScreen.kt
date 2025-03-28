package no.uio.ifi.in2000.team46.presentation.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.maps.MapView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.map.layers.AisLayer
import no.uio.ifi.in2000.team46.map.layers.MetAlertsLayerComponent
import no.uio.ifi.in2000.team46.map.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.map.utils.addUserLocationIndicator
import no.uio.ifi.in2000.team46.presentation.ui.components.LayerFilterButton
import no.uio.ifi.in2000.team46.presentation.ui.components.metAlerts.MetAlertsDetailsPanel
import no.uio.ifi.in2000.team46.presentation.ui.components.zoomToLocationButton
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel

import org.maplibre.android.maps.MapLibreMap


/** MapScreen er UI-skjermen der kartet vises, og den kobler sammen ViewModel og den visuelle presentasjonen.
 * Dette er en Composable-skjerm som integrerer MapView (fra tradisjonell Android View) i et Jetpack Compose-miljø.
 * Bruk av AndroidView: Den benytter AndroidView for å legge inn et MapView i Compose-layouten.
 * Samarbeid med ViewModel: MapScreen henter et MapViewModel-objekt og kaller funksjonen initializeMap for å sette opp kartet når visningen lastes.
 * Dette knytter sammen UI og logikk slik at eventuelle endringer i kartets tilstand kan observeres og reflekteres i brukergrensesnittet.
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    granted: Boolean,
    locationRepository: LocationRepository,
    mapViewModel: MapViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel = viewModel()
) {
    //we need to remember the state of the map, so that it doesn't get reinitialized on recomposition
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    //we need to initialize the map only once, so that it doesn't get reinitialized on recomposition
    var isMapInitialized by remember { mutableStateOf(false) }
    //remember the mapView with lifecycle to handle the lifecycle of the mapView
    val mapView = rememberMapViewWithLifecycle()
    //get the context of the current activity
    val context = LocalContext.current

    //get the selected metalert  from the MetAlertsViewModel
    val selectedMetAlert by metAlertsViewModel.selectedFeature.collectAsState()


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),

        ) { view: MapView ->

            if (!isMapInitialized) {
                view.getMapAsync { map ->
                    mapViewModel.initializeMap(map, context)
                    mapLibreMap = map
                    isMapInitialized = true
                }
            }
        }
        zoomToLocationButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            mapLibreMap?.let { map ->
                if (granted) {
                    mapViewModel.zoomToUserLocation(map, context)

                } else {
                    mapViewModel.zoomToLocation(map, 63.4449834, 10.9124688, 15.0)
                }
            }
        }
        MetAlertsLayerComponent(
            metAlertsViewModel = metAlertsViewModel,
            mapView = mapView
        )
        AisLayer(
            mapView = mapView,
            aisViewModel = aisViewModel
        )
        LayerFilterButton(
            aisViewModel = aisViewModel,
            metAlertsViewModel = metAlertsViewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 16.dp)
        )
        MetAlertsDetailsPanel(
            selectedMetAlert = selectedMetAlert,
            metAlertsViewModel = metAlertsViewModel,
            modifier = Modifier.align(Alignment.TopEnd)
        )


    }
}