package no.uio.ifi.in2000.team46.map

import android.util.Log
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/** MapController utfører konkrete operasjoner på kartet, som å sette visningen og zoome inn.
 *
 * Denne filen inneholder MapController-klassen som er ansvarlig for å utføre
 * lavnivå-operasjoner på kartet, slik som å sette initial visning.
 * Klassen abstraherer direkte operasjoner mot MapLibreMap.
 */

class MapController(private val map: MapLibreMap) {

    /*
    * funksjonen opretter latlng objekt med gitte kordinater, for så å lage en cameraupdate,
    *  med ønsket zoomnivå og animerer deretter kameraet til den nye posisjonen*/

    fun setInitialView(lat: Double, lon: Double, zoom: Double) {
        try {
            // Oppretter et LatLng-objekt basert på latitude og longitude.
            val latLng = LatLng(lat, lon)
            // Lager et CameraUpdate-objekt med den nye posisjonen og zoom-nivået.
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            // Animerer kameraet til den nye posisjonen på kartet.
            map.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            Log.e("MapController", "Error setting initial view: ${e.message}")
        }
    }

    fun zoomToLocation(lat: Double, lon: Double, zoom: Double) {
        try {
            val latLng = LatLng(lat, lon)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            map.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            Log.e("MapController", "Error zooming to location: ${e.message}")
        }
    }



}