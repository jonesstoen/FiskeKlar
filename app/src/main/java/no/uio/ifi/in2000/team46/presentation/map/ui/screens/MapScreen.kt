package no.uio.ifi.in2000.team46.presentation.map.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import no.uio.ifi.in2000.team46.data.remote.geocoding.Feature
import no.uio.ifi.in2000.team46.presentation.map.utils.addMapMarker
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.remote.grib.GribRetrofitInstance
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModelFactory
import no.uio.ifi.in2000.team46.presentation.grib.components.CurrentViewModelFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.annotations.PolylineOptions
import androidx.compose.runtime.LaunchedEffect
import no.uio.ifi.in2000.team46.presentation.navigation.HighlightVesselData
import org.maplibre.android.annotations.IconFactory
import no.uio.ifi.in2000.team46.domain.model.ais.VesselIcons

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
    navController: NavHostController,
    initialLocation: Pair<Double, Double>? = null,
    areaPoints: List<Pair<Double, Double>>? = null,
    highlightVessel: HighlightVesselData? = null
) {
    // ----------- State og permissions -----------
    val ctx = LocalContext.current

    // Request location permission
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ----------- Snackbar og bottom sheet state -----------
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

    // ----------- MapLibreMap referanse -----------
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    // Track whether user is currently dragging the map
    var isUserDragging by remember { mutableStateOf(false) }

    // ----------- Brukerposisjon og indikator -----------
    val userLocation by mapViewModel.userLocation.collectAsState()
    val selectedLocation by mapViewModel.selectedLocation.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val selectedSearchResult = remember { mutableStateOf<Feature?>(null) }

    // Hent temperatur og værsymbol fra mapViewModel
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()

    // Oppdater markørene når kartet er klart
    LaunchedEffect(mapLibreMap, userLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        map.getStyle { style ->
            addUserLocationIndicator(map, style, loc.latitude, loc.longitude)

            // Oppdater vær basert på brukerens posisjon ved oppstart
            if (!mapViewModel.isLocationExplicitlySelected()) {
                mapViewModel.updateTemperature(loc.latitude, loc.longitude)
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
    val currentViewModel: CurrentViewModel = viewModel(
        factory = CurrentViewModelFactory(
            CurrentRepository(
                api = GribRetrofitInstance.GribApi,
                context = ctx
            )
        )
    )
    val driftViewModel: DriftViewModel = viewModel(
        factory = DriftViewModelFactory(
            GribRepository(GribRetrofitInstance.GribApi, ctx),
            CurrentRepository(GribRetrofitInstance.GribApi, ctx)
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

    // Tegn strek og marker hvis highlightVessel er satt
    LaunchedEffect(mapLibreMap, highlightVessel) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (highlightVessel != null) {
            map.clear()
            // Viktig: Initialiser AIS-ikoner før bruk!
            VesselIcons.initializeIcons(ctx)
            // Tegn vanlig rød linje mellom bruker og båt
            map.addPolyline(
                PolylineOptions()
                    .add(
                        LatLng(highlightVessel.userLat, highlightVessel.userLon),
                        LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon)
                    )
                    .color(android.graphics.Color.RED)
                    .width(6f)
            )

            // Marker båtposisjon med AIS-ikon og navn
            val vesselIconType = VesselIcons.getVesselStyle(highlightVessel.shipType).iconType
            val bitmap = VesselIcons.getIcons()[vesselIconType]
            val iconFactory = IconFactory.getInstance(ctx)
            if (bitmap != null) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon))
                        .title(highlightVessel.vesselName)
                        .icon(iconFactory.fromBitmap(bitmap))
                )
            } else {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon))
                        .title(highlightVessel.vesselName)
                )
            }

            // Zoom til området mellom bruker og båt
            val bounds = LatLngBounds.Builder()
                .include(LatLng(highlightVessel.userLat, highlightVessel.userLon))
                .include(LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon))
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        }
    }

    // ----------- UI: BottomSheetScaffold med kart, lag og kontroller -----------
    BottomSheetScaffold(
        scaffoldState = scaffoldState,

        // Hoved‐underlag for hele Scaffold (selve “screen‐bakgrunnen”)
        containerColor = MaterialTheme.colorScheme.background,
        contentColor   = MaterialTheme.colorScheme.onBackground,

        // Arkets bakgrunn og tekst‐ikon‐farge
        sheetContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        sheetContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,

        sheetPeekHeight = 0.dp,
        sheetContent   = {
            selectedFeature?.let { feature ->
                MetAlertsBottomSheetContent(
                    feature = feature,
                    onClose = { metAlertsViewModel.selectFeature(null) }
                )
            }
        }
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
            // 2) Lag
            mapLibreMap?.let { map ->
                MapLayers(
                    map        = map,
                    mapView    = mapView,
                    aisViewModel       = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel    = forbudViewModel,
                    gribViewModel      = gribViewModel,
                    currentViewModel   = currentViewModel,
                    driftViewModel     = driftViewModel,
                )
            }
            // 3) Kontroller
            mapLibreMap?.let { map ->
                MapControls(
                    map = map,
                    mapViewModel = mapViewModel,
                    searchViewModel = searchViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    aisViewModel = aisViewModel,
                    forbudViewModel = forbudViewModel,
                    gribViewModel = gribViewModel,
                    currentViewModel = currentViewModel,
                    driftViewModel = driftViewModel,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    navController = navController,
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
