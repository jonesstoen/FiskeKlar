package no.uio.ifi.in2000.team46.presentation.map.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team46.data.remote.geocoding.Feature
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MapControls(
    map: MapLibreMap,
    mapViewModel: MapViewModel,
    searchViewModel: SearchViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel,
    forbudViewModel: ForbudViewModel,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    navController: NavController,
    onSearchResultSelected: (Feature) -> Unit,
    onUserLocationSelected: (android.location.Location) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val weatherService = remember { WeatherService() }
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
                forbudViewModel = forbudViewModel
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
                temperature = mapViewModel.temperature.collectAsState().value,
                symbolCode = mapViewModel.weatherSymbol.collectAsState().value,
                onWeatherClick = {
                    coroutineScope.launch {
                        val target = map.cameraPosition.target
                        if (target != null) {
                            // Først oppdater været og stedsnavnet
                            mapViewModel.updateWeatherForLocation(target.latitude, target.longitude)
                            // Vent litt for å sikre at stedsnavnet er oppdatert
                            delay(100)
                            val weatherDetails = weatherService.getWeatherDetails(
                                target.latitude,
                                target.longitude
                            )
                            if (weatherDetails != null && 
                                weatherDetails.temperature != null && 
                                weatherDetails.feelsLike != null && 
                                weatherDetails.highTemp != null && 
                                weatherDetails.lowTemp != null && 
                                weatherDetails.symbolCode != null && 
                                weatherDetails.description != null) {
                                val currentLocationName = mapViewModel.locationName.value
                                android.util.Log.d("WeatherDebug", "Sending location name: $currentLocationName")
                                val encodedLocationName = URLEncoder.encode(currentLocationName, StandardCharsets.UTF_8.toString())
                                android.util.Log.d("WeatherDebug", "Encoded location name: $encodedLocationName")
                                navController.navigate(
                                    "weather_detail/${weatherDetails.temperature}/${weatherDetails.feelsLike}/" +
                                    "${weatherDetails.highTemp}/${weatherDetails.lowTemp}/${weatherDetails.symbolCode}/" +
                                    "${weatherDetails.description}/${encodedLocationName}"
                                )
                            }
                        }
                    }
                }
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



