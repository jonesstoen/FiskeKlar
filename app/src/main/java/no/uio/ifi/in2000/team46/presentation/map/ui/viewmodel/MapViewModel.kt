package no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel



import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MapConstants
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MapController
import no.uio.ifi.in2000.team46.presentation.map.utils.addUserLocationIndicator
import no.uio.ifi.in2000.team46.utils.ais.VesselIconHelper
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.utils.isPointInPolygon
import no.uio.ifi.in2000.team46.domain.model.metalerts.Feature
import no.uio.ifi.in2000.team46.domain.model.metalerts.MetAlertsResponse
//import mapconstants

/**MapViewModel styrer forretningslogikken og tilstanden for kartet.
 *
 * Denne filen inneholder MapViewModel-klassen som håndterer logikken for kartvisningen.
 * Klassen styrer initialisering av kartet , og holder oversikt over
 * kameraets posisjon ved hjelp av LiveData.
 */


/** Hendelser som MapViewModel kan skyte ut til UI (snackbar osv). */
sealed class MapUiEvent {
    data class ShowAlertSnackbar(val message: String) : MapUiEvent()
}

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val metAlertsRepository: MetAlertsRepository
) : ViewModel() {

    // ----- Konstanter og API‑nøkler -----
    private val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
    val styleUrl: String =
        "https://api.maptiler.com/maps/basic/style.json?key=$apiKey"

    private val initialLat: Double = MapConstants.INITIAL_LAT
    private val initialLon: Double = MapConstants.INITIAL_LON

    // ----- StateFlows for UI‑binding -----
    val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _temperature = MutableStateFlow<Double?>(null)
    val temperature: StateFlow<Double?> = _temperature

    private val _weatherSymbol = MutableStateFlow<String?>(null)
    val weatherSymbol: StateFlow<String?> = _weatherSymbol

    // MetAlerts (filtrert til marine)
    private val _metAlertsResponse = MutableStateFlow<MetAlertsResponse?>(null)
    val metAlertsResponse: StateFlow<MetAlertsResponse?> = _metAlertsResponse

    // Hendelser til UI (snackbar)
    private val _uiEvents = MutableSharedFlow<MapUiEvent>()
    val uiEvents: SharedFlow<MapUiEvent> = _uiEvents

    // Weather‑service
    private val weatherService = WeatherService()

    private val _selectedFeature = MutableStateFlow<Feature?>(null)  // For valgt MetAlert
    val selectedFeature: StateFlow<Feature?> = _selectedFeature

    @OptIn(ExperimentalMaterial3Api::class)
    private val _bottomSheetState = MutableStateFlow<SheetValue>(SheetValue.Hidden)
    @OptIn(ExperimentalMaterial3Api::class)
    val bottomSheetState: StateFlow<SheetValue> = _bottomSheetState

    init {
        // Hent varslene én gang
        fetchMetAlerts()
        // Start polling av posisjon + sjekk polygon
        startLocationPolling()
    }

    /** Henter varsler fra repository og filtrer ut kun \"marine\". */
    private fun fetchMetAlerts() {
        viewModelScope.launch {
            when (val result = metAlertsRepository.getAlerts()) {
                is Result.Success -> {
                    val marineOnly = result.data.features.filter {
                        it.properties.geographicDomain == "marine"
                    }
                    _metAlertsResponse.value = MetAlertsResponse(
                        features = marineOnly,
                        lang = result.data.lang,
                        lastChange = result.data.lastChange,
                        type = result.data.type
                    )
                }
                is Result.Error -> {
                    Log.e(
                        "MapViewModel",
                        "Error fetching MetAlerts: ${result.exception.message}"
                    )
                }
            }
        }
    }

    /** Poller brukerposisjon hvert 5 sekund, oppdaterer temperatur og sjekker polygon‑alarmer. */
    private fun startLocationPolling() {
        viewModelScope.launch {
            while (isActive) {
                val loc = locationRepository.getCurrentLocation()
                _userLocation.value = loc

                // Oppdater temperatur ved ny posisjon
                loc?.let { updateTemperature(it.latitude, it.longitude) }

                // Sjekk om vi står i et polygon‑varsel
                checkForPolygons(loc)

                delay(5_000)
            }
        }
    }

    /** Går gjennom alle Polygon‑varsler og emitter snackbar‑event om brukeren er innenfor. */
    private suspend fun checkForPolygons(location: Location?) {
        val alerts = _metAlertsResponse.value ?: return
        if (location == null) return

        for (feature in alerts.features) {
            if (feature.geometry.type.equals("Polygon", ignoreCase = true)) {
                val polygon = extractPolygonCoordinates(feature)
                if (isPointInPolygon(location.latitude, location.longitude, polygon)) {
                    val raw = feature.properties.eventAwarenessName
                    val trimmed = raw.replace("fare", "").trim()
                    _uiEvents.emit(
                        MapUiEvent.ShowAlertSnackbar(
                            "ADVARSEL: Du er i et område med utsatt $trimmed farevarsel"
                        )
                    )
                    return
                }
            }
        }
    }

    /** Ekstraherer en liste av (lon,lat)-par fra GeoJSON Polygon. */
    private fun extractPolygonCoordinates(
        feature: Feature
    ): List<Pair<Double, Double>> {
        val coordsRaw = feature.geometry.coordinates as? List<*>
        val firstRing = coordsRaw?.firstOrNull() as? List<*>
        return firstRing?.mapNotNull { item ->
            val coord = item as? List<*>
            val lon = coord?.getOrNull(0) as? Double
            val lat = coord?.getOrNull(1) as? Double
            if (lon != null && lat != null) Pair(lon, lat) else null
        } ?: emptyList()
    }

    /** Setter stil og initial visning når kartet er klart. */
    fun initializeMap(map: MapLibreMap, context: Context) {
        map.setStyle(styleUrl) { style ->
            try {
                val controller = MapController(map)
                controller.setInitialView(
                    initialLat, initialLon,
                    zoom = MapConstants.INITIAL_ZOOM
                )
                addUserLocationIndicator(map, style, initialLat, initialLon)
                VesselIconHelper.addVesselIconsToStyle(context, style)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error initializing map: ${e.message}")
            }
        }
    }

    /** Henter temperatur og symbolkode for gitt posisjon. */
    private fun updateTemperature(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val data = weatherService.getWeatherData(lat, lon)
                _temperature.value = data.temperature
                _weatherSymbol.value = data.symbolCode
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching weather: ${e.message}")
            }
        }
    }

    // Når et MetAlert er valgt, endre bottom sheet state
    @OptIn(ExperimentalMaterial3Api::class)
    fun selectMetAlert(feature: Feature?) {
        _selectedFeature.value = feature
        if (feature != null) {
            _bottomSheetState.value = SheetValue.Expanded  // Expand bottom sheet
        } else {
            _bottomSheetState.value = SheetValue.Hidden  // Hide bottom sheet
        }
    }

    // Reset valgt MetAlert når bottom sheet ikke er utvidet
    @OptIn(ExperimentalMaterial3Api::class)
    fun resetMetAlertSelectionIfNeeded(state: SheetValue) {
        // Hvis bottom sheet er delvis utvidet eller skjult, nullstill MetAlert
        if (state != SheetValue.Expanded) {
            _selectedFeature.value = null  // Nullstiller MetAlert hvis bottom sheet ikke er fullt utvidet
        }
    }

    /** Zoom til angitt posisjon, oppdaterer temperatur. */
    fun zoomToLocation(map: MapLibreMap, lat: Double, lon: Double, zoom: Double) {
        try {
            MapController(map).zoomToLocation(lat, lon, zoom)
            updateTemperature(lat, lon)
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error zooming: ${e.message}")
        }
    }

    /** Zoom til siste kjente brukerposisjon + markør. */
    fun zoomToUserLocation(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            val loc = locationRepository.getFastLocation()
            loc?.let {
                zoomToLocation(map, it.latitude, it.longitude, MapConstants.INITIAL_ZOOM)
                addUserLocationIndicator(map, map.style!!, it.latitude, it.longitude)
            }
        }
    }

    /** Enkel zoom‑in/zoom‑out. */
    fun zoomIn(map: MapLibreMap) {
        val z = map.cameraPosition.zoom
        map.animateCamera(CameraUpdateFactory.zoomTo(z + 1))
    }
    fun zoomOut(map: MapLibreMap) {
        val z = map.cameraPosition.zoom
        map.animateCamera(CameraUpdateFactory.zoomTo(z - 1))
    }
}






