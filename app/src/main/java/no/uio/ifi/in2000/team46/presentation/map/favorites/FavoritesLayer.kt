package no.uio.ifi.in2000.team46.presentation.map.favorites

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import no.uio.ifi.in2000.team46.R
import androidx.core.graphics.toColorInt
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.textFont

/**
 * Komponent som viser favorittområder og -punkter på kartet.
 */
@Composable
fun FavoritesLayer(
    mapView: MapView,
    viewModel: FavoritesLayerViewModel
) {
    val TAG = "FavoritesLayer"
    val favorites by viewModel.favorites.collectAsState()
    val isLayerVisible by viewModel.isLayerVisible.collectAsState()
    val selectedFavorite by viewModel.selectedFavorite.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    // Oppdater laget når favorittene eller synligheten endres
    LaunchedEffect(favorites, isLayerVisible) {
        if (isLayerVisible) {
            updateFavoritesLayer(
                mapView, viewModel,
                primaryColorInt = primaryColor
            )
        } else {
            removeFavoritesLayer(mapView)
        }
    }

    // Håndter klikk på favoritter
    LaunchedEffect(Unit) {
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.addOnMapClickListener { point ->
                val screenPoint = maplibreMap.projection.toScreenLocation(point)
                
                // Sjekk om klikket traff et punkt
                val pointFeatures = maplibreMap.queryRenderedFeatures(
                    PointF(screenPoint.x, screenPoint.y),
                    "favorites-points-layer"
                )
                
                // Sjekk om klikket traff et område
                val areaFeatures = maplibreMap.queryRenderedFeatures(
                    PointF(screenPoint.x, screenPoint.y),
                    "favorites-areas-layer"
                )
                
                val features = pointFeatures + areaFeatures
                
                if (features.isNotEmpty()) {
                    val feature = features[0]
                    val properties = feature.properties()
                    
                    val favoriteId = properties?.get("id")?.asInt
                    favoriteId?.let { id ->
                        val favorite = favorites.find { it.id == id }
                        favorite?.let {
                            viewModel.selectFavorite(it)
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    // Vis detaljer for valgt favoritt
    if (selectedFavorite != null) {
        FavoriteInfoDialog(
            favorite = selectedFavorite!!,
            onDismiss = { viewModel.selectFavorite(null) }
        )
    }
}

/**
 * Dialog som viser detaljer om en favorittlokasjon.
 */
@Composable
fun FavoriteInfoDialog(
    favorite: FavoriteLocation,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(favorite.name) },
        text = {
            Column {
                Text("Type: ${if (favorite.locationType == "POINT") "Punkt" else "Område"}")
                if (favorite.notes != null && favorite.notes!!.isNotEmpty()) {
                    Text(
                        "Notater: ${favorite.notes}",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Lukk")
            }
        }
    )
}

/**
 * Oppdaterer favorittlaget på kartet.
 */
private fun updateFavoritesLayer(mapView: MapView, viewModel: FavoritesLayerViewModel, primaryColorInt: Int) {
    val TAG = "FavoritesLayer"

    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                // Fjern eksisterende lag først
                removeFavoritesLayer(mapView)
                
                // Last inn favoritt-markør fra drawable-mappen
                val context = mapView.context
                try {
                    // Bruk en direkte referanse til ressurs-ID-en
                    val resourceId = R.drawable.favorite_marker
                    
                    // Fjern bildet først hvis det allerede finnes
                    if (style.getImage("favorite_marker") != null) {
                        style.removeImage("favorite_marker")
                    }
                    
                    // Last inn bildet på nytt
                    val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                    if (bitmap != null) {
                        style.addImage("favorite_marker", bitmap)
                        Log.d(TAG, "Favoritt-markør lastet inn, størrelse: ${bitmap.width}x${bitmap.height}")
                    } else {
                        Log.e(TAG, "Kunne ikke dekode bitmap for favoritt-markør")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Feil ved lasting av favoritt-markør: ${e.message}")
                }
                
                // Legg til GeoJSON-kilde
                val sourceId = "favorites-source"
                val geoJson = viewModel.getFavoritesGeoJson()
                val source = GeoJsonSource(sourceId, geoJson)
                style.addSource(source)
                
                // Legg til lag for områder (polygoner)
                val areaFillLayer = FillLayer("favorites-areas-layer", sourceId)
                    .withFilter(
                        Expression.eq(
                        Expression.get("type"),
                        Expression.literal("AREA")
                    ))
                    .withProperties(
                        PropertyFactory.fillColor("#4CAF50".toColorInt()),
                        PropertyFactory.fillOpacity(0.3f)
                    )
                style.addLayer(areaFillLayer)
                
                // Legg til omriss for områder
                val areaLineLayer = LineLayer("favorites-areas-line-layer", sourceId)
                    .withFilter(
                        Expression.eq(
                        Expression.get("type"),
                        Expression.literal("AREA")
                    ))
                    .withProperties(
                        PropertyFactory.lineColor("#4CAF50".toColorInt()),
                        PropertyFactory.lineWidth(2f),
                        PropertyFactory.lineOpacity(0.8f)
                    )
                style.addLayer(areaLineLayer)
                
                // Legg til lag for punkter
                val pointsLayer = SymbolLayer("favorites-points-layer", sourceId)
                    .withFilter(
                        Expression.eq(
                        Expression.get("type"),
                        Expression.literal("POINT")
                    ))
                    .withProperties(
                        PropertyFactory.iconImage("favorite_marker"),
                        PropertyFactory.iconSize(0.03f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAnchor("center")  // Anker ikonet i sentrum
                    )
                style.addLayer(pointsLayer)
                
                // Legg til tekstlag for punkter
                val pointTextLayer = SymbolLayer("favorites-points-text-layer", sourceId)
                    .withFilter(
                        Expression.eq(
                            Expression.get("type"),
                            Expression.literal("POINT")
                        )
                    )
                    .withProperties(
                        PropertyFactory.textField(Expression.get("name")),
                        PropertyFactory.textSize(18f),
                        PropertyFactory.textColor(primaryColorInt),
                        textFont(arrayOf("Arial Bold")),
                        PropertyFactory.textOffset(arrayOf(0f, 0.5f)),  // Økt Y-offset for å flytte teksten lengre ned
                        PropertyFactory.textAllowOverlap(false),
                        PropertyFactory.textAnchor("top"),  // Anker teksten i toppen for punkter
                    )
                pointTextLayer.minZoom = 9.0f
                style.addLayer(pointTextLayer)
                
                // Legg til tekstlag for områder
                val areaTextLayer = SymbolLayer("favorites-areas-text-layer", sourceId)
                    .withFilter(
                        Expression.eq(
                            Expression.get("type"),
                            Expression.literal("AREA")
                        )
                    )
                    .withProperties(
                        PropertyFactory.textField(Expression.get("name")),
                        PropertyFactory.textSize(18f),
                        PropertyFactory.textColor(primaryColorInt),
                        textFont(arrayOf("Arial Bold")),
                        PropertyFactory.textOffset(arrayOf(0f, 0f)),  // Ingen offset for områder
                        PropertyFactory.textAllowOverlap(false),
                        PropertyFactory.textAnchor("center"),  // Anker teksten i sentrum for områder

                    )
                areaTextLayer.minZoom = 9.0f
                style.addLayer(areaTextLayer)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorites layer", e)
            }
        }
    }
}

/**
 * Fjerner favorittlaget fra kartet.
 */
private fun removeFavoritesLayer(mapView: MapView) {
    val TAG = "FavoritesLayer"
    
    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                // Fjern tekstlag
                style.getLayer("favorites-points-text-layer")?.let { style.removeLayer(it) }
                style.getLayer("favorites-areas-text-layer")?.let { style.removeLayer(it) }
                
                // Fjern punktlag
                style.getLayer("favorites-points-layer")?.let { style.removeLayer(it) }
                
                // Fjern områdelag
                style.getLayer("favorites-areas-line-layer")?.let { style.removeLayer(it) }
                style.getLayer("favorites-areas-layer")?.let { style.removeLayer(it) }
                
                // Fjern kilden
                style.getSource("favorites-source")?.let { style.removeSource(it) }
                
                Log.d(TAG, "Favorittlag fjernet")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing favorites layer", e)
            }
        }
    }
}
