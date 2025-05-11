package no.uio.ifi.in2000.team46.presentation.map.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
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
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapUiEvent
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.components.controls.MapControls
import no.uio.ifi.in2000.team46.presentation.map.components.MapLayers
import no.uio.ifi.in2000.team46.presentation.map.components.MapViewContainer
import no.uio.ifi.in2000.team46.data.remote.api.Feature
import no.uio.ifi.in2000.team46.presentation.map.utils.addMapMarker
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.remote.api.GribRetrofitInstance
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModelFactory
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModelFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.annotations.PolylineOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.distinctUntilChanged
import no.uio.ifi.in2000.team46.data.repository.WaveRepository
import no.uio.ifi.in2000.team46.presentation.navigation.HighlightVesselData
import org.maplibre.android.annotations.IconFactory
import no.uio.ifi.in2000.team46.domain.ais.VesselIcons
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModelFactory
import no.uio.ifi.in2000.team46.presentation.grib.components.WaveLegend
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsLegend
import androidx.compose.runtime.saveable.rememberSaveable
import no.uio.ifi.in2000.team46.presentation.grib.components.WindLegend
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModelFactory
import no.uio.ifi.in2000.team46.presentation.map.components.controls.LegendToggle
import no.uio.ifi.in2000.team46.presentation.map.utils.removeMapMarker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import no.uio.ifi.in2000.team46.presentation.grib.components.CurrentOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.PrecipitationLegend
import no.uio.ifi.in2000.team46.presentation.grib.components.PrecipitationOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.WaveOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.WindOverlaySliders
import no.uio.ifi.in2000.team46.presentation.map.components.layermenu.GribMenuState
import no.uio.ifi.in2000.team46.presentation.onboarding.screens.MapOnboardingScreen
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.MapOnboardingViewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel

