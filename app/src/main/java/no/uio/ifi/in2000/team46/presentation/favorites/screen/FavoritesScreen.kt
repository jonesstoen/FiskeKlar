package no.uio.ifi.in2000.team46.presentation.favorites.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoriteWithStats
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import no.uio.ifi.in2000.team46.presentation.map.utils.rememberMapViewWithLifecycle
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel : FavoritesViewModel,
    onNavigate: (String) -> Unit,
    profileViewModel: ProfileViewModel
) {
    // ----------- State og data -----------
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val favoritesWithStats by viewModel.favoritesWithStats.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // ----------- Filtrering for punkt og omraade -----------
    val filteredFavorites = favoritesWithStats.filter {
        val matchesSearch = it.favorite.name.contains(searchQuery, ignoreCase = true)
        val type = it.favorite.locationType?.trim()
        val matchesType = when (currentFilter) {
            null -> true // Alle
            "Punkter" -> type.equals("POINT", ignoreCase = true)
            "Områder" -> type.equals("AREA", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesType
    }
    val showSuggestions = searchQuery.isNotBlank() && filteredFavorites.isNotEmpty()

    // ----------- Scaffold (AppBar, FAB) -----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine favorittsteder") },
                actions = {
                    if (favoritesWithStats.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Slett alle favoritter",
                                tint = Color(0xFFE53935)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("addFavorite") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Legg til favoritt")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ----------- Søkefelt og autocomplete -----------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Søk blant favoritter...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { event ->
                        if (event.key == Key.Enter && filteredFavorites.size == 1) {
                            onNavigate("favoriteDetail/${filteredFavorites.first().favorite.id}")
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Søk")
                }
            )
            if (showSuggestions) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Column {
                        filteredFavorites.take(5).forEach { fav ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = ""
                                        onNavigate("favoriteDetail/${fav.favorite.id}")
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fav.favorite.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // ----------- MiniMap (kartutsnitt) -----------
            MiniMap(
                onMapClick = { onNavigate("map") },
                userLocation = userLocation,
                favorites = filteredFavorites,
                profileViewModel = profileViewModel
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ----------- Filterknapper -----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterButton(
                    text = "Alle",
                    selected = currentFilter == null,
                    onClick = { viewModel.filterByType(null) },
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                FilterButton(
                    text = "Punkter",
                    selected = currentFilter == "Punkter",
                    onClick = { viewModel.filterByType("Punkter") },
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                FilterButton(
                    text = "Områder",
                    selected = currentFilter == "Områder",
                    onClick = { viewModel.filterByType("Områder") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Favorittliste -----------
            if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Trykk + for å legge til favoritter",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFavorites) { favoriteWithStats ->
                        FavoriteCard(
                            favoriteWithStats = favoriteWithStats,
                            onClick = {
                                onNavigate("favoriteDetail/${favoriteWithStats.favorite.id}")
                            }
                        )
                    }
                }
            }
        }

        // ----------- Dialoger -----------
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Slett alle favoritter") },
                text = { Text("Er du sikker på at du vil slette alle favoritter?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.favorites.value.forEach {
                            viewModel.deleteFavorite(it)
                        }
                        showDeleteAllDialog = false
                    }) {
                        Text("Slett alle")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Avbryt")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteCard(
    favoriteWithStats: FavoriteWithStats,
    onClick: () -> Unit
) {
    val favorite = favoriteWithStats.favorite
    val isPoint = favorite.locationType == "POINT"

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type-indikator (punkt eller område)
            if (isPoint) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935)), // Rød for punkt
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50)), // Grønn for område
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "O",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informasjon
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Info om antall fangster og dato for siste fangst
                val lastCatchInfo = favoriteWithStats.lastCatch?.let {
                    "• Sist: ${it.date}"
                } ?: ""

                Text(
                    text = "${favoriteWithStats.catchCount} fisk $lastCatchInfo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Beste fangst-info
            favoriteWithStats.bestCatch?.let { bestCatch ->
                Surface(
                    modifier = Modifier.width(110.dp),
                    color = Color(0xFFDCE8F0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BESTE FANGST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B5F8A)
                        )

                        Text(
                            text = "${bestCatch.weight} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B5F8A)
                        )

                        Text(
                            text = bestCatch.fishType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF3B5F8A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniMap(
    onMapClick: () -> Unit,
    userLocation: android.location.Location?,
    favorites: List<FavoriteWithStats>,
    profileViewModel: ProfileViewModel
) {
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val locationRepository = remember { LocationRepository(context) }
    val scope = rememberCoroutineScope()
    
    // Get the theme from ProfileViewModel and determine if dark mode should be used
    val appTheme by profileViewModel.theme.collectAsState()
    val isDarkMode = when (appTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    // State for user location
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // Get user location
    LaunchedEffect(Unit) {
        locationRepository.getFastLocation()?.let { location ->
            userLocation = location
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Kart
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            view.getMapAsync { map ->
                val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
                val style = if (isDarkMode) "streets-v2-dark" else "basic"
                val styleUrl = "https://api.maptiler.com/maps/$style/style.json?key=$apiKey"
                map.setStyle(styleUrl) {
                    // Legg til brukerens posisjon
                    userLocation?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                            .position(userLatLng)
                            .title("Din posisjon")
                        map.addMarker(markerOptions)
                    }

                    // Legg til favorittsteder
                    favorites.forEach { favoriteWithStats ->
                        val favorite = favoriteWithStats.favorite
                        if (favorite.locationType == "POINT") {
                            // Legg til punkt
                            val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                                .position(LatLng(favorite.latitude, favorite.longitude))
                                .title(favorite.name)
                            map.addMarker(markerOptions)
                        } else {
                            // Legg til område
                            val points = favorite.areaPoints?.let { pointsJson ->
                                try {
                                    val jsonArray = org.json.JSONArray(pointsJson)
                                    List(jsonArray.length()) { i ->
                                        val point = jsonArray.getJSONObject(i)
                                        LatLng(point.getDouble("lat"), point.getDouble("lng"))
                                    }
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            } ?: emptyList()

                            if (points.isNotEmpty()) {
                                val polygonOptions = org.maplibre.android.annotations.PolygonOptions()
                                    .addAll(points)
                                    .fillColor(0x334CAF50) // Grønn med 20% opacity
                                    .strokeColor(0xFF4CAF50.toInt()) // Grønn
                                map.addPolygon(polygonOptions)
                            }
                        }
                    }

                    // Flytt kamera til brukerens posisjon eller første favoritt
                    val centerLatLng = userLocation?.let { LatLng(it.latitude, it.longitude) }
                        ?: favorites.firstOrNull()?.favorite?.let { LatLng(it.latitude, it.longitude) }
                        ?: LatLng(59.9139, 10.7522) // Oslo som fallback

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 10.0))
                }
            }
        }
        // Klikkbar overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMapClick() }
                .background(Color.Transparent)
        )
        // Tekstboks nederst
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0x993B5F8A))
        ) {
            Text(
                text = "Trykk for fullskjermkart",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            )
        }
    }
}