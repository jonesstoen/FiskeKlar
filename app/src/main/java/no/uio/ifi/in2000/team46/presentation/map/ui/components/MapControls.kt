package no.uio.ifi.in2000.team46.presentation.map.ui.components

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.data.remote.geocoding.Feature
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel

import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import no.uio.ifi.in2000.team46.presentation.grib.WaveViewModel

@Composable
fun MapControls(
    map: MapLibreMap,
    mapViewModel: MapViewModel,
    searchViewModel: SearchViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    navController: NavController,
    onSearchResultSelected: (Feature) -> Unit,
    onUserLocationSelected: (Location) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val weatherService = remember { WeatherService() }
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()
    val locationName = mapViewModel.locationName.collectAsState().value

    // Oppdater været når markøren beveger seg
    LaunchedEffect(map.cameraPosition.target) {
        val target = map.cameraPosition.target
        if (target != null) {
            mapViewModel.updateWeatherForLocation(target.latitude, target.longitude)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // 1) Søkeboks øverst til venstre
        SearchBox(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            map = map,
            searchResults = searchViewModel.searchResults.collectAsState().value,
            isSearching = searchViewModel.isSearching.collectAsState().value,
            onSearch = { query ->
                val target = map.cameraPosition.target
                if (target != null) {
                    searchViewModel.search(query, focusLat = target.latitude, focusLon = target.longitude)
                }
            },
            onResultSelected = { feature ->
                val coords = feature.geometry.coordinates
                if (coords.size >= 2) {
                    mapViewModel.zoomToLocation(map, coords[1], coords[0], zoom = 15.0)
                    searchViewModel.clearResults()
                    onSearchResultSelected(feature)
                }
            }
        )

        // 2) Zoom + filter i kolonne nederst til venstre
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ZoomButton(
                onZoomIn = { mapViewModel.zoomIn(map) },
                onZoomOut = { mapViewModel.zoomOut(map) }
            )
            LayerFilterButton(
                aisViewModel = aisViewModel,
                metAlertsViewModel = metAlertsViewModel,
                forbudViewModel    = forbudViewModel,
                gribViewModel = gribViewModel,
                currentViewModel =  currentViewModel,
                driftViewModel = driftViewModel,
                waveViewModel = waveViewModel,
            )

        }

        // 3) Vær‐display + posisjonsknapp nederst til høyre
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherDisplay(
                temperature = temperature,
                symbolCode = weatherSymbol,
                mapViewModel = mapViewModel,
                navController = navController,
                weatherService = weatherService,
                modifier = Modifier.padding(4.dp)
            )
            zoomToLocationButton {
                if (hasLocationPermission) {
                    val location = mapViewModel.userLocation.value
                    if (location != null) {
                        onUserLocationSelected(location)
                        mapViewModel.zoomToUserLocation(map, context)
                    }
                } else {
                    onRequestPermission()
                }
            }
        }
    }
}



