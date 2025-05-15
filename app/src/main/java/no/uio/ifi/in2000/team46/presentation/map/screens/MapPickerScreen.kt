package no.uio.ifi.in2000.team46.presentation.map.screens

import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import org.maplibre.android.camera.CameraPosition.Builder

// summary: displays a map picker screen allowing user to select either a single point or a polygonal area on the map and returns the chosen coordinates via callback or navigation
/**
 * WARNINGS: This file contains usage of deprecated MapLibre classes such as MarkerOptions and PolygonOptions.
 * These are used intentionally due to the lack of stable or well documented alternatives in the current SDK.
 * The functionality remains reliable for our purposes and was prioritized for simplicity and compatibility.
 * Other warnings (unused variables, unnecessary safe calls) are minor and do not affect functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController,
    selectionMode: String,
    navigateToAddFavorite: ((Pair<Double, Double>?, List<Pair<Double, Double>>?, String) -> Unit)? = null,
    profileViewModel: ProfileViewModel
) {

    // remember application context and initialize map view with lifecycle handling
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    // repository for obtaining last known user location
    val locationRepo = remember { LocationRepository(context) }
    // list of picked coordinates on map
    val pickedPoints = remember { mutableStateListOf<LatLng>() }
    // reference to maplibre map instance
    var mapLibre by remember { mutableStateOf<MapLibreMap?>(null) }
    // holds the current user location
    var userLocation by remember { mutableStateOf<Location?>(null) }

    // observe theme from profile view model
    val appTheme by profileViewModel.theme.collectAsState()
    // determine if dark mode should be applied based on user preference or system setting
    val isDarkMode = when (appTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    // configure map style URL using MapTiler key and selected style
    val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
    val style = if (isDarkMode) "streets-v2-dark" else "basic"
    val styleUrl = "https://api.maptiler.com/maps/$style/style.json?key=$apiKey"

    // fetch fast user location asynchronously when composable is first launched
    LaunchedEffect(Unit) {
        userLocation = locationRepo.getFastLocation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Velg ${if (selectionMode == "POINT") "punkt" else "område"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    // check if there is a valid selection: one point or area with at least 3 points
                    val validSelection = (selectionMode == "POINT" && pickedPoints.size == 1) ||
                            (selectionMode == "AREA" && pickedPoints.size >= 3)
                    if (validSelection) {
                        TextButton(onClick = {
                            val savedState = navController.previousBackStackEntry?.savedStateHandle
                            if (navigateToAddFavorite != null) {
                                // use custom navigation callback if provided
                                if (selectionMode == "POINT") {
                                    val point = pickedPoints[0]
                                    navigateToAddFavorite(Pair(point.latitude, point.longitude), null, "POINT")
                                } else {
                                    val area = pickedPoints.map { Pair(it.latitude, it.longitude) }
                                    navigateToAddFavorite(null, area, "AREA")
                                }
                            } else {
                                // fallback to savedStateHandle for passing back results
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
            //map view and tap listener
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { view ->
                view.getMapAsync { map ->
                    // store map instance
                    mapLibre = map
                    // apply chosen style to map
                    map.setStyle(
                        Style.Builder().fromUri(styleUrl)
                    ) {
                        // add listener for map clicks to record coordinates
                        map.addOnMapClickListener { latLng ->
                            when (selectionMode) {
                                "POINT" -> {
                                    // clear any existing point and add new marker
                                    pickedPoints.clear()
                                    pickedPoints.add(latLng)
                                    map.clear()
                                    map.addMarker(
                                        MarkerOptions().position(latLng).title("Valgt punkt")
                                    )
                                }
                                "AREA" -> {
                                    // add new vertex, redraw markers and polygon if enough points
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
                            // consume click event
                            true
                        }
                        // move camera to user location when available
                        userLocation?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            map.cameraPosition = Builder()
                                .target(latLng)
                                .zoom(10.0)
                                .build()
                        }
                    }
                }
            }


            if (pickedPoints.isEmpty()) {
                // prompt user to tap on the map for selection
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

    BackHandler {
        navController.popBackStack()
    }
}
