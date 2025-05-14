package no.uio.ifi.in2000.team46.presentation.map.favorites

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
 *composable that renders favorite locations on map and shows info dialog when tapped
 * main function: observes favorites and visibility state, updates or removes map layers, handles user taps, and displays dialog
 *
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

    //update layer when favorites or visibility changes
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

    // handles click events on the map
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

    // detail dialog for selected favorite
    if (selectedFavorite != null) {
        FavoriteInfoDialog(
            favorite = selectedFavorite!!,
            onDismiss = { viewModel.selectFavorite(null) }
        )
    }
}

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
                if (!favorite.notes.isNullOrEmpty()) {
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
 * updates the favorites layer on the map
 */
private fun updateFavoritesLayer(mapView: MapView, viewModel: FavoritesLayerViewModel, primaryColorInt: Int) {
    val TAG = "FavoritesLayer"

    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                // remove layer if it already exists
                removeFavoritesLayer(mapView)

                // load marker image
                val context = mapView.context
                try {

                    val resourceId = R.drawable.favorite_marker

                    if (style.getImage("favorite_marker") != null) {
                        style.removeImage("favorite_marker")
                    }
                    val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                    if (bitmap != null) {
                        style.addImage("favorite_marker", bitmap)
                        //Log.d(TAG, "Favoritt-markør lastet inn, størrelse: ${bitmap.width}x${bitmap.height}")
                    } else {
                        //Log.e(TAG, "Kunne ikke dekode bitmap for favoritt-markør")
                    }
                } catch (e: Exception) {
                    //Log.e(TAG, "Feil ved lasting av favoritt-markør: ${e.message}")
                }

                // adding the geojson source
                val sourceId = "favorites-source"
                val geoJson = viewModel.getFavoritesGeoJson()
                val source = GeoJsonSource(sourceId, geoJson)
                style.addSource(source)

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
                        PropertyFactory.textOffset(arrayOf(0f, 0.5f)),
                        PropertyFactory.textAllowOverlap(false),
                        PropertyFactory.textAnchor("top"),
                    )
                pointTextLayer.minZoom = 9.0f
                style.addLayer(pointTextLayer)

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
                        PropertyFactory.textOffset(arrayOf(0f, 0f)),
                        PropertyFactory.textAllowOverlap(false),
                        PropertyFactory.textAnchor("center"),

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
 * removdes the favorites layer from the map
 */
private fun removeFavoritesLayer(mapView: MapView) {
    val TAG = "FavoritesLayer"

    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                style.getLayer("favorites-points-text-layer")?.let { style.removeLayer(it) }
                style.getLayer("favorites-areas-text-layer")?.let { style.removeLayer(it) }

                style.getLayer("favorites-points-layer")?.let { style.removeLayer(it) }

                style.getLayer("favorites-areas-line-layer")?.let { style.removeLayer(it) }
                style.getLayer("favorites-areas-layer")?.let { style.removeLayer(it) }

                style.getSource("favorites-source")?.let { style.removeSource(it) }

                Log.d(TAG, "Favorittlag fjernet")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing favorites layer", e)
            }
        }
    }
}
