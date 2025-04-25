package no.uio.ifi.in2000.team46.presentation.map.ui.screens

import android.Manifest
import android.util.Log
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
import kotlinx.coroutines.flow.distinctUntilChanged
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
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.remote.grib.GribRetrofitInstance
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.CurrentViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
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
    // Expand/hide bottom sheet on MetAlert selection
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()
    val bottomSheetState by mapViewModel.bottomSheetState.collectAsState()

    // Bottom sheet state
    val sheetState = rememberStandardBottomSheetState(
        initialValue = bottomSheetState,
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





    // Expand/hide bottom sheet when MetAlert is selected
    LaunchedEffect(selectedFeature) {
        if (selectedFeature != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

    // Observe bottom sheet state and reset MetAlert when it's fully hidden
    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .distinctUntilChanged()
            .collect { state ->
                Log.d("MapScreen", "Bottom sheet state: $state")

                // Reset MetAlert only when the bottom sheet is fully hidden
                if (state != SheetValue.Expanded) {
                    metAlertsViewModel.selectFeature(null)  // Reset the selected MetAlert
                    Log.d("MapScreen", "Selected alert cleared because bottom sheet is Hidden.")
                }
            }
    }
    val gribViewModel: GribViewModel = viewModel(
        factory = GribViewModelFactory(
            GribRepository(
                GribRetrofitInstance.GribApi,
                ctx
            )
        )
    )
    val currentViewModel: CurrentViewModel = viewModel(
        factory = CurrentViewModelFactory(
            CurrentRepository(
                api = GribRetrofitInstance.GribApi,
                context = ctx
            )
        )
    )

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
                    map        = map,
                    mapView    = mapView,
                    aisViewModel       = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel    = forbudViewModel,
                    gribViewModel      = gribViewModel,
                    currentViewModel   = currentViewModel
                )
            }
            // 2.5) Wind layer


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
                    gribViewModel         = gribViewModel,
                    currentViewModel = currentViewModel,
                    onRequestPermission   = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
            }
        }
    }
}
