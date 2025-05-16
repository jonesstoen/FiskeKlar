package no.uio.ifi.in2000.team46.presentation.sos.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModelFactory
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import kotlin.math.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.utils.NetworkUtils

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SosScreen(
    aisViewModel: AisViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            LocationRepository(LocalContext.current),
            MetAlertsRepository()
        )
    ),
    onBack: (() -> Unit)? = null,
    navController: NavController = rememberNavController()
) {
    val vesselPositions by aisViewModel.vesselPositions.collectAsState()
    val userLocation by mapViewModel.userLocation.collectAsState()
    val lastUserLocation = remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var savedPosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    // check for internet connectivity
    var isNetworkConnected by remember { mutableStateOf(NetworkUtils.isNetworkAvailable(context)) }

    // dont call activateLayer/updateVisibleRegion if vessel list is not empty and position has not changed
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            val currentLoc = Pair(userLocation!!.latitude, userLocation!!.longitude)
            if (vesselPositions.isEmpty() || lastUserLocation.value != currentLoc) {
                aisViewModel.activateLayer()
                aisViewModel.updateVisibleRegion(
                    minLon = userLocation!!.longitude - 0.5,
                    minLat = userLocation!!.latitude - 0.5,
                    maxLon = userLocation!!.longitude + 0.5,
                    maxLat = userLocation!!.latitude + 0.5
                )
                lastUserLocation.value = currentLoc
            }
        }
    }

    // find the three closest vessels to the user location
    val nearestVessels = remember(vesselPositions, userLocation) {
        userLocation?.let { loc ->
            vesselPositions
                .map { vessel ->
                    val dist = haversine(
                        loc.latitude,
                        loc.longitude,
                        vessel.latitude,
                        vessel.longitude
                    )
                    vessel to dist
                }
                .sortedBy { it.second }
                .take(3)
        } ?: emptyList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { it: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // red toppbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("EMERGENCY", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // closest vessel positions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nærmeste fartøy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = {
                    aisViewModel.refreshVesselPositions()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Fartøysdata oppdatert")
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Oppdater")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                nearestVessels.forEach { (vessel, dist) ->
                    val vesselType = VesselTypes.ALL_TYPES.entries.find { it.value == vessel.shipType }?.key ?: "Ukjent"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsBoat, contentDescription = null, tint = Color(0xFF1B4965), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(vessel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("$vesselType • ${"%.1f".format(dist)} NM", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                            if (userLocation != null) {
                                Button(
                                    onClick = {
                                        navController.navigate(
                                            "mapWithVessel?userLat=${userLocation!!.latitude}&userLon=${userLocation!!.longitude}&vesselLat=${vessel.latitude}&vesselLon=${vessel.longitude}&vesselName=${vessel.name}&shipType=${vessel.shipType}"
                                        )
                                    },
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475B8B), contentColor = Color.White)
                                ) {
                                    Text("Vis på kart", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                if (nearestVessels.isEmpty()) {
                    if (!isNetworkConnected) {
                        Text("Ingen internettforbindelse", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        Text("Ingen fartøy funnet i nærheten.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // emergency numbers as clickable cards
            Text(
                "Trykk på et nødnummer for å ringe i nødstilfelle",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column {
                    val rsLogo: Painter = painterResource(id = R.drawable.rs_logo)
                    val emergencyNumbers = listOf(
                        Triple("Redningsselskapet", rsLogo, "02016"),
                        Triple("Politi", Icons.Default.LocalPolice, "112"),
                        Triple("Brannvesen", Icons.Default.LocalFireDepartment, "110"),
                        Triple("Ambulanse", Icons.Default.LocalHospital, "113")
                    )
                    emergencyNumbers.forEachIndexed { idx, (label, icon, number) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$number")
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (label == "Redningsselskapet") {
                                Image(icon as Painter, contentDescription = null, modifier = Modifier.size(36.dp))
                            } else {
                                Icon(icon as ImageVector, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(label, modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text(number, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        if (idx < emergencyNumbers.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // current location with save button
            Text(
                text = "Din nåværende posisjon",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp,  vertical = 14.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        if (userLocation != null) {
                            val lat = userLocation!!.latitude
                            val lon = userLocation!!.longitude
                            Text(
                                "${"%.5f".format(lat)}°N, ${"%.5f".format(lon)}°E",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text("Posisjon ikke tilgjengelig", color = Color.Gray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// this function calculates the distance between two geographical points using the haversine formula
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distanceKm = R * c
    return distanceKm / 1.852 // convert to nautical miles
}