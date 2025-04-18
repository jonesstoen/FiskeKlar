package no.uio.ifi.in2000.team46.presentation.ui.components.MapScreen

import android.Manifest
import android.content.Context
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.map.utils.addUserLocationIndicator
import no.uio.ifi.in2000.team46.presentation.ui.components.metAlerts.MetAlertsBottomSheetContent
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapUiEvent
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModelFactory
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.search.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository

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
    searchViewModel: SearchViewModel = viewModel()
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

    // Update user‐location indicator whenever location changes
    val userLocation by mapViewModel.userLocation.collectAsState()
    LaunchedEffect(mapLibreMap, userLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val loc = userLocation  ?: return@LaunchedEffect
        map.getStyle { style ->
            addUserLocationIndicator(map, style, loc.latitude, loc.longitude)
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
                    onRequestPermission   = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
            }
        }
    }
}
