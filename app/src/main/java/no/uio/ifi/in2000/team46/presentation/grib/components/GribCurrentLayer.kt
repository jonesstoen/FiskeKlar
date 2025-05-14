package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.SymbolLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation

// this component renders the current vector layer on the map using maplibre
// it shows current arrows using icons and allows user to tap for details via dialog

@Composable
fun GribCurrentLayer(
    currentViewModel: CurrentViewModel,
    map: MapLibreMap,
    mapView: MapView,
    filterVectors: Boolean = false,
    filterStep: Int = 3
) {
    val isVisible by currentViewModel.isLayerVisible.collectAsState()
    val filteredVectors by currentViewModel.filteredCurrentVectors.collectAsState()
    val threshold by currentViewModel.currentThreshold.collectAsState()

    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    LaunchedEffect(isVisible, filteredVectors, threshold) {
        if (!isVisible) {
            // remove layer and source if not visible
            map.getStyle { style ->
                style.removeLayer("current_layer")
                style.removeSource("current_source")
            }
            return@LaunchedEffect
        }

        map.getStyle { style ->
            val sourceId = "current_source"
            val layerId = "current_layer"
            val normalIconName = "current_icon"
            val redIconName = "current_icon_red"

            // add normal and red icons to the style if missing
            if (style.getImage(normalIconName) == null) {
                val normalBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon)
                style.addImage(normalIconName, normalBitmap, false)
            }
            if (style.getImage(redIconName) == null) {
                val redBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon_red)
                style.addImage(redIconName, redBitmap, false)
            }

            // apply optional filtering to reduce vector density
            val filteredData = if (filterVectors) {
                filteredVectors.filterIndexed { index, _ -> index % filterStep == 0 }
            } else filteredVectors

            // convert vectors to geojson features with properties
            val features = filteredData.mapNotNull { v ->
                Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                    addNumberProperty("direction", v.direction)
                    addNumberProperty("speed", v.speed)
                    val iconName = if (v.speed > threshold) redIconName else normalIconName
                    addStringProperty("icon", iconName)
                }
            }

            val featureCollection = FeatureCollection.fromFeatures(features)

            // update or add source
            style.getSourceAs<GeoJsonSource>(sourceId)?.setGeoJson(featureCollection)
                ?: style.addSource(GeoJsonSource(sourceId, featureCollection))

            // add layer if it doesn't exist
            if (style.getLayer(layerId) == null) {
                style.addLayer(
                    SymbolLayer(layerId, sourceId).withProperties(
                        iconImage(get("icon")),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconRotationAlignment("map"),
                        iconRotate(get("direction")),
                        iconSize(
                            interpolate(
                                linear(), zoom(),
                                stop(0.0, 0.10),
                                stop(8.0, 0.20),
                                stop(12.0, 0.30),
                                stop(16.0, 0.40),
                                stop(20.0, 0.50)
                            )
                        )
                    )
                )
            }

            // listen for user map clicks to show vector info
            map.addOnMapClickListener { point ->
                val screenPoint = map.projection.toScreenLocation(point)
                val featuresAtClick = map.queryRenderedFeatures(screenPoint, layerId)
                selectedFeature = featuresAtClick.firstOrNull()
                selectedFeature != null
            }
        }
    }

    // show dialog with speed and direction if a vector is selected
    selectedFeature?.let { feature ->
        val speed = feature.getNumberProperty("speed")?.toDouble() ?: 0.0
        val direction = feature.getNumberProperty("direction")?.toDouble() ?: 0.0

        CurrentVectorDialog(
            onDismiss = { selectedFeature = null },
            speed = speed,
            direction = direction
        )
    }
}

@Composable
fun CurrentVectorDialog(
    onDismiss: () -> Unit,
    speed: Double,
    direction: Double
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 240.dp, max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "direction",
                        modifier = Modifier
                            .rotate(direction.toFloat())
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Strømvektor",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // show speed and direction text
                Text("Fart: %.2f knop".format(speed), style = MaterialTheme.typography.bodyLarge)
                Text("Retning: %.0f°".format(direction), style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))

                // close button
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Lukk")
                }
            }
        }
    }
}
