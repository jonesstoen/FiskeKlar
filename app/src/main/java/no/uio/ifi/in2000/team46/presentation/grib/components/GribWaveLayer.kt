package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.local.parser.WaveVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.WaveViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.PropertyFactory.rasterOpacity
import org.maplibre.android.style.layers.PropertyFactory.rasterResampling
import org.maplibre.android.style.sources.ImageSource

@Composable
fun GribWaveLayer(
    waveViewModel: WaveViewModel,
    map: MapLibreMap,
    mapView: MapView
) {
    val isVisible     by waveViewModel.isLayerVisible.collectAsState()
    val waveResult    by waveViewModel.waveData.collectAsState()
    // Bitmap + grid-data som brukes for ImageSource
    var rasterBitmap  by remember { mutableStateOf<Bitmap?>(null) }
    var gridData      by remember { mutableStateOf<GridData?>(null) }

    LaunchedEffect(isVisible, waveResult) {
        // Skjul / fjern hvis laget ikke skal vises eller vi ikke har data
        if (!isVisible || waveResult !is Result.Success) {
            // Sørg for å slå av loading
            waveViewModel.setRasterLoading(false)
            rasterBitmap = null
            gridData     = null
            return@LaunchedEffect
        }

        // Start loading
        waveViewModel.setRasterLoading(true)

        try {
            val waves = (waveResult as Result.Success<List<WaveVector>>).data

            withContext(Dispatchers.Default) {
                // --- bygg grid ---
                val latList = waves.map { it.lat }.distinct().sorted()
                val lonList = waves.map { it.lon }.distinct().sorted()
                val grid    = Array(latList.size) { DoubleArray(lonList.size) }
                waves.forEach { w ->
                    val i = latList.indexOf(w.lat)
                    val j = lonList.indexOf(w.lon)
                    grid[i][j] = w.height
                }
                gridData = GridData(
                    latArray = latList,
                    lonArray = lonList,
                    grid     = grid,
                    latMin   = latList.first(),
                    latMax   = latList.last(),
                    lonMin   = lonList.first(),
                    lonMax   = lonList.last()
                )

                // --- lag bitmap ---
                val size = 1024
                val bmp  = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val heights = waves.map { it.height }
                val minH    = heights.minOrNull() ?: 0.0
                val maxH    = heights.maxOrNull() ?: 1.0

                for (y in 0 until size) {
                    val ty  = y.toDouble() / (size - 1)
                    val lat = lerp(gridData!!.latMax, gridData!!.latMin, ty)
                    for (x in 0 until size) {
                        val tx  = x.toDouble() / (size - 1)
                        val lon = lerp(gridData!!.lonMin, gridData!!.lonMax, tx)
                        val h   = bilinearHeight(lon, lat, grid, lonList, latList)
                        val t   = ((h - minH) / (maxH - minH)).coerceIn(0.0, 1.0)
                        bmp.setPixel(x, y, interpolateColor(t))
                    }
                }
                rasterBitmap = bmp
            }

        } finally {
            // Alltid stopp loading når jobben er gjort
            waveViewModel.setRasterLoading(false)
        }
    }

    LaunchedEffect(rasterBitmap, gridData) {
        map.getStyle { style ->
            val srcId   = "wave_raster_source"
            val layerId = "wave_raster_layer"

            if (rasterBitmap != null && gridData != null) {
                // Fjern gamle kilder/lag
                style.getLayer(layerId)?.let { style.removeLayer(it) }
                style.getSource(srcId)?.let { style.removeSource(it) }

                // Definer Quad ut fra gridData
                val gd = gridData!!
                val quad = LatLngQuad(
                    LatLng(gd.latMax, gd.lonMin),
                    LatLng(gd.latMax, gd.lonMax),
                    LatLng(gd.latMin, gd.lonMax),
                    LatLng(gd.latMin, gd.lonMin)
                )
                style.addSource(ImageSource(srcId, quad, rasterBitmap!!))
                style.addLayer(
                    RasterLayer(layerId, srcId).withProperties(
                        rasterOpacity(0.7f),
                        rasterResampling("linear")
                    )
                )
            } else {
                // Fjern hvis null eller usynlig
                style.getLayer(layerId)?.let { style.removeLayer(it) }
                style.getSource(srcId)?.let { style.removeSource(it) }
            }
        }
    }
}

// Holder grid‐data + bounds
private data class GridData(
    val latArray: List<Double>,
    val lonArray: List<Double>,
    val grid: Array<DoubleArray>,
    val latMin: Double, val latMax: Double,
    val lonMin: Double, val lonMax: Double
)

// Hjelpefunksjoner

private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

private fun bilinearHeight(
    lon: Double,
    lat: Double,
    grid: Array<DoubleArray>,
    lonArray: List<Double>,
    latArray: List<Double>
): Double {
    val i1 = latArray.indexOfFirst { it >= lat }.coerceAtMost(latArray.lastIndex)
    val j1 = lonArray.indexOfFirst { it >= lon }.coerceAtMost(lonArray.lastIndex)
    val i0 = (i1 - 1).coerceAtLeast(0)
    val j0 = (j1 - 1).coerceAtLeast(0)
    val lat0 = latArray[i0]; val lat1 = latArray[i1]
    val lon0 = lonArray[j0]; val lon1 = lonArray[j1]
    val fLat = if (lat1 == lat0) 0.0 else (lat - lat0) / (lat1 - lat0)
    val fLon = if (lon1 == lon0) 0.0 else (lon - lon0) / (lon1 - lon0)
    val h00 = grid[i0][j0]
    val h10 = grid[i0][j1]
    val h01 = grid[i1][j0]
    val h11 = grid[i1][j1]
    val h0 = h00 * (1 - fLon) + h10 * fLon
    val h1 = h01 * (1 - fLon) + h11 * fLon
    return h0 * (1 - fLat) + h1 * fLat
}

private fun interpolateColor(t: Double): Int {
    val stops = listOf(
        0.0  to Color.rgb( 33, 102, 172),
        0.25 to Color.rgb(103, 169, 207),
        0.5  to Color.rgb(209, 229, 240),
        0.75 to Color.rgb(253, 219, 199),
        1.0  to Color.rgb(178,  24,  43)
    )
    val (u0, c0) = stops.last  { it.first <= t }
    val (u1, c1) = stops.first { it.first >= t }
    if (u0 == u1) return c0
    val frac = ((t - u0) / (u1 - u0)).toFloat()
    val r = (Color.red(c0)   + (Color.red(c1)   - Color.red(c0))   * frac).toInt()
    val g = (Color.green(c0) + (Color.green(c1) - Color.green(c0)) * frac).toInt()
    val b = (Color.blue(c0)  + (Color.blue(c1)  - Color.blue(c0))  * frac).toInt()
    return Color.rgb(r, g, b)
}
