package no.uio.ifi.in2000.team46.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import no.uio.ifi.in2000.team46.presentation.home.screens.HomeScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.screens.AddFishingEntryScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.screens.FishingLogDetailScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.screens.FishingLogScreen
import no.uio.ifi.in2000.team46.presentation.profile.screens.ProfileScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoriteDetailScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoritesScreen
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import no.uio.ifi.in2000.team46.presentation.map.screens.MapPickerScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.AddFavoriteScreen
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import no.uio.ifi.in2000.team46.presentation.weather.screens.WeatherDetailScreen
import no.uio.ifi.in2000.team46.domain.weather.WeatherData
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.presentation.weather.viewmodel.WeatherDetailViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import no.uio.ifi.in2000.team46.presentation.sos.screens.SosScreen
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.team46.domain.weather.WeatherDetails
import no.uio.ifi.in2000.team46.presentation.map.screens.MapScreen
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.OnboardingViewModel
import no.uio.ifi.in2000.team46.presentation.ui.theme.backgroundLight
import no.uio.ifi.in2000.team46.presentation.profile.screens.ThemeSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    mapView: MapView,
    mapViewModel: MapViewModel,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    searchViewModel: SearchViewModel,
    fishLogViewModel: FishingLogViewModel,
    profileViewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
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
        fishLogRepo
    )


    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    "home" to Icons.Default.Home,
                    "map" to Icons.Default.Map,
                    "sos" to Icons.Default.Warning,
                    "profile" to Icons.Default.Person
                ).forEach { (route, icon) ->
                    val title = when(route) {
                        "home" -> "Hjem"
                        "map" -> "Kart"
                        "sos" -> "SOS"
                        "profile" -> "Min Side"
                        else -> route
                    }
                    NavigationBarItem(
                        icon = {
                            Icon(
                                icon,
                                contentDescription = route,
                                tint = if (route == "sos") Color(0xFFD32F2F) else LocalContentColor.current
                            )
                        },
                        label = {
                            Text(
                                title,
                                color = if (route == "sos") Color(0xFFD32F2F) else LocalContentColor.current
                            )
                        },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                    onboardingViewModel = onboardingViewModel,
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
                    navController = navController,
                    profileViewModel = profileViewModel
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
                    navController = navController,
                    initialLocation = if (lat != null && lng != null) Pair(lat, lng) else null,
                    profileViewModel = profileViewModel
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
                    favoritesViewModel = favoritesViewModel,
                    navController = navController,
                    onCancel = { navController.popBackStack() },
                    onSave = { date, time, location, fishType, weight, notes, imageUri, latitude, longitude, fishCount ->
                        fishLogViewModel.addEntry(
                            date = date,
                            time = time,
                            location = location,
                            fishType = fishType,
                            weight = weight,
                            notes = notes,
                            imageUri = imageUri,
                            latitude = latitude,
                            longitude = longitude,
                            count = fishCount
                        )
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
                var weatherDetails by remember { mutableStateOf<WeatherDetails?>(null) }

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
                    onNavigate = { route -> navController.navigate(route) },
                    profileViewModel = profileViewModel
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
                    },
                    profileViewModel = profileViewModel
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
                val previousRoute = navController.previousBackStackEntry?.destination?.route

                AddFavoriteScreen(
                    viewModel = favoritesViewModel,
                    navController = navController,
                    onCancel = { navController.popBackStack() },
                    onSave = {
                        if (previousRoute == "addFishingEntry") {
                            navController.currentBackStackEntry?.savedStateHandle?.set("newFavorite", nameArg)
                            navController.navigate("addFishingEntry") {
                                popUpTo("addFishingEntry") { 
                                    saveState = true 
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate("favorites") {
                                popUpTo("favorites") { inclusive = false }
                            }
                        }
                    },
                    defaultName = nameArg
                )
            }


            composable(
                "mapPicker/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "POINT"
                MapPickerScreen(
                    navController = navController,
                    selectionMode = type,
                    profileViewModel = profileViewModel
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
                    navController = navController,
                    areaPoints = areaPoints,
                    profileViewModel = profileViewModel
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
                    viewModel = profileViewModel,
                    onNavigateToHome = { navController.navigate("home") },
                    onNavigateToAlerts = { navController.navigate("alerts") },
                    onNavigateToTheme = { navController.navigate("theme_settings") }
                )
            }

            composable("theme_settings") {
                ThemeSettingsScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.navigateUp() }
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

            composable("sos") {
                SosScreen(onBack = { navController.popBackStack() }, navController = navController)
            }

            composable(
                "mapWithVessel?userLat={userLat}&userLon={userLon}&vesselLat={vesselLat}&vesselLon={vesselLon}&vesselName={vesselName}&shipType={shipType}",
                arguments = listOf(
                    navArgument("userLat") { type = NavType.StringType },
                    navArgument("userLon") { type = NavType.StringType },
                    navArgument("vesselLat") { type = NavType.StringType },
                    navArgument("vesselLon") { type = NavType.StringType },
                    navArgument("vesselName") { type = NavType.StringType },
                    navArgument("shipType") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val userLat = backStackEntry.arguments?.getString("userLat")?.toDoubleOrNull()
                val userLon = backStackEntry.arguments?.getString("userLon")?.toDoubleOrNull()
                val vesselLat = backStackEntry.arguments?.getString("vesselLat")?.toDoubleOrNull()
                val vesselLon = backStackEntry.arguments?.getString("vesselLon")?.toDoubleOrNull()
                val vesselName = backStackEntry.arguments?.getString("vesselName") ?: ""
                val shipType = backStackEntry.arguments?.getInt("shipType")

                MapScreen(
                    mapView = mapView,
                    mapViewModel = mapViewModel,
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    searchViewModel = searchViewModel,
                    navController = navController,
                    highlightVessel = if (userLat != null && userLon != null && vesselLat != null && vesselLon != null && shipType != null)
                        HighlightVesselData(userLat, userLon, vesselLat, vesselLon, vesselName, shipType)
                    else null,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}
