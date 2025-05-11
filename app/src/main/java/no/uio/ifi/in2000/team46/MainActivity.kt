package no.uio.ifi.in2000.team46

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import no.uio.ifi.in2000.team46.presentation.map.utils.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.navigation.AppNavHost
import no.uio.ifi.in2000.team46.presentation.ui.theme.AppTheme
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModelFactory
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModelFactory
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModelFactory
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.OnboardingViewModel
import no.uio.ifi.in2000.team46.data.local.datastore.dataStore

class MainActivity : ComponentActivity() {
    // Hoist all your ViewModels here so they survive navigation
    private val locationRepository by lazy { LocationRepository(this) }
    private val db by lazy { AppDatabase.getDatabase(this) }

    private val fishLogRepo by lazy {
        FishLogRepository(db.fishingLogDao())
    }
    private val fishTypeRepo by lazy {
        FishTypeRepository(db.fishTypeDao())
    }
    private val weatherService by lazy { WeatherService() }

    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(locationRepository, MetAlertsRepository())
    }
    private val aisViewModel: AisViewModel by viewModels()
    private val forbudViewModel: ForbudViewModel by viewModels()
    private val metAlertsViewModel: MetAlertsViewModel by viewModels {
        MetAlertsViewModelFactory(MetAlertsRepository())
    }
    private val searchViewModel: SearchViewModel by viewModels()

    private val fishLogViewModel: FishingLogViewModel by viewModels {
        FishingLogViewModel.FishLogViewModelFactory(fishLogRepo, fishTypeRepo)
    }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(UserRepository(db.userDao()), dataStore, fishLogRepo)
    }

    private val favoritesViewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(
            FavoriteRepository(db.favoriteLocationDao()),
            fishLogRepo
        )
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize MapLibre
        MapLibre.getInstance(this, "kPH7fJZHXa4Pj6d1oIuw", WellKnownTileServer.MapTiler)
        enableEdgeToEdge()

        // Check if this is first launch
        onboardingViewModel.checkFirstLaunch(this)

        setContent {
            AppTheme(viewModel = profileViewModel) {
                val navController = rememberNavController()
                // Hoist your single MapView
                val mapView = rememberMapViewWithLifecycle()

                AppNavHost(
                    navController = navController,
                    mapView = mapView,
                    mapViewModel = mapViewModel,
                    aisViewModel = aisViewModel,
                    metAlertsViewModel = metAlertsViewModel,
                    forbudViewModel = forbudViewModel,
                    searchViewModel = searchViewModel,
                    fishLogViewModel = fishLogViewModel,
                    profileViewModel = profileViewModel,
                    onboardingViewModel = onboardingViewModel,
                    weatherService = weatherService
                )
            }
        }
    }
}

