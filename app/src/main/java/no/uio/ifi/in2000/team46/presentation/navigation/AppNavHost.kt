package no.uio.ifi.in2000.team46.presentation.navigation

import android.os.Bundle
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import no.uio.ifi.in2000.team46.presentation.map.ui.screens.MapScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import no.uio.ifi.in2000.team46.presentation.ui.screens.HomeScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens.AddFishingEntryScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens.FishingLogDetailScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens.FishingLogScreen
import no.uio.ifi.in2000.team46.presentation.profile.screens.ProfileScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoriteDetailScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoritesScreen
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.screens.MapPickerScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.AddFavoriteScreen
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import no.uio.ifi.in2000.team46.presentation.weatherScreenMap.screens.WeatherDetailScreen
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.presentation.weatherScreenMap.viewmodel.WeatherDetailViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    mapView: org.maplibre.android.maps.MapView,
    mapViewModel: MapViewModel,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    searchViewModel: SearchViewModel,
    fishLogViewModel: FishingLogViewModel,
    profileViewModel: ProfileViewModel,
    weatherService: WeatherService,
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val fishLogRepo = FishLogRepository(db.fishingLogDao())
    val favoriteRepo = FavoriteRepository(db.favoriteLocationDao())
    val favoritesViewModel = FavoritesViewModel(
        favoriteRepo,
        fishLogRepo,
        db.favoriteLocationDao(),
        db.fishingLogDao(),
        db.processedSuggestionDao(),
        db.savedSuggestionDao()
    )


    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    "home" to Icons.Default.Home,
                    "map" to Icons.Default.Map,
                    "profile" to Icons.Default.Person
                ).forEach { (route, icon) ->
                    val title = when(route) {
                        "home" -> "Hjem"
                        "map" -> "Kart"
                        "profile" -> "Min Side"
                        else -> route
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = route) },
                        label = { Text(title) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val ctx = LocalContext.current
                val db = AppDatabase.getDatabase(ctx)
                val userRepo = UserRepository(db.userDao())
                HomeScreen(
                    viewModel = profileViewModel,
                    onNavigateToMap = { navController.navigate("map") },
                    onNavigateToWeather = { navController.navigate("weather") },
                    onNavigateToFishLog = { navController.navigate("fishingLog") },
                    onNavigateToFavorites = { navController.navigate("favorites") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }

            composable("map") { backStack ->
                MapScreen(
                    mapView = mapView,
                    mapViewModel = mapViewModel,
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    searchViewModel = searchViewModel,
                    navBackStackEntry = backStack,
                )
            }

            composable(
                "map/{lat}/{lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lng") { type = NavType.StringType }
                )
            ) { backStack ->
                val lat = backStack.arguments?.getString("lat")?.toDoubleOrNull()
                val lng = backStack.arguments?.getString("lng")?.toDoubleOrNull()

                MapScreen(
                    mapView = mapView,
                    mapViewModel = mapViewModel,
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    searchViewModel = searchViewModel,
                    navBackStackEntry = backStack,
                    initialLocation = if (lat != null && lng != null) Pair(lat, lng) else null
                )
            }

            composable("fishingLog") {
                FishingLogScreen(
                    viewModel = fishLogViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("addFishingEntry") {
                AddFishingEntryScreen(
                    viewModel = fishLogViewModel,
                    onCancel = { navController.popBackStack() },
                    onSave = { date, time, location, fishType, weight, notes, imageUri, latitude, longitude, fishCount ->
                        for (i in 1..fishCount) {
                            fishLogViewModel.addEntry(
                                date = date,
                                time = time,
                                location = location,
                                fishType = fishType,
                                weight = weight,
                                notes = notes,
                                imageUri = imageUri,
                                latitude = latitude,
                                longitude = longitude
                            )
                        }
                        navController.popBackStack()
                    }
                )
            }


            composable(
                "fishingLogDetail/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
                FishingLogDetailScreen(
                    entryId   = entryId,
                    viewModel = fishLogViewModel,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable("alerts") {
                // TODO: implement AlertsScreen
            }

            composable("weather") {
                val viewModel = remember { WeatherDetailViewModel(weatherService) }
                val userLocation by mapViewModel.userLocation.collectAsState()
                var weatherData by remember { mutableStateOf<WeatherData?>(null) }
                var weatherDetails by remember { mutableStateOf<no.uio.ifi.in2000.team46.domain.model.weather.WeatherDetails?>(null) }

                LaunchedEffect(userLocation) {
                    userLocation?.let { location ->
                        try {
                            weatherDetails = weatherService.getWeatherDetails(location.latitude, location.longitude)
                            weatherDetails?.let { details ->
                                weatherData = WeatherData(
                                    temperature = details.temperature,
                                    symbolCode = details.symbolCode ?: "",
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            }
                        } catch (e: Exception) {
                            // Håndter feil her hvis nødvendig
                        }
                    }
                }

                if (weatherData != null && weatherDetails != null) {
                    WeatherDetailScreen(
                        navController = navController,
                        weatherData = weatherData!!,
                        locationName = "Min posisjon",
                        feelsLike = weatherDetails!!.feelsLike ?: 0.0,
                        highTemp = weatherDetails!!.highTemp ?: 0.0,
                        lowTemp = weatherDetails!!.lowTemp ?: 0.0,
                        weatherDescription = weatherDetails!!.description ?: "",
                        windSpeed = weatherDetails!!.windSpeed,
                        windDirection = weatherDetails!!.windDirection,
                        viewModel = viewModel,
                        searchViewModel = searchViewModel,
                        weatherService = weatherService,
                        isFromHomeScreen = true
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            composable("favorites") {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(
                "mapPickerFromSuggestion/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""

                MapPickerScreen(
                    navController = navController,
                    selectionMode = "POINT", // vi starter med punkt (eller område, hvis du vil la brukeren velge senere)
                    navigateToAddFavorite = { pickedPoint, pickedArea, locationType ->
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("savedName", name)
                            if (locationType == "POINT") {
                                set("pickedPoint", pickedPoint)
                            } else {
                                set("pickedArea", pickedArea)
                            }
                            set("savedLocationType", locationType)
                        }
                        navController.navigate("addFavorite?name=$name")
                    }
                )
            }

            composable(
                "addFavorite?name={name}",
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val nameArg = backStackEntry.arguments?.getString("name") ?: ""

                AddFavoriteScreen(
                    viewModel = favoritesViewModel,
                    navController = navController,
                    onCancel = { navController.popBackStack() },
                    onSave = {
                        navController.navigate("favorites") {
                            popUpTo("favorites") { inclusive = true }
                        }
                    },
                    defaultName = nameArg // 👈 du må også sende denne inn i AddFavoriteScreen
                )
            }


            composable(
                "mapPicker/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "POINT"
                MapPickerScreen(
                    navController = navController,
                    selectionMode = type
                )
            }

            composable(
                "mapArea/{areaPointsJson}",
                arguments = listOf(navArgument("areaPointsJson") { type = NavType.StringType })
            ) { backStack ->
                val areaPointsJson = backStack.arguments?.getString("areaPointsJson")
                val areaPoints: List<Pair<Double, Double>> = try {
                    val jsonArray = org.json.JSONArray(areaPointsJson)
                    (0 until jsonArray.length()).map { i ->
                        val point = jsonArray.getJSONObject(i)
                        Pair(point.getDouble("lat"), point.getDouble("lng"))
                    }
                } catch (e: Exception) { emptyList() }
                MapScreen(
                    mapView = mapView,
                    mapViewModel = mapViewModel,
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    searchViewModel = searchViewModel,
                    navBackStackEntry = backStack,
                    areaPoints = areaPoints
                )
            }

            composable(
                "favoriteDetail/{favoriteId}",
                arguments = listOf(navArgument("favoriteId") { type = NavType.IntType })
            ) { backStackEntry ->
                val favoriteId = backStackEntry.arguments?.getInt("favoriteId") ?: return@composable
                FavoriteDetailScreen(
                    favoriteId = favoriteId,
                    viewModel = favoritesViewModel,
                    onBack = { navController.popBackStack() },
                    onAddFishingLog = { location ->
                        navController.navigate("addFishingEntry?location=$location")
                    },
                    onNavigateToMap = { lat, lng, areaPointsJson ->
                        if (areaPointsJson != null) {
                            navController.navigate("mapArea/$areaPointsJson")
                        } else {
                            navController.navigate("map/$lat/$lng")
                        }
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    viewModel         = profileViewModel,
                    onNavigateToHome  = { navController.navigate("home") },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
            //Weather detail screen that appears on map
            composable(
                route = "weather_detail/{temperature}/{feelsLike}/{highTemp}/{lowTemp}/{symbolCode}/{description}/{locationName}/{windSpeed}/{windDirection}/{latitude}/{longitude}",
                arguments = listOf(
                    navArgument("temperature") { type = NavType.FloatType },
                    navArgument("feelsLike") { type = NavType.FloatType },
                    navArgument("highTemp") { type = NavType.FloatType },
                    navArgument("lowTemp") { type = NavType.FloatType },
                    navArgument("symbolCode") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType },
                    navArgument("locationName") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("windSpeed") {
                        type = NavType.FloatType
                        defaultValue = 0f
                    },
                    navArgument("windDirection") {
                        type = NavType.FloatType
                        defaultValue = 0f
                    },
                    navArgument("latitude") { type = NavType.FloatType },
                    navArgument("longitude") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val temperature = backStackEntry.arguments?.getFloat("temperature")?.toDouble()
                val feelsLike = backStackEntry.arguments?.getFloat("feelsLike")?.toDouble()
                val highTemp = backStackEntry.arguments?.getFloat("highTemp")?.toDouble()
                val lowTemp = backStackEntry.arguments?.getFloat("lowTemp")?.toDouble()
                val symbolCode = backStackEntry.arguments?.getString("symbolCode")
                val description = backStackEntry.arguments?.getString("description")
                val encodedLocationName = backStackEntry.arguments?.getString("locationName")
                val windSpeed = backStackEntry.arguments?.getFloat("windSpeed")?.toDouble()
                val windDirection = backStackEntry.arguments?.getFloat("windDirection")?.toDouble()
                val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble()
                val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble()

                val locationName = encodedLocationName?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                } ?: "Nåværende posisjon"

                if (temperature != null && feelsLike != null && highTemp != null &&
                    lowTemp != null && symbolCode != null && description != null &&
                    latitude != null && longitude != null) {

                    val viewModel = remember {
                        WeatherDetailViewModel(weatherService)
                    }

                    WeatherDetailScreen(
                        navController = navController,
                        weatherData = WeatherData(
                            temperature = temperature,
                            symbolCode = symbolCode,
                            latitude = latitude,
                            longitude = longitude
                        ),
                        locationName = locationName,
                        feelsLike = feelsLike,
                        highTemp = highTemp,
                        lowTemp = lowTemp,
                        weatherDescription = description,
                        windSpeed = windSpeed,
                        windDirection = windDirection,
                        hasWarning = false,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
