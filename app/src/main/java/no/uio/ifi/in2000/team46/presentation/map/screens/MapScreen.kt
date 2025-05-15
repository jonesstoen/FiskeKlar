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
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.annotations.PolylineOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.data.repository.WaveRepository
import no.uio.ifi.in2000.team46.presentation.navigation.HighlightVesselData
import org.maplibre.android.annotations.IconFactory
import no.uio.ifi.in2000.team46.domain.ais.VesselIcons
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModelFactory
import androidx.compose.runtime.saveable.rememberSaveable
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModelFactory
import no.uio.ifi.in2000.team46.presentation.map.utils.removeMapMarker
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import no.uio.ifi.in2000.team46.presentation.grib.components.CurrentOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.PrecipitationOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.WaveOverlaySliders
import no.uio.ifi.in2000.team46.presentation.grib.components.WindOverlaySliders
import no.uio.ifi.in2000.team46.presentation.map.components.layermenu.GribMenuState
import no.uio.ifi.in2000.team46.presentation.onboarding.screens.MapOnboardingScreen
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.MapOnboardingViewModel
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.utils.NetworkUtils
import no.uio.ifi.in2000.team46.presentation.map.components.NetworkConnectivityAlert
import no.uio.ifi.in2000.team46.presentation.map.components.LegendPanel
import no.uio.ifi.in2000.team46.presentation.map.components.controls.LegendController
import no.uio.ifi.in2000.team46.presentation.map.favorites.FavoritesLayerViewModel
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.presentation.map.utils.SetupSnackbar
import no.uio.ifi.in2000.team46.presentation.map.utils.SetupBottomSheetReset
import no.uio.ifi.in2000.team46.presentation.map.utils.SetupInitialMapView
import androidx.compose.ui.geometry.Rect
import org.maplibre.android.annotations.PolygonOptions

