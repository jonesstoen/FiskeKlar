package no.uio.ifi.in2000.team46

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import no.uio.ifi.in2000.team46.presentation.map.utils.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.navigation.AppNavHost
import no.uio.ifi.in2000.team46.presentation.ui.theme.TEAM46Theme
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.MapViewModelFactory
import no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModelFactory
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModelFactory


class MainActivity : ComponentActivity() {
    // Hoist all your ViewModels here so they survive navigation
    private val locationRepository by lazy { LocationRepository(this) }

    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(locationRepository, MetAlertsRepository())
    }
    private val aisViewModel: AisViewModel by viewModels()
    private val forbudViewModel: ForbudViewModel by viewModels()
    private val metAlertsViewModel: MetAlertsViewModel by viewModels {
        MetAlertsViewModelFactory(MetAlertsRepository())
    }
    private val searchViewModel: SearchViewModel by viewModels()
    private val fishLogRepo by lazy {
        FishLogRepository(this)
    }
    private val fishTypeRepo by lazy {
        FishTypeRepository(
            AppDatabase.getDatabase(this)
                .fishTypeDao()
        )
    }
    private val fishLogViewModel: FishingLogViewModel by viewModels {
        FishingLogViewModel.FishLogViewModelFactory(fishLogRepo, fishTypeRepo)
    }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize MapLibre
        MapLibre.getInstance(this, "kPH7fJZHXa4Pj6d1oIuw", WellKnownTileServer.MapTiler)
        enableEdgeToEdge()

        setContent {
            TEAM46Theme {
                val navController = rememberNavController()
                // Hoist your single MapView
                val mapView = rememberMapViewWithLifecycle()

                AppNavHost(
                    navController       = navController,
                    mapView             = mapView,
                    mapViewModel        = mapViewModel,
                    aisViewModel        = aisViewModel,
                    metAlertsViewModel  = metAlertsViewModel,
                    forbudViewModel     = forbudViewModel,
                    searchViewModel     = searchViewModel,
                    fishLogViewModel    = fishLogViewModel,
                    profileViewModel    = profileViewModel
                )
            }
        }
    }
}

