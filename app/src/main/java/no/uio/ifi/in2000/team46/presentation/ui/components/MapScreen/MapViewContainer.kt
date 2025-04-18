package no.uio.ifi.in2000.team46.presentation.ui.components.MapScreen

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapViewContainer(
    mapView: MapView,
    styleUrl: String,
    modifier: Modifier = Modifier,
    onMapReady: (map: MapLibreMap, ctx: Context) -> Unit
) {
    // Hent Compose‐Context (samme som mapView.context)
    val ctx = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { _ /* context */ ->
            // Hvis mapView allerede ligger et annet sted i view‑hierarkiet, fjern det fra gamla parent:
            (mapView.parent as? ViewGroup)?.removeView(mapView)
            // Returner den hoistede MapView
            mapView
        },
        update = { mv: MapView ->
            // Kjør kart‐ready hver gang compose oppdaterer
            mv.getMapAsync { map ->
                // Last stilen én gang når kartet er klart
                map.setStyle(styleUrl) {
                    onMapReady(map, ctx)
                }
            }
        }
    )
}
