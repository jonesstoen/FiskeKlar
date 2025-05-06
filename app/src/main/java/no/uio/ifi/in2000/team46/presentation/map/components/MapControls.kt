package no.uio.ifi.in2000.team46.presentation.map.components

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.data.remote.api.Feature
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel


import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel

@Composable
fun MapControls(
    map: MapLibreMap,
    mapViewModel: MapViewModel,
    searchViewModel: SearchViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel,
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

        LayerFilterButton(
            aisViewModel,
            metAlertsViewModel,
            gribViewModel,
            currentViewModel,
            driftViewModel,
            waveViewModel,
            precipitationViewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )

        // 2b) Zoom-knapper plassert til høyre for filter-knappen
        val fabSize = 56.dp  // FloatingActionButton default size
        val spacing = 8.dp
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp + fabSize + spacing, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zoom inn
            Surface(
                shape = CircleShape,
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                IconButton(onClick = { mapViewModel.zoomIn(map) }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
                }
            }
            // Zoom ut
            Surface(
                shape = CircleShape,
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                IconButton(onClick = { mapViewModel.zoomOut(map) }) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
                }
            }
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
                        mapViewModel.setSelectedLocation(location.latitude, location.longitude)
                        mapViewModel.updateWeatherForLocation(location.latitude, location.longitude)
                        mapViewModel.zoomToUserLocation(map, context)
                    }
                } else {
                    onRequestPermission()
                }
            }
        }
    }
}


