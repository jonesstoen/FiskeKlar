package no.uio.ifi.in2000.team46.presentation.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.map.layers.AisLayer
import no.uio.ifi.in2000.team46.map.layers.ForbudLayer
import no.uio.ifi.in2000.team46.map.layers.MetAlertsLayerComponent
import no.uio.ifi.in2000.team46.map.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.ui.components.BottomNavBar
import no.uio.ifi.in2000.team46.presentation.ui.components.LayerFilterButton
import no.uio.ifi.in2000.team46.presentation.ui.components.SearchBox
import no.uio.ifi.in2000.team46.presentation.ui.components.ZoomButton
import no.uio.ifi.in2000.team46.presentation.ui.components.weather.WeatherDisplay
import no.uio.ifi.in2000.team46.presentation.ui.components.metAlerts.MetAlertsBottomSheetContent
import no.uio.ifi.in2000.team46.presentation.ui.components.weather.WeatherDisplay
import no.uio.ifi.in2000.team46.presentation.ui.components.zoomToLocationButton
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.search.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.maps.MapLibreMap
import java.time.LocalDate
import java.time.LocalTime
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.utils.isPointInPolygon


/** MapScreen er UI-skjermen der kartet vises, og den kobler sammen ViewModel og den visuelle presentasjonen.
 * Dette er en Composable-skjerm som integrerer MapView (fra tradisjonell Android View) i et Jetpack Compose-miljø.
 * Bruk av AndroidView: Den benytter AndroidView for å legge inn et MapView i Compose-layouten.
 * Samarbeid med ViewModel: MapScreen henter et MapViewModel-objekt og kaller funksjonen initializeMap for å sette opp kartet når visningen lastes.
 * Dette knytter sammen UI og logikk slik at eventuelle endringer i kartets tilstand kan observeres og reflekteres i brukergrensesnittet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    granted: Boolean,
    locationRepository: LocationRepository,
    mapViewModel: MapViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
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
    var showFishingLog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val fishingLogViewModel: FishingLogViewModel = viewModel { FishingLogViewModel(FishLogRepository(context))}



// Oppdater brukerens posisjon hvert 10. sekund
    LaunchedEffect(Unit) {
        while (true) {
            if (granted) {
                mapViewModel.fetchUserLocation(context)
            }
            delay(5000) // 5 sekunder
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
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()


    //opening the sheet when a warning is selected
    LaunchedEffect(selectedMetAlert) {
        if (selectedMetAlert != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }
    // observing the bottom sheet state with snapshotFlow
    // if the sheet is closed manually (Hidden), the active alert in the ViewModel is reset.
    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { state ->
                Log.d("MapScreen", "Bottom sheet state: $state")
                // Hvis bottom sheet ikke er fullt ekspandert, antas det å være lukket.
                // if the bottom sheet is not fully expanded, it is assumed to be closed.(if it is partially expanded, it is still considered closed)
                if (state != SheetValue.Expanded) {
                    metAlertsViewModel.selectFeature(null)
                    Log.d("MapScreen", "Selected alert cleared because bottom sheet is not Expanded")
                }
            }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
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
                ForbudLayer(mapView, forbudViewModel)

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
                        metAlertsViewModel,
                        forbudViewModel
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
        LaunchedEffect(mapViewModel.userLocation, metAlertsViewModel.metAlertsResponse) {
            delay(5000) // Delay for å sikre at lokasjon er hentet
            val currentLocation = mapViewModel.userLocation.value
            val alertsResponse = metAlertsViewModel.metAlertsResponse.value
            if (currentLocation == null || alertsResponse == null) {
                return@LaunchedEffect
            }
            alertsResponse.features.forEach { feature ->
                if (feature.geometry.type.equals("Polygon", ignoreCase = true)) {
                    val polygon: List<Pair<Double, Double>> = run {
                        val coordinatesRaw = feature.geometry.coordinates as? List<*>
                        val firstRing = coordinatesRaw?.firstOrNull() as? List<*>
                        firstRing?.mapNotNull { item ->
                            val coord = item as? List<*>
                            val lon = coord?.getOrNull(0) as? Double
                            val lat = coord?.getOrNull(1) as? Double
                            if (lon != null && lat != null) Pair(lon, lat) else null
                        } ?: emptyList()
                    }
                    val inside = isPointInPolygon(currentLocation.latitude, currentLocation.longitude, polygon)
                    if (inside) {
                        val alertType = feature.properties.eventAwarenessName
                        val updatedAlertType = alertType.replace("fare", "").trim()
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "ADVARSEL: Du er i et område med utsatt $updatedAlertType farevarsel",
                                actionLabel = "Vis mer "
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                metAlertsViewModel.selectFeature(feature.properties.id)
                            }
                        }
                    }
                }
            }
        }
    }
}
