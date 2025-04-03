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
import androidx.compose.ui.Alignment
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import no.uio.ifi.in2000.team46.map.layers.MetAlertsLayerComponent
import no.uio.ifi.in2000.team46.map.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.ui.components.LayerFilterButton
import no.uio.ifi.in2000.team46.presentation.ui.components.weather.WeatherDisplay
import no.uio.ifi.in2000.team46.presentation.ui.components.metAlerts.MetAlertsBottomSheetContent
import no.uio.ifi.in2000.team46.presentation.ui.components.zoomToLocationButton
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel

import org.maplibre.android.maps.MapLibreMap
import java.time.LocalDate
import java.time.LocalTime


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
    aisViewModel: AisViewModel = viewModel()
) {
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isMapInitialized by remember { mutableStateOf(false) }
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val temperature by mapViewModel.temperature.collectAsState()
    val weatherSymbol by mapViewModel.weatherSymbol.collectAsState()
    var showFishingLog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val fishingLogViewModel: FishingLogViewModel = viewModel { FishingLogViewModel(FishLogRepository(context))}

    // State for new entry
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectedMetAlert by metAlertsViewModel.selectedFeature.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()


    //opening the sheet when a warning is selected
    LaunchedEffect(selectedMetAlert) {
        if (selectedMetAlert != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

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
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            ) { view ->
                // initialize map and making sure it is only initialized once
                if (!isMapInitialized) {
                    view.getMapAsync { map ->
                        mapViewModel.initializeMap(map, context)
                        mapLibreMap = map
                        isMapInitialized = true
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                WeatherDisplay(
                    temperature = temperature,
                    symbolCode = weatherSymbol,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                zoomToLocationButton(
                    modifier = Modifier
                ) {
                    mapLibreMap?.let { map ->
                        if (granted) {
                            mapViewModel.zoomToUserLocation(map, context)
                        } else {
                            mapViewModel.zoomToLocation(map, 63.4449834, 10.9124688, 15.0)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {showFishingLog = true},
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row (
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Åpne fiskelogg",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fiskelogg")
                }
            }
            //adding the different layers to the map, along with the filter button
            MetAlertsLayerComponent(metAlertsViewModel, mapView)
            AisLayer(mapView, aisViewModel)
            WindLayerComponent(
                windDataViewModel = windDataViewModel,
                mapView = mapView
            )
            LayerFilterButton(
                aisViewModel,
                metAlertsViewModel,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Ny fangst") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Sted") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Område") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notater") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            fishingLogViewModel.addEntry(
                                LocalDate.now(),
                                LocalTime.now(),
                                location,
                                area,
                                notes
                            )
                            showAddDialog = false
                            location = ""
                            area = ""
                            notes = ""
                        }
                    ) {
                        Text("Lagre")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Avbryt")
                    }
                }
            )
        }
    }

    if (showFishingLog) {
        FishingLogScreen(
            viewModel = fishingLogViewModel,
            onNavigateBack = { showFishingLog = false },
            modifier = Modifier.fillMaxSize()
        )
    }
}
