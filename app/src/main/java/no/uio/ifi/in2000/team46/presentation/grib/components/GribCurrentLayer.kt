package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.match
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.style.layers.SymbolLayer
//popouen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun GribCurrentLayer(
    currentViewModel: CurrentViewModel,
    map: MapLibreMap,
    mapView: MapView,
    filterVectors: Boolean = false,
    filterStep: Int = 3
) {
    val isVisible by currentViewModel.isLayerVisible.collectAsState()
    val currentResult by currentViewModel.currentData.collectAsState()
    val threshold by currentViewModel.currentThreshold.collectAsState()

    // Popup state for valgt feature
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    if (isVisible && currentResult is Result.Success) {
        val currentData = (currentResult as Result.Success<List<CurrentVector>>).data

        LaunchedEffect(currentData, threshold) {
            map.getStyle { style ->
                val sourceId = "current_source"
                val layerId = "current_layer"

                val normalIconName = "current_icon"
                val redIconName = "current_icon_red"

                val normalBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon)
                val redBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon_red)

                if (style.getImage(normalIconName) == null) style.addImage(normalIconName, normalBitmap, false)
                if (style.getImage(redIconName) == null) style.addImage(redIconName, redBitmap, false)

                val filteredData = if (filterVectors) {
                    currentData.filterIndexed { index, _ -> index % filterStep == 0 }
                } else currentData

                val features = filteredData.mapNotNull { v ->
                    Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                        addNumberProperty("direction", v.direction)
                        addNumberProperty("speed", v.speed)
                        val iconName = if (v.speed > threshold) redIconName else normalIconName
                        addStringProperty("icon", iconName)
                    }
                }

                val featureCollection = FeatureCollection.fromFeatures(features)
                style.getSourceAs<GeoJsonSource>(sourceId)?.setGeoJson(featureCollection)
                    ?: style.addSource(GeoJsonSource(sourceId, featureCollection))

                if (style.getLayer(layerId) == null) {
                    style.addLayer(
                        SymbolLayer(layerId, sourceId).withProperties(
                            iconImage(get("icon")),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconRotationAlignment("map"),
                            iconRotate(get("direction")),
                            iconSize(
                                Expression.interpolate(
                                    Expression.linear(), Expression.zoom(),
                                    Expression.stop(0.0, 0.10),
                                    Expression.stop(8.0, 0.20),
                                    Expression.stop(12.0, 0.30),
                                    Expression.stop(16.0, 0.40),
                                    Expression.stop(20.0, 0.50)
                                )
                            )
                        )
                    )
                }

                map.addOnMapClickListener { point ->
                    val screenPoint = map.projection.toScreenLocation(point)
                    val featuresAtClick = map.queryRenderedFeatures(screenPoint, layerId)
                    selectedFeature = featuresAtClick.firstOrNull()
                    selectedFeature != null
                }
            }
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("current_layer")?.let { style.removeLayer(it) }
                style.getSource("current_source")?.let { style.removeSource(it) }
            }
        }
    }

    // Popup-dialog når en strømvektor er valgt
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
                        contentDescription = "Retning",
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

                Text(
                    text = "Fart: %.2f knop".format(speed),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Retning: %.0f°".format(direction),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Lukk")
                }
            }
        }
    }
}

