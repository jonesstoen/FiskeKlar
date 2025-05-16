package no.uio.ifi.in2000.team46.presentation.map.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

// summary: hosts a map view in a compose layout and applies a style before notifying when map is ready
// main function: ensure mapview is properly attached, styled, and provides map instance via onMapReady callback

@Composable
fun MapViewContainer(
    mapView: MapView,
    styleUrl: String,
    modifier: Modifier = Modifier,
    onMapReady: (map: MapLibreMap, ctx: Context) -> Unit
) {
    // retrieve compose context, same as mapView.context
    val ctx = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { _ /* context */ ->
            // if mapView is attached elsewhere, remove it from old parent viewgroup
            (mapView.parent as? ViewGroup)?.removeView(mapView)
            // return the hoisted mapView for compose to display
            mapView
        },
        update = { mv: MapView ->
            // invoke map ready callback each time compose recomposes
            mv.getMapAsync { map ->
                // apply style once when map is ready
                map.setStyle(styleUrl) {
                    // notify caller with map instance and context
                    onMapReady(map, ctx)
                }
            }
        }
    )
}
