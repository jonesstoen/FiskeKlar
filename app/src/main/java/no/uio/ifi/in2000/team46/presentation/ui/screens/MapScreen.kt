package no.uio.ifi.in2000.team46.presentation.ui.screens


/*
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


    //updating the user's position every 5 seconds
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
                        .padding(start = 16.dp, end = 100.dp)
                ) {
                    // Search Box Top Left
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

                // Weather Display Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    WeatherDisplay(
                        temperature = temperature,
                        symbolCode = weatherSymbol,
                        modifier = Modifier.padding(4.dp),
                        onWeatherClick = {
                            // Hent detaljert værinformasjon og naviger til værdetalj-skjermen
                            coroutineScope.launch {
                                val weatherDetails = weatherService.getWeatherDetails(
                                    mapViewModel.currentLocation.value.latitude,
                                    mapViewModel.currentLocation.value.longitude
                                )
                                navController.navigate(
                                    "weather_detail/${weatherDetails.temperature}/${weatherDetails.feelsLike}/" +
                                    "${weatherDetails.highTemp}/${weatherDetails.lowTemp}/${weatherDetails.symbolCode}/" +
                                    "${weatherDetails.description}"
                                )
                            }
                        }
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

                // Location Button, Bottom Right
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
            delay(5000) // delay to ensure the map is loaded
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
}*/
