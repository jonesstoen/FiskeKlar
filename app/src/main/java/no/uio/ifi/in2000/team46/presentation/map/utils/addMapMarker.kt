package no.uio.ifi.in2000.team46.presentation.map.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.Property
import no.uio.ifi.in2000.team46.R

private const val MARKER_SOURCE_ID = "selected-location-source"
private const val MARKER_LAYER_ID = "selected-location-layer"
private const val MARKER_ICON_ID = "selected-location-icon"

/**
 * Add a marker on the map on the clicked position.
 * @param map MapLibreMap-objektet
 * @param style Style-objektet
 * @param latitude Breddegrad
 * @param longitude Lengdegrad
 * @param context Context-objektet
 */
fun addMapMarker(
    map: MapLibreMap,
    style: Style,
    latitude: Double,
    longitude: Double,
    context: Context
) {
    try {
        // Sjekk om markøren allerede er initialisert
        if (style.getLayer(MARKER_LAYER_ID) == null) {
            // Første gang: Opprett markør
            val drawable = ContextCompat.getDrawable(context, R.drawable.map_marker)
            val bitmap = drawable?.toBitmap()
            if (bitmap != null) {
                style.addImage(MARKER_ICON_ID, bitmap)
                
                // Opprett GeoJsonSource
                val source = GeoJsonSource(MARKER_SOURCE_ID)
                style.addSource(source)

                // Opprett SymbolLayer med optimaliserte egenskaper
                val layer = SymbolLayer(MARKER_LAYER_ID, MARKER_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(MARKER_ICON_ID),
                        PropertyFactory.iconSize(0.05f),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.symbolSortKey(1.0f),
                        PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                        PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)
                    )
                style.addLayer(layer)
            }
        }
        
        // Oppdater bare posisjonen hvis laget allerede eksisterer
        val source = style.getSource(MARKER_SOURCE_ID) as? GeoJsonSource
        source?.setGeoJson(createPointFeature(latitude, longitude))
    } catch (e: Exception) {
        Log.e("MapMarker", "Feil ved oppretting/oppdatering av markør: ${e.message}")
    }
}

/**
 * Fjern markøren fra kartet
 */
fun removeMapMarker(style: Style) {
    try {
        style.getLayer(MARKER_LAYER_ID)?.let { style.removeLayer(it) }
        style.getSource(MARKER_SOURCE_ID)?.let { style.removeSource(it) }
    } catch (e: Exception) {
        Log.e("MapMarker", "Feil ved fjerning av markør: ${e.message}")
    }
}

private fun initializeMarker(style: Style, context: Context) {
    // Last inn ikon
    val drawable = ContextCompat.getDrawable(context, R.drawable.map_marker)
    val bitmap = drawable?.toBitmap()
    if (bitmap != null) {
        style.addImage(MARKER_ICON_ID, bitmap)
        
        // Opprett GeoJsonSource
        val source = GeoJsonSource(MARKER_SOURCE_ID)
        style.addSource(source)

        // Opprett SymbolLayer med optimaliserte egenskaper
        val layer = SymbolLayer(MARKER_LAYER_ID, MARKER_SOURCE_ID)
            .withProperties(
                PropertyFactory.iconImage(MARKER_ICON_ID),
                PropertyFactory.iconSize(0.05f),
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.symbolSortKey(1.0f),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)
            )
        style.addLayer(layer)
    }
}

private fun updateMarkerPosition(style: Style, latitude: Double, longitude: Double) {
    val source = style.getSource(MARKER_SOURCE_ID) as? GeoJsonSource
    source?.setGeoJson(createPointFeature(latitude, longitude))
}

private fun createPointFeature(latitude: Double, longitude: Double): String {
    return """
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [$longitude, $latitude]
            }
        }
    """.trimIndent()
}

private fun Drawable.toBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        return bitmap
    }

    val scale = 0.4f
    val width = (intrinsicWidth * scale).toInt()
    val height = (intrinsicHeight * scale).toInt()
    
    return try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        bitmap
    } catch (e: Exception) {
        Log.e("MapMarker", "Feil ved konvertering av drawable til bitmap: ${e.message}", e)
        null
    }
}
