package no.uio.ifi.in2000.team46.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.map.layers.AisLayer
import no.uio.ifi.in2000.team46.map.layers.MetAlertsLayerComponent
import no.uio.ifi.in2000.team46.map.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.ui.components.BottomNavBar
import no.uio.ifi.in2000.team46.presentation.ui.components.LayerFilterButton
import no.uio.ifi.in2000.team46.presentation.ui.components.SearchBox
import no.uio.ifi.in2000.team46.presentation.ui.components.ZoomButton
import no.uio.ifi.in2000.team46.presentation.ui.components.metAlerts.MetAlertsBottomSheetContent
import no.uio.ifi.in2000.team46.presentation.ui.components.weather.WeatherDisplay
import no.uio.ifi.in2000.team46.presentation.ui.components.zoomToLocationButton
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.search.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import org.maplibre.android.maps.MapLibreMap

/** MapScreen er UI-skjermen der kartet vises, og den kobler sammen ViewModel og den visuelle presentasjonen.
 * Dette er en Composable-skjerm som integrerer MapView (fra tradisjonell Android View) i et Jetpack Compose-miljø.
 * Bruk av AndroidView: Den benytter AndroidView for å legge inn et MapView i Compose-layouten.
 * Samarbeid med ViewModel: MapScreen henter et MapViewModel-objekt og kaller funksjonen initializeMap for å sette opp kartet når visningen lastes.
 * Dette knytter sammen UI og logikk slik at eventuelle endringer i kartets tilstand kan observeres og reflekteres i brukergrensesnittet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    granted: Boolean,
    locationRepository: LocationRepository,
    mapViewModel: MapViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val isShowingHistory by searchViewModel.showingHistory.collectAsState()


// Oppdater brukerens posisjon hvert 10. sekund
    LaunchedEffect(Unit) {
        while (true) {
            if (granted) {
                mapViewModel.fetchUserLocation(context)
            }
            delay(10000) // 10 sekunder
        }
    }

    // Cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    val selectedMetAlert by metAlertsViewModel.selectedFeature.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    LaunchedEffect(selectedMetAlert) {
        if (selectedMetAlert != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "map",
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                selectedMetAlert?.let {
                    MetAlertsBottomSheetContent(
                        feature = it,
                        onClose = { metAlertsViewModel.selectFeature(null) }
                    )
                }
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Map View
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { view ->
                    if (!isMapInitialized) {
                        view.getMapAsync { map ->
                            mapViewModel.initializeMap(map, context)
                            mapLibreMap = map
                            isMapInitialized = true
                        }
                    }
                }

                // Layers
                MetAlertsLayerComponent(metAlertsViewModel, mapView)
                AisLayer(mapView, aisViewModel)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, end = 100.dp) // Gir rom til WeatherDisplay
                ) {
                    // Search Box - Top Left
                    mapLibreMap?.let { map ->
                        val userLocation by mapViewModel.userLocation.collectAsState()
                        SearchBox(
                            map = map,
                            searchResults = searchResults,
                            isSearching = isSearching,
                            isShowingHistory = isShowingHistory,
                            onSearch = { query ->
                                val focusLat = userLocation?.latitude ?: map.cameraPosition.target?.latitude
                                val focusLon = userLocation?.longitude ?: map.cameraPosition.target?.longitude
                                searchViewModel.search(
                                    query = query,
                                    focusLat = focusLat,
                                    focusLon = focusLon
                                )
                            },
                            onResultSelected = { feature ->
                                searchViewModel.addToHistory(feature)
                                val coordinates = feature.geometry.coordinates
                                if (coordinates.size >= 2) {
                                    mapViewModel.zoomToLocation(
                                        map,
                                        coordinates[1], // latitude
                                        coordinates[0], // longitude
                                        15.0
                                    )
                                    searchViewModel.clearResults()
                                }
                            },
                        )
                    }
                }

                // Weather Display - Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    WeatherDisplay(
                        temperature = temperature,
                        symbolCode = weatherSymbol,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                // Bottom Left Controls Group
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Layer Filter Button
                    LayerFilterButton(
                        aisViewModel,
                        metAlertsViewModel
                    )

                    // Zoom Controls
                    ZoomButton(
                        onZoomIn = {
                            mapLibreMap?.let { map ->
                                mapViewModel.zoomIn(map)
                            }
                        },
                        onZoomOut = {
                            mapLibreMap?.let { map ->
                                mapViewModel.zoomOut(map)
                            }
                        }
                    )
                }

                // Location Button - Bottom Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    zoomToLocationButton {
                        mapLibreMap?.let { map ->
                            if (granted) {
                                mapViewModel.zoomToUserLocation(map, context)
                            } else {
                                mapViewModel.zoomToLocation(map, 63.4449834, 10.9124688, 15.0)
                            }
                        }
                    }
                }
            }
        }
    }
}
