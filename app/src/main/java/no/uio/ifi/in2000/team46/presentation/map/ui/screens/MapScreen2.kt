package no.uio.ifi.in2000.team46.presentation.map.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.presentation.map.utils.addUserLocationIndicator
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsBottomSheetContent
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapUiEvent
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModelFactory
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MapControls
import no.uio.ifi.in2000.team46.presentation.map.ui.components.layers.MapLayers
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MapViewContainer
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.data.remote.geocoding.Feature
import no.uio.ifi.in2000.team46.presentation.map.utils.addMapMarker
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MapConstants
import no.uio.ifi.in2000.team46.presentation.map.ui.components.WeatherDisplay
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen2(
    mapView: MapView,
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            LocationRepository(LocalContext.current),
            MetAlertsRepository()
        )
    ),
    aisViewModel: AisViewModel = viewModel(),
    metAlertsViewModel: MetAlertsViewModel = viewModel(
        factory = MetAlertsViewModelFactory(MetAlertsRepository())
    ),
    forbudViewModel: ForbudViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    navController: NavController
) {
    val ctx = LocalContext.current

    // Request location permission
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom sheet state — allow Hidden initial value by disabling skipHiddenState
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
        snackbarHostState = snackbarHostState
    )

    // Hold reference to MapLibreMap
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    
    // Track whether user is currently dragging the map
    var isUserDragging by remember { mutableStateOf(false) }

    // Update user‐location indicator whenever location changes
    val userLocation by mapViewModel.userLocation.collectAsState()
    val selectedLocation by mapViewModel.selectedLocation.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val selectedSearchResult = remember { mutableStateOf<Feature?>(null) }
    
    // Hent temperatur og værsymbol fra mapViewModel
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()
    

    // Oppdater markørene når kartet er klart
    LaunchedEffect(mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style ->
            // Legg kun til brukerens posisjonsmarkør hvis vi har en faktisk posisjon
            userLocation?.let { location ->
                addUserLocationIndicator(map, style, location.latitude, location.longitude)
                // Oppdater vær basert på brukerens posisjon ved oppstart
                if (!mapViewModel.isLocationExplicitlySelected()) {
                    mapViewModel.updateTemperature(location.latitude, location.longitude)
                }
            }
        }
    }
    
    // Oppdater markørene når lokasjonen endres
    LaunchedEffect(mapLibreMap, userLocation, selectedLocation, isUserDragging, selectedSearchResult.value) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isUserDragging) {  // Only update markers if user is not dragging
            map.getStyle { style ->
                // Oppdater brukerens posisjonsmarkør
                userLocation?.let { loc ->
                    addUserLocationIndicator(map, style, loc.latitude, loc.longitude)
                }
                
                // Oppdater valgt posisjonsmarkør KUN hvis en er valgt bevisst
                when {
                    selectedSearchResult.value != null -> {
                        val result = selectedSearchResult.value!!
                        val coordinates = result.geometry.coordinates
                        if (coordinates.size >= 2) {
                            val longitude = coordinates[0]
                            val latitude = coordinates[1]
                            addMapMarker(map, style, latitude, longitude, ctx)
                            mapViewModel.setSelectedLocation(latitude, longitude)
                            mapViewModel.updateWeatherForLocation(latitude, longitude)
                        }
                    }
                    selectedLocation != null && mapViewModel.isLocationExplicitlySelected() -> {
                        addMapMarker(map, style, selectedLocation!!.first, selectedLocation!!.second, ctx)
                    }
                }
            }
        }
    }

    // Show snackbar events from ViewModel
    LaunchedEffect(mapViewModel.uiEvents) {
        mapViewModel.uiEvents.collect { event ->
            if (event is MapUiEvent.ShowAlertSnackbar) {
                snackbarHostState.showSnackbar(event.message, actionLabel = "Vis mer")
            }
        }
    }

    // Expand/hide bottom sheet on MetAlert selection
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()
    LaunchedEffect(selectedFeature) {
        if (selectedFeature != null) scaffoldState.bottomSheetState.expand()
        else scaffoldState.bottomSheetState.hide()
    }

    // BottomSheetScaffold wraps the map + layers + controls
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            selectedFeature?.let { feature ->
                MetAlertsBottomSheetContent(
                    feature = feature,
                    onClose = { metAlertsViewModel.selectFeature(null) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { sheetPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(sheetPadding)
        ) {
            // 1) Map container
            MapViewContainer(
                mapView  = mapView,
                styleUrl = mapViewModel.styleUrl,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map, _ ->
                    mapLibreMap = map
                    mapViewModel.initializeMap(map, ctx)
                    
                    // Add listeners to detect when user is dragging the map
                    map.addOnCameraMoveStartedListener { reason ->
                        if (reason == org.maplibre.android.maps.MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                            isUserDragging = true
                        }
                        true
                    }
                    
                    map.addOnCameraIdleListener {
                        isUserDragging = false
                        true
                    }

                    // Add click listener for the map
                    map.addOnMapClickListener { point ->
                        if (!isUserDragging) {
                            // Konverter klikk-koordinater til skjermkoordinater
                            val screenPoint = map.projection.toScreenLocation(point)
                            
                            // Sjekk features på klikk-posisjonen for hvert lag separat
                            val aisFeatures = map.queryRenderedFeatures(screenPoint, "ais-vessels-layer")
                            val metAlertFeatures = map.queryRenderedFeatures(screenPoint, "metalerts-layer")
                            val forbudFeatures = map.queryRenderedFeatures(screenPoint, "forbud-layer")
                            
                            when {
                                // Hvis vi klikket på en AIS-feature, ikke flytt markøren
                                aisFeatures.isNotEmpty() -> false
                                
                                // Hvis vi klikket på et farevarsel, ikke flytt markøren
                                metAlertFeatures.isNotEmpty() -> false
                                
                                // Hvis vi klikket på et forbudsområde, ikke flytt markøren
                                forbudFeatures.isNotEmpty() -> false
                                
                                // Hvis vi ikke klikket på noe spesielt, flytt markøren
                                else -> {
                                    selectedSearchResult.value = null  // Nullstill søkeresultatet
                                    mapViewModel.setSelectedLocation(point.latitude, point.longitude)
                                    // Oppdater markøren umiddelbart
                                    map.getStyle { style ->
                                        addMapMarker(map, style, point.latitude, point.longitude, ctx)
                                    }
                                    mapViewModel.zoomToLocation(map, point.latitude, point.longitude, map.cameraPosition.zoom)
                                    true
                                }
                            }
                        } else {
                            false
                        }
                    }
                }
            )

            // 2) Layers
            mapLibreMap?.let { map ->
                MapLayers(
                    mapView            = mapView,
                    aisViewModel       = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel    = forbudViewModel
                )
            }

            // 3) Controls
            mapLibreMap?.let { map ->
                MapControls(
                    map                   = map,
                    mapViewModel          = mapViewModel,
                    searchViewModel       = searchViewModel,
                    metAlertsViewModel    = metAlertsViewModel,
                    aisViewModel          = aisViewModel,
                    forbudViewModel       = forbudViewModel,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission   = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    navController         = navController,
                    onSearchResultSelected = { feature ->
                        selectedSearchResult.value = feature
                        map.getStyle { style ->
                            val coordinates = feature.geometry.coordinates
                            if (coordinates.size >= 2) {
                                val longitude = coordinates[0]
                                val latitude = coordinates[1]
                                addMapMarker(map, style, latitude, longitude, ctx)
                                mapViewModel.setSelectedLocation(latitude, longitude)
                                mapViewModel.updateWeatherForLocation(latitude, longitude)
                            }
                        }
                    },
                    onUserLocationSelected = { location ->
                        map.getStyle { style ->
                            addMapMarker(map, style, location.latitude, location.longitude, ctx)
                            mapViewModel.setSelectedLocation(location.latitude, location.longitude)
                            mapViewModel.updateWeatherForLocation(location.latitude, location.longitude)
                        }
                    }
                )
            }

        }
    }
}
