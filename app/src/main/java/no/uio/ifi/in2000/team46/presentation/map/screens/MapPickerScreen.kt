package no.uio.ifi.in2000.team46.presentation.map.screens

import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.presentation.map.utils.rememberMapViewWithLifecycle
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController,
    selectionMode: String,
    navigateToAddFavorite: ((Pair<Double, Double>?, List<Pair<Double, Double>>?, String) -> Unit)? = null
) {

    // ----------- State og kartoppsett -----------
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val locationRepo = remember { LocationRepository(context) }
    val pickedPoints = remember { mutableStateListOf<LatLng>() }
    var mapLibre by remember { mutableStateOf<MapLibreMap?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    // Hent brukerens posisjon
    LaunchedEffect(Unit) {
        userLocation = locationRepo.getFastLocation()
    }

    // ----------- UI: Scaffold, TopAppBar, kart og valg -----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Velg ${if (selectionMode == "POINT") "punkt" else "område"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    val validSelection = (selectionMode == "POINT" && pickedPoints.size == 1) ||
                            (selectionMode == "AREA" && pickedPoints.size >= 3)
                    if (validSelection) {
                        TextButton(onClick = {
                            val savedState = navController.previousBackStackEntry?.savedStateHandle
                            if (navigateToAddFavorite != null) {
                                // Bruk den skreddersydde funksjonen hvis den finnes
                                if (selectionMode == "POINT") {
                                    val point = pickedPoints[0]
                                    navigateToAddFavorite(Pair(point.latitude, point.longitude), null, "POINT")
                                } else {
                                    val area = pickedPoints.map { Pair(it.latitude, it.longitude) }
                                    navigateToAddFavorite(null, area, "AREA")
                                }
                            } else {
                                // Ellers bruk vanlig savedStateHandle
                                if (selectionMode == "POINT") {
                                    val point = pickedPoints[0]
                                    savedState?.set("pickedPoint", Pair(point.latitude, point.longitude))
                                    savedState?.set("savedLocationType", "POINT")
                                } else {
                                    val area = pickedPoints.map { Pair(it.latitude, it.longitude) }
                                    savedState?.set("pickedArea", area)
                                    savedState?.set("savedLocationType", "AREA")
                                }
                                navController.popBackStack()
                            }
                        }) {
                            Text("Velg")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ----------- Kartvisning og valg -----------
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { view ->
                view.getMapAsync { map ->
                    mapLibre = map
                    map.setStyle(
                        Style.Builder().fromUri("https://api.maptiler.com/maps/basic-v2/style.json?key=kPH7fJZHXa4Pj6d1oIuw")
                    ) {
                        map.addOnMapClickListener { latLng ->
                            when (selectionMode) {
                                "POINT" -> {
                                    pickedPoints.clear()
                                    pickedPoints.add(latLng)
                                    map.clear()
                                    map.addMarker(
                                        MarkerOptions().position(latLng).title("Valgt punkt")
                                    )
                                }
                                "AREA" -> {
                                    pickedPoints.add(latLng)
                                    map.clear()
                                    pickedPoints.forEach {
                                        map.addMarker(MarkerOptions().position(it))
                                    }
                                    if (pickedPoints.size >= 3) {
                                        map.addPolygon(
                                            PolygonOptions()
                                                .addAll(pickedPoints)
                                                .fillColor(0x5500BCD4)
                                                .strokeColor(Color.Blue.hashCode())
                                        )
                                    }
                                }
                            }
                            true
                        }
                        userLocation?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            map.setCameraPosition(
                                org.maplibre.android.camera.CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(10.0)
                                    .build()
                            )
                        }
                    }
                }
            }

            // ----------- Hint nederst -----------
            if (pickedPoints.isEmpty()) {
                Text(
                    text = "Trykk på kartet for å velge ${if (selectionMode == "POINT") "punkt" else "område"}",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color(0x993B5F8A))
                        .padding(12.dp),
                    color = Color.White
                )
            }
        }
    }

    // ----------- BackHandler for å gå tilbake -----------
    BackHandler {
        navController.popBackStack()
    }
}