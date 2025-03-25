package no.uio.ifi.in2000.team46.presentation.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.maps.MapView
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.map.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.ui.components.zoomToLocationButton
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import org.maplibre.android.maps.MapLibreMap


/** MapScreen er UI-skjermen der kartet vises, og den kobler sammen ViewModel og den visuelle presentasjonen.
 * Dette er en Composable-skjerm som integrerer MapView (fra tradisjonell Android View) i et Jetpack Compose-miljø.
 * Bruk av AndroidView: Den benytter AndroidView for å legge inn et MapView i Compose-layouten.
 * Samarbeid med ViewModel: MapScreen henter et MapViewModel-objekt og kaller funksjonen initializeMap for å sette opp kartet når visningen lastes.
 * Dette knytter sammen UI og logikk slik at eventuelle endringer i kartets tilstand kan observeres og reflekteres i brukergrensesnittet.
 */
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val mapView = rememberMapViewWithLifecycle()
    // Henter et MapViewModel for å håndtere kartlogikken
    val mapViewModel: MapViewModel = viewModel()
    var mapLibreMap: MapLibreMap? = null

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view: MapView ->
            // getMapAsync laster kartet asynkront. Når kartet er klart, kalles lambdaen
            view.getMapAsync { map ->
                // Initialiserer kartet med ønsket stil og posisjon ved hjelp av ViewModel
                mapViewModel.initializeMap(map)
                mapLibreMap = map


            }
        }
        zoomToLocationButton (
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {

            mapLibreMap?.let { map ->
                mapViewModel.zoomToLocation(map, 59.9139, 10.7522, 10.0)
            }
        }

    }
}