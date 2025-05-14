package no.uio.ifi.in2000.team46.presentation.map.utils

import android.util.Log
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

// summary: provides methods to control map view, such as setting initial camera position and zoom

class MapController(private val map: MapLibreMap) {

    // set initial view of map by moving and zooming camera to given coordinates
    fun setInitialView(lat: Double, lon: Double, zoom: Double) {
        try {
            // create latlng object with given latitude and longitude
            val latLng = LatLng(lat, lon)
            // create camera update with target latlng and zoom level
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            // animate camera to new position and zoom on map
            map.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            // log error if initial view setting fails
            Log.e("MapController", "Error setting initial view: ${e.message}")
        }
    }

    // move and zoom camera to specified location
    fun zoomToLocation(lat: Double, lon: Double, zoom: Double) {
        try {
            // create latlng object with provided coordinates
            val latLng = LatLng(lat, lon)
            // build camera update for given position and zoom
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
            // animate camera to specified location
            map.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            // log error if zoom operation fails
            Log.e("MapController", "Error zooming to location: ${e.message}")
        }
    }

}
