package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class MapViewModel : ViewModel() {
    // Startverdier for kartet
    private val initialLat: Double = MapConstants.INITIAL_LAT
    private val initialLon: Double = MapConstants.INITIAL_LON
    private val initialZoom: Double = MapConstants.INITIAL_ZOOM


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
                // Setter den initiale visningen basert på startverdiene.
                controller.setInitialView(initialLat, initialLon, initialZoom)
                // Oppdaterer LiveData med den nye kamera-posisjonen slik at UI kan reagere på endringen.
                _cameraPosition.value = LatLng(initialLat, initialLon)
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



}