// =====================
// MAP SCREEN
// =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapView: MapView,
    mapViewModel: MapViewModel,
    aisViewModel: AisViewModel = viewModel(),
    metAlertsViewModel: MetAlertsViewModel = viewModel(),
    forbudViewModel: ForbudViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    navController: NavHostController,
    initialLocation: Pair<Double, Double>? = null,
    areaPoints: List<Pair<Double, Double>>? = null,
    highlightVessel: HighlightVesselData? = null,
    mapOnboardingViewModel: MapOnboardingViewModel = viewModel(),
    profileViewModel: ProfileViewModel
) {
    // Fjern lokal showOnboarding state og bruk ViewModel state direkte
    val showMapOnboarding by mapOnboardingViewModel.showMapOnboarding.collectAsState()
    
    // noen av verdiene som vi kunne brukt remembersavable på støtter ikke den funksjonaliteten derfor er de bare remember
    // ----------- State og permissions -----------
    val ctx = LocalContext.current
    
    // Get the theme from ProfileViewModel
    val appTheme by profileViewModel.theme.collectAsState()
    
    // Determine if dark theme should be used based on app settings, not just system theme
    val isDark = when (appTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    
    val styleUrl = mapViewModel.getStyleUrl(isDark)
    var isLayerMenuExpanded by remember { mutableStateOf(false) }


    // Request location permission
    var hasLocationPermission by rememberSaveable { mutableStateOf(false) }
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
    var mapLibreMap by remember  { mutableStateOf<MapLibreMap?>(null) }

    // Track whether user is currently dragging the map
    var isUserDragging by remember  { mutableStateOf(false) }

    // ----------- Brukerposisjon og indikator -----------
    val userLocation by mapViewModel.userLocation.collectAsState()
    val selectedLocation by mapViewModel.selectedLocation.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val selectedSearchResult = remember { mutableStateOf<Feature?>(null) }

    // Hent temperatur og værsymbol fra mapViewModel
    //FIXME: is this being  used ?
//    val temperature by mapViewModel.temperature.collectAsState()
//    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()

    //handling for initialLocation and areaPoints
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
                //map.addMarker(MarkerOptions().position(LatLng(lat, lng)).title("Favorittpunkt"))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 14.0))
            }
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
    // ------------- Bølger -------------
    val waveViewModel: WaveViewModel = viewModel(
        factory = WaveViewModelFactory(
            WaveRepository(
                api     = GribRetrofitInstance.GribApi,
                context = ctx
            )
        )
    )
    // for tracking wave loading
    val isWaveLoading by waveViewModel.isRasterLoading.collectAsState()
    if (isWaveLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .zIndex(10f)        // sørg for at det ligger over kart‐lagene
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
    // for showing the wave legend
    val isWaveVisible   by waveViewModel.isLayerVisible.collectAsState()
    val waveResult      by waveViewModel.waveData.collectAsState()

    val precipitationViewModel: PrecipitationViewModel = viewModel(
        factory = PrecipitationViewModelFactory(
            GribRepository(GribRetrofitInstance.GribApi, ctx)
        )
    )

    // ----------- MetAlerts bottom sheet -----------
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()
    LaunchedEffect(selectedFeature) {
        if (selectedFeature != null) scaffoldState.bottomSheetState.expand()
        else scaffoldState.bottomSheetState.hide()
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

    // Tegn strek og marker hvis highlightVessel er satt
    LaunchedEffect(mapLibreMap, highlightVessel) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style ->
            removeMapMarker(style) // Fjern markøren for valgt posisjon
        }
        if (highlightVessel != null) {
            mapViewModel.clearSelectedLocation() // Fjern valgt posisjon i ViewModel også
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
            // Oppdater vær for brukerposisjon og båtposisjon uten å sette valgt markør
            mapViewModel.updateWeatherForLocation(highlightVessel.userLat, highlightVessel.userLon, explicit = false)
            mapViewModel.updateWeatherForLocation(highlightVessel.vesselLat, highlightVessel.vesselLon, explicit = false)
        }
    }

    // Oppdater brukerposisjons-indikator når kartet er klart eller brukerposisjon endres
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

    // Oppdater markør når kartet er klart eller når selectedLocation endres
    LaunchedEffect(mapLibreMap, mapViewModel.selectedLocation.collectAsState().value) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style ->
            if (mapViewModel.selectedLocation.value != null && mapViewModel.isLocationExplicitlySelected()) {
                val selectedLoc = mapViewModel.selectedLocation.value!!
                addMapMarker(map, style, selectedLoc.first, selectedLoc.second, ctx)
            } else {
            }
        }
    }

    // Sjekk om dette er første launch når komponenten monteres
    LaunchedEffect(Unit) {
        mapOnboardingViewModel.checkFirstLaunch(ctx)
    }

    // ----------- UI: BottomSheetScaffold med kart, lag og kontroller -----------
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        sheetContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        sheetPeekHeight = 0.dp,
        sheetContent = {
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
                mapView = mapView,
                styleUrl = styleUrl,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map, _ ->
                    Log.d("MapScreen", "onMapReady called")
                    mapLibreMap = map
                    mapViewModel.initializeMap(map, ctx,styleUrl)
                    
                    // Add listeners to detect when user is dragging the map
                    map.addOnCameraMoveStartedListener { reason ->
                        if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                            isUserDragging = true
                        }
                    }
                    map.addOnCameraIdleListener {
                        isUserDragging = false
                    }

                    // Add click listener for the map
                    map.addOnMapLongClickListener { point ->
                        if (!isUserDragging) {
                            val screenPoint = map.projection.toScreenLocation(point)
                            val aisFeatures = map.queryRenderedFeatures(screenPoint, "ais-vessels-layer")
                            val metAlertFeatures = map.queryRenderedFeatures(screenPoint, "metalerts-layer")
                            val forbudFeatures = map.queryRenderedFeatures(screenPoint, "forbud-layer")

                            when {
                                aisFeatures.isNotEmpty() -> false
                                metAlertFeatures.isNotEmpty() -> false
                                forbudFeatures.isNotEmpty() -> false
                                else -> {
                                    Log.d("MapScreen", "Setting new location: ${point.latitude}, ${point.longitude}")
                                    mapViewModel.setSelectedLocation(point.latitude, point.longitude)
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
                    waveViewModel      = waveViewModel,
                    precipitationViewModel = precipitationViewModel
                )
            }
            LegendToggle(
                isLayerVisible = isWaveVisible && waveResult is Result.Success,
                verticalPosition = 0
            ) {
                WaveLegend(modifier = Modifier.align(Alignment.CenterEnd))
            }

            val isMetAlertsVisible by metAlertsViewModel.isLayerVisible.collectAsState()

            LegendToggle(
                isLayerVisible = isMetAlertsVisible,
                verticalPosition = 1
            ) {
                MetAlertsLegend(modifier = Modifier.align(Alignment.CenterEnd))
            }

            val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
            val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
            val isPrecipitationVisible by precipitationViewModel.isLayerVisible.collectAsState()

            LegendToggle(
                isLayerVisible = isWindLayerVisible,
                verticalPosition = 2
            ) {
                WindLegend(modifier = Modifier.align(Alignment.CenterEnd))
            }

            // 4) Precipitation‐legend
            val isPrecipVisible by precipitationViewModel.isLayerVisible.collectAsState()
            LegendToggle(
                isLayerVisible = isPrecipVisible,
                verticalPosition = 3  // velg neste ledige posisjon
            ) {
                PrecipitationLegend(
                    modifier = Modifier.align(Alignment.CenterEnd)
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
                    waveViewModel = waveViewModel,
                    precipitationViewModel = precipitationViewModel,
                    hasLocationPermission = hasLocationPermission,
                    isLayerMenuExpanded = isLayerMenuExpanded,
                    onLayerMenuExpandedChange = { isLayerMenuExpanded = it },
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
                                mapViewModel.updateWeatherForLocation(latitude, longitude, explicit = true)
                            }
                        }
                    },
                    onUserLocationSelected = { location ->
                        map.getStyle { style ->
                            removeMapMarker(style)  // Fjern eksisterende markør
                            mapViewModel.clearSelectedLocation()  // Nullstill valgt posisjon
                            mapViewModel.updateWeatherForLocation(location.latitude, location.longitude, explicit = false)
                        }
                    },
                    onShowWindSliders = { gribViewModel.setShowWindSliders(true) },
                    onShowCurrentSliders = {
                        isLayerMenuExpanded = false
                        currentViewModel.setShowCurrentSliders(true)
                    },
                    onShowWaveSliders = {
                        isLayerMenuExpanded = false
                        waveViewModel.setShowWaveSliders(true)
                    }

                )
            }
            val showWindSliders by gribViewModel.showWindSliders.collectAsState()
            val gribMenuState by gribViewModel.gribMenuState.collectAsState()

            if (isWindLayerVisible && showWindSliders) {
                WindOverlaySliders(
                    gribViewModel = gribViewModel,
                    onClose = {
                        gribViewModel.setShowWindSliders(false)
                        gribViewModel.setGribMenuState(GribMenuState.Wind)
                        isLayerMenuExpanded = true
                    }
                )
            }

            val showCurrentSliders by currentViewModel.showCurrentSliders.collectAsState()

            if (isCurrentLayerVisible && showCurrentSliders) {
                CurrentOverlaySliders(
                    currentViewModel = currentViewModel,
                    onClose = {
                        currentViewModel.setShowCurrentSliders(false)
                        gribViewModel.setGribMenuState(GribMenuState.Current)
                        isLayerMenuExpanded = true
                    }
                )
            }
            val showWaveSliders by waveViewModel.showWaveSliders.collectAsState()

            if (isWaveVisible && showWaveSliders) {
                WaveOverlaySliders(
                    waveViewModel = waveViewModel,
                    onClose = {
                        waveViewModel.setShowWaveSliders(false)
                        gribViewModel.setGribMenuState(GribMenuState.Wave)
                        isLayerMenuExpanded = true
                    }
                )
            }
            val showPrecipSliders by precipitationViewModel.showPrecipSliders.collectAsState()

            if (isPrecipitationVisible && showPrecipSliders) {
                PrecipitationOverlaySliders(
                    viewModel = precipitationViewModel,
                    onClose = {
                        precipitationViewModel.setShowPrecipSliders(false)
                        gribViewModel.setGribMenuState(GribMenuState.Precipitation)
                        isLayerMenuExpanded = true
                    }
                )
            }





            // Legg til hjelpeknapp i øvre høyre hjørne
            IconButton(
                onClick = { mapOnboardingViewModel.showMapOnboarding() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Vis hjelp",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Bruk showMapOnboarding fra ViewModel
            if (showMapOnboarding) {
                MapOnboardingScreen(
                    viewModel = mapOnboardingViewModel,
                    onFinish = { mapOnboardingViewModel.hideMapOnboarding() }
                )
            }
        }
    }
}