/**
 * WARNINGS: This file contains usage of deprecated MapLibre classes such as MarkerOptions and PolygonOptions.
 * These are used intentionally due to the lack of stable or well documented alternatives in the current SDK.
 * The functionality remains reliable for our purposes and was prioritized for simplicity and compatibility.
 * Other warnings (unused variables, unnecessary safe calls) are minor and do not affect functionality.
 */

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
    profileViewModel: ProfileViewModel,
    showFavorites: Boolean = false
) {
    // Create the FavoritesLayerViewModel using the local database DAO
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val favoritesViewModel = remember {
        FavoritesLayerViewModel(FavoriteRepository(db.favoriteLocationDao()))
    }

    // use the shared onboarding state from the ViewModel rather than local state
    val showMapOnboarding by mapOnboardingViewModel.showMapOnboarding.collectAsState()

    // when showFavorites is true, automatically enable the favorites layer
    LaunchedEffect(showFavorites) {
        if (showFavorites) {
            favoritesViewModel.setLayerVisibility(true)
        }
    }

    // track whether we have internet connectivity
    var isNetworkConnected by remember { mutableStateOf(NetworkUtils.isNetworkAvailable(context)) }
    var showNetworkAlert by remember { mutableStateOf(false) }

    // if not connected, show a one-time alert dialog
    LaunchedEffect(Unit) {
        if (!isNetworkConnected) {
            showNetworkAlert = true
        }
    }

    // simple dialog component to notify user of lost connectivity
    NetworkConnectivityAlert(
        show = showNetworkAlert,
        onDismiss = { showNetworkAlert = false }
    )


    // observe the users chosen app theme from ProfileViewModel
    val appTheme by profileViewModel.theme.collectAsState()

    // determine whether to use dark theme based on user setting or system default
    val isDark = when (appTheme) {
        "dark" -> true
        "light" -> false
        else   -> isSystemInDarkTheme()
    }

    // get the appropriate MapLibre style URL according to the theme
    val styleUrl = mapViewModel.getStyleUrl(isDark)

    // control visibility of the layer selection menu
    var isLayerMenuExpanded by remember { mutableStateOf(false) }

    // ----------- Initial map zoom state -----------

    // track whether we have already performed the initial camera zoom
    val hasPerformedInitialZoom by mapViewModel.hasPerformedInitialZoom.collectAsState()



    var hasLocationPermission by rememberSaveable { mutableStateOf(false) }

    // Launcher to request the ACCESS_FINE_LOCATION permission on demand
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    // Automatically request permission once when this composable enters composition
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    // Host for showing transient snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    // Scaffold that ties together the map content, snackbar, and bottom sheet
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState,
        snackbarHostState = snackbarHostState
    )

    // Hold a reference to the underlying MapLibreMap instance
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    // Track whether the user is currently dragging the map (to differentiate from programmatic camera moves)
    var isUserDragging by remember { mutableStateOf(false) }


    val userLocation by mapViewModel.userLocation.collectAsState()
    val selectedLocation by mapViewModel.selectedLocation.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    // Hold the last search result the user tapped on
    val selectedSearchResult = remember { mutableStateOf<Feature?>(null) }

    //Initial map setup (area or point)
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
                // Legg til markør for favorittplasseringen (punkter)
                map.addMarker(MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title("Favorittpunkt")
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 14.0))
            }
        }
    }
    // ----------- Snackbar from ViewModel events -----------

    SetupSnackbar(
        uiEvents          = mapViewModel.uiEvents,
        snackbarHostState = snackbarHostState,
        onShowFeature     = { feature -> mapViewModel.selectMetAlert(feature) }
    )


    // Main GRIB data ViewModel (wind, pressure, etc.)
    val gribViewModel: GribViewModel = viewModel(
        factory = GribViewModelFactory(
            GribRepository(GribRetrofitInstance.GribApi, context)
        )
    )
    // Current flow ViewModel
    val currentViewModel: CurrentViewModel = viewModel(
        factory = CurrentViewModelFactory(
            CurrentRepository(GribRetrofitInstance.GribApi, context)
        )
    )
    // Drift (combined) ViewModel
    val driftViewModel: DriftViewModel = viewModel(
        factory = DriftViewModelFactory(
            GribRepository(GribRetrofitInstance.GribApi, context),
            CurrentRepository(GribRetrofitInstance.GribApi, context)
        )
    )
    // Wave height ViewModel
    val waveViewModel: WaveViewModel = viewModel(
        factory = WaveViewModelFactory(
            WaveRepository(GribRetrofitInstance.GribApi, context)
        )
    )
    val precipitationViewModel: PrecipitationViewModel = viewModel(
        factory = PrecipitationViewModelFactory(
            GribRepository(GribRetrofitInstance.GribApi, context)
        )
    )
    // controller for showing the legends


    // Show a loading overlay while wave data is being fetched
    val isWaveLoading by waveViewModel.isRasterLoading.collectAsState()
    if (isWaveLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .zIndex(10f)
        ) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

    // Legend controller for switching between overlays
    val legendController = remember { LegendController() }

    LegendPanel(
        isDark                  = isDark,
        legendController        = legendController,
        gribViewModel           = gribViewModel,
        waveViewModel           = waveViewModel,
        precipitationViewModel  = precipitationViewModel,
        currentViewModel        = currentViewModel,
        metAlertsViewModel      = metAlertsViewModel
    )

    //  Bottom sheet expand/collapse logic

    // Expand or hide the MetAlerts bottom sheet when a feature is selected
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()
    LaunchedEffect(selectedFeature) {
        if (selectedFeature != null) scaffoldState.bottomSheetState.expand()
        else scaffoldState.bottomSheetState.hide()
    }

    // Automatically clear the selected alert when the sheet hides
    SetupBottomSheetReset(
        scaffoldState      = scaffoldState,
        metAlertsViewModel = metAlertsViewModel
    )



    // draw a line and marker when a vessel is highlighted
    LaunchedEffect(mapLibreMap, highlightVessel) {
        // exit early if the map instance isn't ready yet
        val map = mapLibreMap ?: return@LaunchedEffect

        // Remove any existing selected-location marker from the map style
        map.getStyle { style ->
            removeMapMarker(style)
        }

        // Only proceed if we actually have a vessel to highlight
        if (highlightVessel != null) {
            mapViewModel.clearSelectedLocation()
            map.clear()

            // IMPORTANT: Ensure AIS icon bitmaps are loaded before adding vessel markers
            VesselIcons.initializeIcons(context)

            // Draw a red polyline between the user and the vessel
            map.addPolyline(
                PolylineOptions()
                    .add(
                        LatLng(highlightVessel.userLat, highlightVessel.userLon),
                        LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon)
                    )
                    .color(android.graphics.Color.RED)
                    .width(6f)
            )

            val vesselIconType = VesselIcons.getVesselStyle(highlightVessel.shipType).iconType
            val bitmap = VesselIcons.getIcons()[vesselIconType]
            val iconFactory = IconFactory.getInstance(context)

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

            // Compute a LatLngBounds that includes both user and vessel positions
            val bounds = LatLngBounds.Builder()
                .include(LatLng(highlightVessel.userLat, highlightVessel.userLon))
                .include(LatLng(highlightVessel.vesselLat, highlightVessel.vesselLon))
                .build()

            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

            // Refresh weather data for both locations without creating a new marker
            mapViewModel.updateWeatherForLocation(
                highlightVessel.userLat,
                highlightVessel.userLon,
                explicit = false
            )
            mapViewModel.updateWeatherForLocation(
                highlightVessel.vesselLat,
                highlightVessel.vesselLon,
                explicit = false
            )
        }
    }


    // Oppdater brukerposisjons-indikator når kartet er klart eller brukerposisjon endres
    //updating user location indicator, when map is ready or the user location changes
    LaunchedEffect(mapLibreMap, userLocation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        map.getStyle { style ->
            addUserLocationIndicator(map, style, loc.latitude, loc.longitude)

            //update weather based on user location at startup
            if (!mapViewModel.isLocationExplicitlySelected()) {
                mapViewModel.updateTemperature(loc.latitude, loc.longitude)
            }
        }
    }

    //updating marker when map is ready or when selectedLocation changes
    LaunchedEffect(mapLibreMap, mapViewModel.selectedLocation.collectAsState().value) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style ->
            if (mapViewModel.selectedLocation.value != null && mapViewModel.isLocationExplicitlySelected()) {
                val selectedLoc = mapViewModel.selectedLocation.value!!
                addMapMarker(map, style, selectedLoc.first, selectedLoc.second, context)
            }
        }
    }

    //chek it it is the first time launching in order to show onboarding
    LaunchedEffect(Unit) {
        mapOnboardingViewModel.checkFirstLaunch(context)
    }

    //zoom to user location only the first time the map opens from the homescreen
    LaunchedEffect(mapLibreMap) {

        if (!hasPerformedInitialZoom && initialLocation == null && areaPoints == null) {
            mapLibreMap?.let { map ->
                //delay to be sure the map is ready
                delay(300)

                mapViewModel.zoomToUserLocationInitial(map, context)
            }
        } else if (!hasPerformedInitialZoom && (initialLocation != null || areaPoints != null)) {
            // if the map is opened from the homescreen with a location or area, we don't want to zoom to user location
            mapViewModel.setInitialZoomPerformed()
        }
    }

    //bottom scaffold for showing the metalerts
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
            MapViewContainer(
                mapView = mapView,
                styleUrl = styleUrl,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map, _ ->
                    Log.d("MapScreen", "onMapReady called")
                    mapLibreMap = map
                    mapViewModel.initializeMap(map, context,styleUrl)
                    
                    // add listeners to detect when user is dragging the map
                    map.addOnCameraMoveStartedListener { reason ->
                        if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                            isUserDragging = true
                        }
                    }
                    map.addOnCameraIdleListener {
                        isUserDragging = false
                    }

                    // add click listener for the map
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
                                        addMapMarker(map, style, point.latitude, point.longitude, context)
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
            // layer set up
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
                    precipitationViewModel = precipitationViewModel,
                    favoritesViewModel = favoritesViewModel,
                    isDarkTheme = isDark
                )
            }
            //variables for keeping track of the layer visibility
            val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
            val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
            val isPrecipitationVisible by precipitationViewModel.isLayerVisible.collectAsState()


        // Ddefining a map to hold the bounds of UI elements
        val elementBounds = remember { mutableMapOf<String, Rect>() }
        
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
                favoritesViewModel = favoritesViewModel,
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                isLayerMenuExpanded = isLayerMenuExpanded,
                onLayerMenuExpandedChange = { isLayerMenuExpanded = it },
                navController = navController,
                onSearchResultSelected = { feature ->
                    searchViewModel.addToHistory(feature)
                    selectedSearchResult.value = feature

                    // fetching the coordinates from the search result
                    val latitude = feature.geometry.coordinates[1]
                    val longitude = feature.geometry.coordinates[0]

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(latitude, longitude),
                            14.0
                        )
                    )

                    map.getStyle { style ->

                        mapViewModel.clearSelectedLocation()

                        mapViewModel.setSelectedLocation(latitude, longitude)

                        addMapMarker(map, style, latitude, longitude, context)
                    }
                },
                onShowWindSliders = { gribViewModel.setShowWindSliders(true) },
                onShowCurrentSliders = {
                    isLayerMenuExpanded = false
                    currentViewModel.setShowCurrentSliders(true)
                },
                onShowWaveSliders = { waveViewModel.setShowWaveSliders(true) },
                onUserLocationSelected = { location ->

                    mapViewModel.updateTemperature(location.latitude, location.longitude)
                },
                elementBounds = elementBounds
            )
        }

            val showWindSliders by gribViewModel.showWindSliders.collectAsState()


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
            val isWaveVisible   by waveViewModel.isLayerVisible.collectAsState()

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


            // help button for showing the onboarding screen after the map is ready
            IconButton(
                onClick = { mapOnboardingViewModel.showMapOnboarding() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 14.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Vis hjelp",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (showMapOnboarding) {
                MapOnboardingScreen(
                    viewModel = mapOnboardingViewModel,
                    onFinish = { mapOnboardingViewModel.hideMapOnboarding() },
                    onZoom = { 

                        mapLibreMap?.let { map ->
                            val currentZoom = map.cameraPosition.zoom
                            val newZoom = currentZoom + 1.0
                            val cameraUpdate = CameraUpdateFactory.zoomTo(newZoom)
                            map.animateCamera(cameraUpdate)
                        }
                    },
                    onToggleLayers = {
                        isLayerMenuExpanded = !isLayerMenuExpanded
                    },
                    onShowLocation = { 

                        mapLibreMap?.let { map ->
                            mapViewModel.zoomToUserLocation(map, context)
                        }
                    }
                )
            }
        }
    }
}
