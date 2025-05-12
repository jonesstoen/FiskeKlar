package no.uio.ifi.in2000.team46.presentation.map.components.controls

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
import no.uio.ifi.in2000.team46.presentation.map.components.WeatherDisplay
import no.uio.ifi.in2000.team46.presentation.map.components.layermenu.LayerFilterButton

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
    precipitationViewModel: PrecipitationViewModel,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    isLayerMenuExpanded: Boolean,
    onLayerMenuExpandedChange: (Boolean) -> Unit,
    navController: NavController,
    onSearchResultSelected: (Feature) -> Unit,
    onShowWindSliders: () -> Unit,
    onShowCurrentSliders: () -> Unit,
    onShowWaveSliders: () -> Unit,
    onUserLocationSelected: (Location) -> Unit,
    // Ny parameter for å lagre element-posisjoner for onboarding
    elementBounds: MutableMap<String, androidx.compose.ui.geometry.Rect>? = null
) {
    val context = LocalContext.current
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    // update weather whenever camera moves
    LaunchedEffect(map.cameraPosition.target) {
        map.cameraPosition.target?.let {
            mapViewModel.updateWeatherForLocation(it.latitude, it.longitude)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // search input or button in top-left
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            if (isSearchExpanded) {
                SearchBox(
                    modifier = Modifier,
                    map = map,
                    searchResults = searchViewModel.searchResults.collectAsState().value,
                    isSearching = searchViewModel.isSearching.collectAsState().value,
                    onSearch = { query ->
                        map.cameraPosition.target?.let {
                            searchViewModel.search(query, focusLat = it.latitude, focusLon = it.longitude)
                        }
                    },
                    onResultSelected = { feature ->
                        val coords = feature.geometry.coordinates
                        if (coords.size >= 2) {
                            mapViewModel.zoomToLocation(map, coords[1], coords[0], zoom = 15.0)
                            searchViewModel.clearResults()
                            onSearchResultSelected(feature)
                            isSearchExpanded = false
                            map.uiSettings.setAllGesturesEnabled(true)
                        }
                    },
                    isShowingHistory = true,
                    onDismissRequest = {
                        isSearchExpanded = false
                        map.uiSettings.setAllGesturesEnabled(true)
                    }
                )
            } else {
                IconButton(
                    onClick = { isSearchExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        // Lagre posisjon for søkeknappen for onboarding
                        .onGloballyPositioned { coordinates ->
                            elementBounds?.put("search_field", coordinates.boundsInRoot())
                        }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Open Search")
                }
            }
        }

        // zoom and layer buttons in bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                ZoomButton(
                    onZoomIn = { mapViewModel.zoomIn(map) },
                    onZoomOut = { mapViewModel.zoomOut(map) },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        elementBounds?.put("zoom_buttons", coordinates.boundsInRoot())
                    }
                )
                LayerFilterButton(
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    gribViewModel = gribViewModel,
                    currentViewModel = currentViewModel,
                    driftViewModel = driftViewModel,
                    waveViewModel = waveViewModel,
                    precipitationViewModel = precipitationViewModel,
                    isExpanded = isLayerMenuExpanded,
                    onExpandedChange = onLayerMenuExpandedChange,
                    onShowWindSliders = {
                        // hide menu before opening wind sliders
                        onLayerMenuExpandedChange(false)
                        onShowWindSliders()
                    },
                    onShowWaveSliders = {
                        onLayerMenuExpandedChange(false)
                        onShowWaveSliders()
                    },
                    onShowCurrentSliders = onShowCurrentSliders,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        elementBounds?.put("filter_button", coordinates.boundsInRoot())
                    }
                )
            }
        }

        // weather + location button in bottom-right
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
                weatherService = remember { WeatherService() },
                modifier = Modifier
                    .padding(4.dp)
                    .onGloballyPositioned { coordinates ->
                        elementBounds?.put("weather_panel", coordinates.boundsInRoot())
                    }
            )
            zoomToLocationButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        elementBounds?.put("location_button", coordinates.boundsInRoot())
                    }
            ) {
                if (hasLocationPermission) {
                    mapViewModel.userLocation.value?.let { location ->
                        mapViewModel.clearSelectedLocation()
                        mapViewModel.zoomToUserLocation(map, context)
                        onUserLocationSelected(location)
                    }
                } else {
                    onRequestPermission()
                }
            }
        }
    }
}




