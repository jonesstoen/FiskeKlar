package no.uio.ifi.in2000.team46.presentation.map.viewmodel



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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.presentation.map.utils.MapConstants
import no.uio.ifi.in2000.team46.presentation.map.utils.MapController
import no.uio.ifi.in2000.team46.presentation.map.utils.addUserLocationIndicator
import no.uio.ifi.in2000.team46.utils.ais.VesselIconHelper
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.utils.isPointInPolygon
import no.uio.ifi.in2000.team46.domain.metalerts.Feature
import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import no.uio.ifi.in2000.team46.presentation.map.utils.removeMapMarker

//import mapconstants

/**MapViewModel styrer forretningslogikken og tilstanden for kartet.
 *
 * Denne filen inneholder MapViewModel-klassen som håndterer logikken for kartvisningen.
 * Klassen styrer initialisering av kartet , og holder oversikt over
 * kameraets posisjon ved hjelp av LiveData.
 */


/** Hendelser som MapViewModel kan skyte ut til UI (snackbar osv). */
sealed class MapUiEvent {

    data class ShowAlertSnackbar(
                val message: String,
                val feature: Feature
            ) : MapUiEvent()
}


class MapViewModel(
    private val locationRepository: LocationRepository,
    private val metAlertsRepository: MetAlertsRepository
) : ViewModel() {

    // ----- Konstanter og API‑nøkler -----

    // API-nøkkel for MapTiler

    // Returnerer korrekt stil basert på dark/light mode




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
    //to keep track of which areas have been alerted for
    private val alertedFeatureIds = mutableSetOf<String>()

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

    private val _selectedLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val selectedLocation: StateFlow<Pair<Double, Double>?> = _selectedLocation.asStateFlow()

    private var _isLocationExplicitlySelected = MutableStateFlow(false)
    val isLocationExplicitlySelected: StateFlow<Boolean> = _isLocationExplicitlySelected.asStateFlow()

    private val _locationName = MutableStateFlow("Nåværende posisjon")
    val locationName: StateFlow<String> = _locationName.asStateFlow()
    
    // Flag for å holde styr på om initial zoom er utført
    private val _hasPerformedInitialZoom = MutableStateFlow(false)
    val hasPerformedInitialZoom: StateFlow<Boolean> = _hasPerformedInitialZoom.asStateFlow()

    private var weatherUpdateJob: Job? = null
    private var map: MapLibreMap? = null

    fun getStyleUrl(isDarkTheme: Boolean): String {
        val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
        val style = if (isDarkTheme) "streets-v2-dark" else "basic"
        return "https://api.maptiler.com/maps/$style/style.json?key=$apiKey"
    }

    init {
        viewModelScope.launch {
            _locationName.value = "Nåværende posisjon"
            _temperature.value = null
            _weatherSymbol.value = null

            // Hent posisjon og vær umiddelbart ved oppstart
            try {
                val initialLocation = locationRepository.getCurrentLocation()
                initialLocation?.let { location ->
                    _userLocation.value = location
                    val weatherData = weatherService.getWeatherData(location.latitude, location.longitude)
                    _temperature.value = weatherData.temperature
                    _weatherSymbol.value = weatherData.symbolCode
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error getting initial location and weather: ${e.message}")
            }

            // Start polling og hent varsler etter initial oppsett
            startLocationPolling()
            fetchMetAlerts()
        }
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
            // Vent litt før første polling for å unngå konflikt med init
            delay(1000)

            while (isActive) {
                val loc = locationRepository.getCurrentLocation()
                _userLocation.value = loc

                // Alltid oppdater vær basert på brukerens posisjon hvis ingen eksplisitt valgt posisjon
                if (!_isLocationExplicitlySelected.value) {
                    loc?.let { location ->
                        try {
                            val weatherData = weatherService.getWeatherData(location.latitude, location.longitude)
                            _temperature.value = weatherData.temperature
                            _weatherSymbol.value = weatherData.symbolCode
                        } catch (e: Exception) {
                            Log.e("MapViewModel", "Error updating weather: ${e.message}")
                        }
                    }
                }

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
            if (!feature.geometry.type.equals("Polygon", ignoreCase = true)) continue

            // Hent en unik ID for varselet (bruk f.eks. eventAwarenessName eller et felt "id" om tilgjengelig)
            val featureId = feature.properties.eventAwarenessName

            val polygon = extractPolygonCoordinates(feature)
            val inside = isPointInPolygon(location.latitude, location.longitude, polygon)

            if (inside && !alertedFeatureIds.contains(featureId)) {
                // Brukeren har gått inn i området for første gang
                alertedFeatureIds += featureId
                val raw = feature.properties.eventAwarenessName
                val trimmed = raw.replace("fare", "").trim()
                _uiEvents.emit(
                        MapUiEvent.ShowAlertSnackbar(
                                message = "ADVARSEL: Du er i et område med utsatt $trimmed farevarsel",
                                feature = feature
                                    )
                            )
                // Vi kan velge å returnere her hvis vi bare vil varsle for én feature om gangen
                return
            } else if (!inside && alertedFeatureIds.contains(featureId)) {
                // Brukeren har forlatt området — fjern flagget så vi kan varsle neste gang
                alertedFeatureIds -= featureId
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
    fun initializeMap(map: MapLibreMap, context: Context, styleUrl: String) {
        this.map = map
        map.setStyle(styleUrl) { style ->
            try {
                // Sett standard visning med lav zoom som utgangspunkt
                val controller = MapController(map)
                controller.setInitialView(
                    initialLat, initialLon,
                    zoom = 5.0 // Lav zoom før brukerposisjon er tilgjengelig
                )
                
                // Legg til fartøy-ikoner til kartstilen
                VesselIconHelper.addVesselIconsToStyle(context, map.style!!)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error initializing map: ${e.message}")
            }
        }
    }

    /** Henter temperatur og symbolkode for gitt posisjon. */
    fun updateTemperature(lat: Double, lon: Double) {
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
            val currentZoom = map.cameraPosition.zoom
            val targetZoom = if (currentZoom > 7.0) currentZoom else 7.0
            val cameraPosition = CameraUpdateFactory.newLatLngZoom(
                org.maplibre.android.geometry.LatLng(lat, lon),
                targetZoom
            )
            map.animateCamera(cameraPosition, 500) // 500ms animasjonstid
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error zooming: ${e.message}")
        }
    }

    /** Zoom til siste kjente brukerposisjon + markør. */
    fun zoomToUserLocation(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            val loc = locationRepository.getFastLocation()
            loc?.let {
                clearSelectedLocation()  // Nullstill valgt posisjon
                zoomToLocation(map, it.latitude, it.longitude, map.cameraPosition.zoom)
                map.getStyle { style ->
                    removeMapMarker(style)  // Fjern eksisterende markør
                    addUserLocationIndicator(map, style, it.latitude, it.longitude)
                }
            }
        }
    }
    
    /** 
     * Zoom til brukerens posisjon ved initialisering av kartet.
     * Bruker zoom-nivå fra MapConstants.INITIAL_ZOOM.
     */
    fun zoomToUserLocationInitial(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            // Hent brukerens posisjon raskt
            val loc = locationRepository.getFastLocation()
            loc?.let {
                // Nullstill eventuell valgt posisjon
                clearSelectedLocation()
                
                // Zoom til brukerens posisjon med INITIAL_ZOOM fra MapConstants
                val cameraPosition = CameraUpdateFactory.newLatLngZoom(
                    org.maplibre.android.geometry.LatLng(it.latitude, it.longitude),
                    MapConstants.INITIAL_ZOOM
                )
                map.animateCamera(cameraPosition, 1000) // 1000ms animasjonstid
                
                // Legg til posisjonsindikator
                map.getStyle { style ->
                    removeMapMarker(style)
                    addUserLocationIndicator(map, style, it.latitude, it.longitude)
                }
                
                // Oppdater værdata for brukerens posisjon
                updateTemperature(it.latitude, it.longitude)
                
                // Marker at initial zoom er utført
                setInitialZoomPerformed()
            }
        }
    }
    
    /**
     * Markerer at initial zoom er utført.
     * Dette brukes for å unngå at kartet zoomer til brukerens posisjon
     * hver gang brukeren navigerer tilbake til kartet.
     */
    fun setInitialZoomPerformed() {
        _hasPerformedInitialZoom.value = true
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

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _selectedLocation.value = Pair(latitude, longitude)
            _isLocationExplicitlySelected.value = true

            // Avbryt forrige væroppdatering hvis den fortsatt kjører
            weatherUpdateJob?.cancelAndJoin()

            // Start en ny væroppdatering med forsinkelse
            weatherUpdateJob = viewModelScope.launch {
                delay(500) // Vent 500ms før væroppdatering
                updateWeatherForLocation(latitude, longitude)
                updateLocationName(latitude, longitude)
            }
        }
    }

    fun clearSelectedLocation() {
        viewModelScope.launch {
            _selectedLocation.value = null
            _isLocationExplicitlySelected.value = false

            // Oppdater vær basert på brukerens posisjon
            _userLocation.value?.let { location ->
                updateTemperature(location.latitude, location.longitude)
                _locationName.value = "Nåværende posisjon"
            }
        }
    }

    fun isLocationExplicitlySelected(): Boolean {
        return _isLocationExplicitlySelected.value
    }

    fun updateWeatherForLocation(latitude: Double, longitude: Double, explicit: Boolean = true) {
        // Valider koordinater
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            Log.e("MapViewModel", "Ugyldige koordinater: lat=$latitude, lon=$longitude")
            return
        }

        viewModelScope.launch {
            try {
                val weatherData = weatherService.getWeatherData(latitude, longitude)
                _temperature.value = weatherData.temperature
                _weatherSymbol.value = weatherData.symbolCode
                if (explicit) {
                    _selectedLocation.value = Pair(latitude, longitude)
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching weather: ${e.message}")
            }
        }
    }

    suspend fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&addressdetails=1")
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "MetAlerts/1.0")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                val address = jsonObject.optJSONObject("address")
                if (address == null) {
                    return@withContext "Ukjent posisjon"
                }

                val street = address.optString("road", "")
                val city = address.optString("city", "")
                val municipality = address.optString("municipality", "")

                when {
                    street.isNotEmpty() && city.isNotEmpty() -> "$street, $city"
                    street.isNotEmpty() && municipality.isNotEmpty() -> "$street, $municipality"
                    street.isNotEmpty() -> street
                    city.isNotEmpty() -> city
                    municipality.isNotEmpty() -> municipality
                    else -> "Ukjent posisjon"
                }
            }
        } catch (e: Exception) {
            "Ukjent posisjon"
        }
    }

    fun updateLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _locationName.value = getLocationName(latitude, longitude)
        }
    }
}






