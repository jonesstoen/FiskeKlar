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
import androidx.navigation.NavBackStackEntry
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
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

// =====================
// MAP SCREEN
// =====================

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
    navBackStackEntry: NavBackStackEntry,
    initialLocation: Pair<Double, Double>? = null,
    areaPoints: List<Pair<Double, Double>>? = null
) {
    // ----------- State og permissions -----------
    val ctx = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ----------- Snackbar og bottom sheet state -----------
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
        snackbarHostState = snackbarHostState
    )

    // ----------- MapLibreMap referanse -----------
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    // ----------- Brukerposisjon og indikator -----------
    val userLocation by mapViewModel.userLocation.collectAsState()
    LaunchedEffect(mapLibreMap, userLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val loc = userLocation  ?: return@LaunchedEffect
        map.getStyle { style ->
            addUserLocationIndicator(map, style, loc.latitude, loc.longitude)
        }
    }

    // ----------- Håndter initialLocation og areaPoints -----------
    LaunchedEffect(mapLibreMap, areaPoints, initialLocation) {
        mapLibreMap?.let { map ->
            map.clear()
            if (areaPoints != null && areaPoints.isNotEmpty()) {
                val polygonOptions = PolygonOptions()
                    .addAll(areaPoints.map { LatLng(it.first, it.second) })
                    .fillColor(0x5500BCD4)
                    .strokeColor(android.graphics.Color.BLUE)
                map.addPolygon(polygonOptions)
                val bounds = LatLngBounds.Builder()
                areaPoints.forEach { bounds.include(LatLng(it.first, it.second)) }
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50))
            } else if (initialLocation != null) {
                val (lat, lng) = initialLocation
                map.addMarker(MarkerOptions().position(LatLng(lat, lng)).title("Favorittpunkt"))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 14.0))
            }
        }
    }

    // ----------- Rydd opp markører ved skjermbytte -----------
    DisposableEffect(Unit) {
        onDispose {
            mapLibreMap?.clear()
        }
    }

    // ----------- Snackbar fra ViewModel -----------
    LaunchedEffect(mapViewModel.uiEvents) {
        mapViewModel.uiEvents.collect { event ->
            if (event is MapUiEvent.ShowAlertSnackbar) {
                snackbarHostState.showSnackbar(event.message, actionLabel = "Vis mer")
            }
        }
    }

    // ----------- GribViewModel for værdata -----------
    val gribViewModel: GribViewModel = viewModel(
        factory = GribViewModelFactory(
            GribRepository(
                GribRetrofitInstance.GribApi,
                ctx
            )
        )
    )

    // ----------- MetAlerts bottom sheet -----------
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()
    LaunchedEffect(selectedFeature) {
        if (selectedFeature != null) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }

    // ----------- UI: BottomSheetScaffold med kart, lag og kontroller -----------
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            selectedFeature?.let { feature ->
                MetAlertsBottomSheetContent(
                    feature = feature,
                    onClose = { metAlertsViewModel.selectFeature(null) }
                )
            }
        },
        sheetPeekHeight = 0.dp
    ) { sheetPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(sheetPadding)
        ) {
            // 1) Kartcontainer
            MapViewContainer(
                mapView  = mapView,
                styleUrl = mapViewModel.styleUrl,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map, _ ->
                    mapLibreMap = map
                    mapViewModel.initializeMap(map, ctx)
                }
            )
            // 2) Lag
            mapLibreMap?.let { map ->
                MapLayers(
                    map        = map,
                    mapView    = mapView,
                    aisViewModel       = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel    = forbudViewModel,
                    gribViewModel      = gribViewModel
                )
            }
            // 3) Kontroller
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
                    onRequestPermission   = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
            }
        }
    }
}
