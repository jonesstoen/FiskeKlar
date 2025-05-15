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



/**
 * mapviewmodel manages business logic and state for the map
 *
 * this file contains the mapviewmodel class which handles logic for map view
 * class controls map initialization and tracks camera position using livedata
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

    private val initialLat: Double = MapConstants.INITIAL_LAT
    private val initialLon: Double = MapConstants.INITIAL_LON

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _temperature = MutableStateFlow<Double?>(null)
    val temperature: StateFlow<Double?> = _temperature

    private val _weatherSymbol = MutableStateFlow<String?>(null)
    val weatherSymbol: StateFlow<String?> = _weatherSymbol

    // to keep track of the metalerts response
    private val _metAlertsResponse = MutableStateFlow<MetAlertsResponse?>(null)
    val metAlertsResponse: StateFlow<MetAlertsResponse?> = _metAlertsResponse
    //to keep track of which areas have been alerted for
    private val alertedFeatureIds = mutableSetOf<String>()

    // events to be sent to the UI
    private val _uiEvents = MutableSharedFlow<MapUiEvent>()
    val uiEvents: SharedFlow<MapUiEvent> = _uiEvents


    private val weatherService = WeatherService()

    private val _selectedFeature = MutableStateFlow<Feature?>(null)
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
    
    // to keep track of whether the initial zoom has been performed
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

            // fetching the initial location and weather data
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
            startLocationPolling()
            fetchMetAlerts()
        }
    }

    // fetching the metalerts from the repository and filtering out the land related warnings
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

    // polling the location every 5 seconds and updating the temperature every 5 seconds
    private fun startLocationPolling() {
        viewModelScope.launch {
            // waiting to avoid  conflictwith initialization of the map
            delay(1000)

            while (isActive) {
                val loc = locationRepository.getCurrentLocation()
                _userLocation.value = loc

                // always updating the temperature for the current location
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

                checkForPolygons(loc)

                delay(5_000)
            }
        }
    }

    // checkong if the user is inside a polygon, from the metalerts response
    private suspend fun checkForPolygons(location: Location?) {
        val alerts = _metAlertsResponse.value ?: return
        if (location == null) return

        for (feature in alerts.features) {
            if (!feature.geometry.type.equals("Polygon", ignoreCase = true)) continue


            val featureId = feature.properties.eventAwarenessName

            val polygon = extractPolygonCoordinates(feature)
            val inside = isPointInPolygon(location.latitude, location.longitude, polygon)

            if (inside && !alertedFeatureIds.contains(featureId)) {
                // user entered the polygon, and we can show the alert
                alertedFeatureIds += featureId
                val raw = feature.properties.eventAwarenessName
                val trimmed = raw.replace("fare", "").trim()
                _uiEvents.emit(
                        MapUiEvent.ShowAlertSnackbar(
                                message = "ADVARSEL: Du er i et område med utsatt $trimmed farevarsel",
                                feature = feature
                                    )
                            )
                return
            } else if (!inside && alertedFeatureIds.contains(featureId)) {
                // user has left the polygon, and we can remove the alert
                alertedFeatureIds -= featureId
            }
        }
    }

    //extracts the coordinates of the polygon from the feature
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

    // initializes the map with a given style
    fun initializeMap(map: MapLibreMap, context: Context, styleUrl: String) {
        this.map = map
        map.setStyle(styleUrl) {
            try {
                val controller = MapController(map)
                controller.setInitialView(
                    initialLat, initialLon,
                    zoom = 5.0
                )
                
                // adding vessel icons to the map
                VesselIconHelper.addVesselIconsToStyle(context, map.style!!)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error initializing map: ${e.message}")
            }
        }
    }

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

    // when the user clicks on a metalert, the bottom sheet is expanded
    @OptIn(ExperimentalMaterial3Api::class)
    fun selectMetAlert(feature: Feature?) {
        _selectedFeature.value = feature
        if (feature != null) {
            _bottomSheetState.value = SheetValue.Expanded  // expand bottom sheet
        } else {
            _bottomSheetState.value = SheetValue.Hidden  // hide bottom sheet
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun resetMetAlertSelectionIfNeeded(state: SheetValue) {
        // if bottom sheet is not expanded, reset selected feature
        if (state != SheetValue.Expanded) {
            _selectedFeature.value = null  // Reset selected feature
        }
    }

    //zoom to specified location
    fun zoomToLocation(map: MapLibreMap, lat: Double, lon: Double, zoom: Double) {
        try {
            val currentZoom = map.cameraPosition.zoom
            val targetZoom = if (currentZoom > 7.0) currentZoom else 7.0
            val cameraPosition = CameraUpdateFactory.newLatLngZoom(
                org.maplibre.android.geometry.LatLng(lat, lon),
                targetZoom
            )
            map.animateCamera(cameraPosition, 500)
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error zooming: ${e.message}")
        }
    }

    fun zoomToUserLocation(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            val loc = locationRepository.getFastLocation()
            loc?.let {
                clearSelectedLocation()
                zoomToLocation(map, it.latitude, it.longitude, map.cameraPosition.zoom)
                map.getStyle { style ->
                    removeMapMarker(style)
                    addUserLocationIndicator(map, style, it.latitude, it.longitude)
                }
            }
        }
    }
    
    //zooming to user location when the app is first opened
    fun zoomToUserLocationInitial(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            // get the last known location, form memory if available
            val loc = locationRepository.getFastLocation()
            loc?.let {

                clearSelectedLocation()
                
                // zooming to initial zoomm levle
                val cameraPosition = CameraUpdateFactory.newLatLngZoom(
                    org.maplibre.android.geometry.LatLng(it.latitude, it.longitude),
                    MapConstants.INITIAL_ZOOM
                )
                map.animateCamera(cameraPosition, 1000) // 1000ms animasjonstid
                
                // adding user location indicator
                map.getStyle { style ->
                    removeMapMarker(style)
                    addUserLocationIndicator(map, style, it.latitude, it.longitude)
                }
                
                // updating temperature to current location
                updateTemperature(it.latitude, it.longitude)

                setInitialZoomPerformed()
            }
        }
    }
    

    //marks that initial zoom has been performed, in order to avoid zooming to user location all the time
    fun setInitialZoomPerformed() {
        _hasPerformedInitialZoom.value = true
    }

    //zoom in and out
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

            // cancel any existing weather update job
            weatherUpdateJob?.cancelAndJoin()

            // start a new weather update job
            weatherUpdateJob = viewModelScope.launch {
                delay(500) // wait before updating
                updateWeatherForLocation(latitude, longitude)
                updateLocationName(latitude, longitude)
            }
        }
    }

    fun clearSelectedLocation() {
        viewModelScope.launch {
            _selectedLocation.value = null
            _isLocationExplicitlySelected.value = false
            // updating weather for current location
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
        // Validate coordinates
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

                val address = jsonObject.optJSONObject("address") ?: return@withContext "Ukjent posisjon"

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
