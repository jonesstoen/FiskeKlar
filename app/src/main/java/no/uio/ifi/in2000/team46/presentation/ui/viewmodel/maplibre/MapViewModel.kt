package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.map.MapConstants
import no.uio.ifi.in2000.team46.map.MapController
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/**MapViewModel styrer forretningslogikken og tilstanden for kartet.
 *
 * Denne filen inneholder MapViewModel-klassen som håndterer logikken for kartvisningen.
 * Klassen styrer initialisering av kartet , og holder oversikt over
 * kameraets posisjon ved hjelp av LiveData.
 */

class MapViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    // Startverdier for kartet
    private val initialLat: Double = MapConstants.INITIAL_LAT
    private val initialLon: Double = MapConstants.INITIAL_LON
    private val initialZoom: Double = MapConstants.INITIAL_ZOOM

    //for fetching the user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val _userLocation = MutableLiveData<Location?>()
    val userLocation: LiveData<Location?> = _userLocation


    private val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
    //url for å hente kartstilen kan endres til ønsket stil
    val styleUrl: String = "https://api.maptiler.com/maps/basic/style.json?key=$apiKey"

    // State for kamera-posisjon, livedate slik at andre komponenter kan observere endringer
    private val _cameraPosition = MutableLiveData<LatLng>()
    val cameraPosition: LiveData<LatLng> = _cameraPosition
    /**
     * funksjonen tar inn ett maplibre kart og :
     * setter kartets stil til styleUrl
     * bruker mapController til å sette initial view
     * oppdaterer kamera posisjonen
     */
    fun initializeMap(map: MapLibreMap) {
        map.setStyle(styleUrl) { style ->
            try {
                // Oppretter en MapController for å håndtere lavnivå-operasjoner på kartet.
                val controller = MapController(map)
                val lat = userLocation.value?.latitude ?: initialLat
                val lon = userLocation.value?.longitude ?: initialLon
                val zoom = userLocation.value?.let { 10.0 } ?: initialZoom
                // Setter den initiale visningen basert på startverdiene.
                controller.setInitialView(lat, lon, zoom)
                // Oppdaterer LiveData med den nye kamera-posisjonen slik at UI kan reagere på endringen.
                _cameraPosition.value = LatLng(lat, lon)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error initializing map: ${e.message}")
            }
        }
    }

    fun zoomToLocation(map: MapLibreMap, lat: Double, lon: Double, zoom: Double = 10.0){
        try {
            val controller = MapController(map)
            controller.zoomToLocation(lat, lon, zoom)
            _cameraPosition.value = LatLng(lat, lon)
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error zooming to location: ${e.message}")
        }

    }
    fun zoomToUserLocation(map: MapLibreMap, context: Context) {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            location?.let {
                zoomToLocation(map, it.latitude, it.longitude,20.0)
            }
        }
    }

    fun fetchUserLocation(context: Context) {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            _userLocation.value = location
        }
    }





}