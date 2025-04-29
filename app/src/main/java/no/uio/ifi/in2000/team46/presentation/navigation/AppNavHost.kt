package no.uio.ifi.in2000.team46.presentation.navigation

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import no.uio.ifi.in2000.team46.presentation.map.ui.screens.MapScreen
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
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoriteDetailScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.FavoritesScreen
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.screens.MapPickerScreen
import no.uio.ifi.in2000.team46.presentation.favorites.screen.AddFavoriteScreen

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
    profileViewModel: ProfileViewModel
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
                    navBackStackEntry = backStack
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
                    entryId = entryId,
                    viewModel = fishLogViewModel,
                    onBack = { navController.popBackStack() }
                )
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
                    viewModel = profileViewModel,
                    onNavigateToHome = { navController.navigate("home") },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
        }
    }
}
