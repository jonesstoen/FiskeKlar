package no.uio.ifi.in2000.team46.presentation.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument

import no.uio.ifi.in2000.team46.presentation.ui.screens.FishLog.FishingLogScreen
import no.uio.ifi.in2000.team46.presentation.ui.screens.HomeScreen
import no.uio.ifi.in2000.team46.presentation.ui.screens.MapScreen
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog.FishingLogViewModel
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import no.uio.ifi.in2000.team46.presentation.ui.screens.FishLog.AddFishingEntryScreen
import no.uio.ifi.in2000.team46.presentation.ui.screens.FishLog.FishingLogDetailScreen
import no.uio.ifi.in2000.team46.presentation.ui.screens.ProfileScreen
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.profile.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.profile.ProfileViewModelFactory
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AppNavHost(
    navController: NavHostController,
    fishLogViewModel: FishingLogViewModel,
    locationRepository: LocationRepository,
    mapViewModel: MapViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    aisViewModel: AisViewModel,
    forbudViewModel: ForbudViewModel,
    profileViewModel: ProfileViewModel

) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = profileViewModel,
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToWeather = { navController.navigate("weather") },
                onNavigateToFishLog = { navController.navigate("fishlog") },
                onNavigateToFavorites = { navController.navigate("favorites") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAlerts = { navController.navigate("alerts") }
            )
        }
        composable("map") {
            MapScreen(
                granted = true, //TODO: Implement location permission check logic
                locationRepository = locationRepository,
                mapViewModel = mapViewModel,
                metAlertsViewModel = metAlertsViewModel,
                aisViewModel = aisViewModel,
                forbudViewModel = forbudViewModel,
                onNavigate = { route -> navController.navigate(route) }
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
                onSave = { date: LocalDate, time: LocalTime, location, fishType, weight, notes, imageUri ->
                    if (notes != null) {
                        fishLogViewModel.addEntry(
                            date = date,
                            time = time,
                            location = location,
                            fishType = fishType,
                            weight = weight,
                            notes = notes,
                            imageUri = imageUri
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

        composable("alerts") {

            navController.navigate("home")
        }
        composable("weather") {
            navController.navigate("home")
        }
        composable("favorites") {
            navController.navigate("home")
        }
        composable("profile") {
            val context = LocalContext.current
            val db = remember { AppDatabase.getDatabase(context) }
            val userRepo = remember { UserRepository(db.userDao()) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userRepo))

            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToAlerts = { navController.navigate("alerts") }

            )
        }

    }
}
