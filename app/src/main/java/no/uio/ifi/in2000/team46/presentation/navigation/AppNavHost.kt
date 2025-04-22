package no.uio.ifi.in2000.team46.presentation.navigation

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
import androidx.compose.runtime.getValue
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
import no.uio.ifi.in2000.team46.data.repository.UserRepository

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

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    "home"    to Icons.Default.Home,
                    "map"     to Icons.Default.Map,
                    "profile" to Icons.Default.Person
                ).forEach { (route, icon) ->
                    val title = when(route) {
                        "home"    -> "Hjem"
                        "map"     -> "Kart"
                        "profile" -> "Min Side"
                        else      -> route
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
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val ctx = LocalContext.current
                val db = AppDatabase.getDatabase(ctx)
                val userRepo = UserRepository(db.userDao())
                HomeScreen(
                    viewModel = profileViewModel,
                    onNavigateToMap       = { navController.navigate("map") },
                    onNavigateToWeather   = { navController.navigate("weather") },
                    onNavigateToFishLog   = { navController.navigate("fishlog") },
                    onNavigateToFavorites = { navController.navigate("favorites") },
                    onNavigateToProfile   = { navController.navigate("profile") },
                    onNavigateToAlerts    = { navController.navigate("alerts") }
                )
            }

            composable("map") {
                MapScreen(
                    mapView            = mapView,
                    mapViewModel       = mapViewModel,
                    aisViewModel       = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel    = forbudViewModel,
                    searchViewModel    = searchViewModel
                )
            }

            composable("fishlog") {
                FishingLogScreen(
                    viewModel = fishLogViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("addFishingEntry") {
                AddFishingEntryScreen(
                    viewModel = fishLogViewModel,
                    onCancel = { navController.popBackStack() },
                    onSave = { date, time, location, fishType, weight, notes, imageUri ->
                        notes?.let { fishLogViewModel.addEntry(date, time, location, fishType, weight, it, imageUri) }
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
                // TODO: implement WeatherScreen
            }
            composable("favorites") {
                // TODO: implement FavoritesScreen
            }

            composable("profile") {
                ProfileScreen(
                    viewModel         = profileViewModel,
                    onNavigateToHome  = { navController.navigate("home") },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
        }
    }
}
