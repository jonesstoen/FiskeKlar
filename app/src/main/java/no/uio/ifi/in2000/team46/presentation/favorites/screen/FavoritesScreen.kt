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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions

/**
 * WARNINGS: This file contains usage of deprecated MapLibre classes such as MarkerOptions and PolygonOptions.
 * These are used intentionally due to the lack of stable or well-documented alternatives in the current SDK.
 * The functionality remains reliable for our purposes and was prioritized for simplicity and compatibility.
 * Other warnings (unused variables) are minor and do not affect functionality.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigate: (String) -> Unit,
    profileViewModel: ProfileViewModel
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val favoritesWithStats by viewModel.favoritesWithStats.collectAsState()
    val currentFilter by viewModel.filterType.collectAsState()
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }
    val userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    val filteredFavorites = favoritesWithStats.filter {
        val matchesSearch = it.favorite.name.contains(searchQuery, ignoreCase = true)
        val type = it.favorite.locationType.trim()
        val matchesType = when (currentFilter) {
            null -> true
            "Punkter" -> type.equals("POINT", ignoreCase = true)
            "Områder" -> type.equals("AREA", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesType
    }

    val showSuggestions = searchQuery.isNotBlank() && filteredFavorites.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine favorittsteder") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake til hjem")
                    }
                },
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

            MiniMap(
                onMapClick = { onNavigate("map?showFavorites=true") },
                userLocation = userLocation,
                favorites = filteredFavorites,
                profileViewModel = profileViewModel
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                VerticalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                FilterButton(
                    text = "Punkter",
                    selected = currentFilter == "Punkter",
                    onClick = { viewModel.filterByType("Punkter") },
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                FilterButton(
                    text = "Områder",
                    selected = currentFilter == "Områder",
                    onClick = { viewModel.filterByType("Områder") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Trykk + for å legge til favoritter", style = MaterialTheme.typography.bodyLarge)
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

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // container for button content, applies click and background
    Box(
        modifier = modifier
            // make box fill the max height of its parent
            .fillMaxHeight()
            // apply click action when pressed
            .clickable(onClick = onClick)
            // set background color depending on selection state
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        // display label text with dynamic color and style
        Text(
            text = text,
            // choose text color based on whether button is selected
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            // apply medium font weight for emphasis
            fontWeight = FontWeight.Medium
        )
    }
}



// this composable displays a favorite location card with stats including count of catches, last catch date, and best catch details

@Composable
fun FavoriteCard(
    favoriteWithStats: FavoriteWithStats,
    onClick: () -> Unit
) {
    // extract favorite data and determine if location type is point
    val favorite = favoriteWithStats.favorite
    val isPoint = favorite.locationType == "POINT"

    // clickable card full width
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
            // type indicator: circle for point or rounded rectangle for area
            if (isPoint) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935)), // red for point
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
                        .background(Color(0xFF4CAF50)), // green for area
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

            // information column for name and catch summary
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // build text for total catches and last catch date
                val lastCatchInfo = favoriteWithStats.lastCatch?.let {
                    "• sist: ${it.date}"
                } ?: ""

                Text(
                    text = "${favoriteWithStats.catchCount} fisk $lastCatchInfo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // display best catch stats if available
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


// this composable renders a small map preview showing user and favorite locations, with clickable overlay to expand

@Composable
fun MiniMap(
    onMapClick: () -> Unit,
    userLocation: android.location.Location?,
    favorites: List<FavoriteWithStats>,
    profileViewModel: ProfileViewModel
) {
    // create map view lifecycle aware
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    // repository for location retrieval
    val locationRepository = remember { LocationRepository(context) }
    // coroutine scope for async calls
    val scope = rememberCoroutineScope()

    // observe app theme from viewmodel to set map style
    val appTheme by profileViewModel.theme.collectAsState()
    val isDarkMode = when (appTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    // local state for accurate user location
    var currentLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // fetch fast location once
    LaunchedEffect(Unit) {
        locationRepository.getFastLocation()?.let { location ->
            currentLocation = location
        }
    }

    // clean up map view resources on dispose
    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    // container box for map and overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // display map view
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            view.getMapAsync { map ->
                // set style url based on theme
                val apiKey = "kPH7fJZHXa4Pj6d1oIuw"
                val style = if (isDarkMode) "streets-v2-dark" else "basic"
                val styleUrl = "https://api.maptiler.com/maps/$style/style.json?key=$apiKey"
                map.setStyle(styleUrl) {

                    // add markers or polygons for each favorite
                    favorites.forEach { favWithStats ->
                        val fav = favWithStats.favorite
                        if (fav.locationType == "POINT") {
                            // add point marker
                            val marker = MarkerOptions()
                                .position(LatLng(fav.latitude, fav.longitude))
                                .title(fav.name)
                            map.addMarker(marker)
                        } else {
                            // parse area points json and draw polygon
                            val points = fav.areaPoints?.let { json ->
                                try {
                                    val arr = org.json.JSONArray(json)
                                    List(arr.length()) { i ->
                                        val obj = arr.getJSONObject(i)
                                        LatLng(obj.getDouble("lat"), obj.getDouble("lng"))
                                    }
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            } ?: emptyList()

                            if (points.isNotEmpty()) {
                                val polygon = PolygonOptions()
                                    .addAll(points)
                                    .fillColor(0x334CAF50) // green fill with opacity
                                    .strokeColor(0xFF4CAF50.toInt()) // green border
                                map.addPolygon(polygon)
                            }
                        }
                    }

                    // center camera on user or first favorite or fallback to oslo
                    val center = currentLocation?.let {
                        LatLng(it.latitude, it.longitude)
                    } ?: favorites.firstOrNull()?.favorite?.let {
                        LatLng(it.latitude, it.longitude)
                    } ?: LatLng(59.9139, 10.7522)

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10.0))
                }
            }
        }

        // transparent overlay to capture clicks
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMapClick() }
                .background(Color.Transparent)
        )

        // caption bar at bottom prompting full screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0x993B5F8A))
        ) {
            Text(
                text = "trykk for fullskjermkart",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            )
        }
    }
}
