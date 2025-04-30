package no.uio.ifi.in2000.team46.presentation.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.PrecipitationViewModel
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel

@Composable
fun MapControls(
    map: MapLibreMap,
    mapViewModel: MapViewModel,
    searchViewModel: SearchViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    precipitationViewModel: PrecipitationViewModel,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        // 1) Søkeboks øverst til venstre
        SearchBox(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            map = map,
            searchResults = searchViewModel.searchResults.collectAsState().value,
            isSearching   = searchViewModel.isSearching.collectAsState().value,
            onSearch      = { query ->
                val target = map.cameraPosition.target
                if (target != null) {
                    searchViewModel.search(query, focusLat = target.latitude, focusLon = target.longitude)
                }
            },
            onResultSelected = { feature ->
                val coords = feature.geometry.coordinates
                if (coords.size >= 2) {
                    mapViewModel.zoomToLocation(map, coords[1], coords[0], zoom = 15.0)
                    searchViewModel.clearResults()
                }
            }
        )

        // 2) Zoom + filter i kolonne nederst til venstre
        // Zoom + filter nederst til venstre, zoom rett over filter:
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)  // <-- plasser kolonnen nederst til venstre
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),  // litt mellomrom mellom knappene
            horizontalAlignment = Alignment.Start
        ) {
            ZoomButton(
                onZoomIn  = { mapViewModel.zoomIn(map) },
                onZoomOut = { mapViewModel.zoomOut(map) }
            )
            LayerFilterButton(
                aisViewModel       = aisViewModel,
                metAlertsViewModel = metAlertsViewModel,
                forbudViewModel    = forbudViewModel,
                gribViewModel = gribViewModel,
                currentViewModel =  currentViewModel,
                driftViewModel = driftViewModel,
                precipitationViewModel = precipitationViewModel,

            )

        }

        // 3) Vær‐display + posisjonsknapp nederst til høyre
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherDisplay(
                temperature = mapViewModel.temperature.collectAsState().value,
                symbolCode  = mapViewModel.weatherSymbol.collectAsState().value
            )
            zoomToLocationButton {
                if (hasLocationPermission) {
                    mapViewModel.zoomToUserLocation(map, context)
                } else {
                    onRequestPermission()
                }
            }
        }
    }
}



