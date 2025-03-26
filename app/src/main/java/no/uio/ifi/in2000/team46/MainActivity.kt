package no.uio.ifi.in2000.team46

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.team46.data.remote.RetrofitInstance
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.presentation.ui.screens.MapScreen
import no.uio.ifi.in2000.team46.presentation.ui.theme.TEAM46Theme
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModelFactory
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModelFactory
import no.uio.ifi.in2000.team46.utils.permissions.LocationPermissionManager
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : ComponentActivity() {
    private lateinit var locationPermissionManager: LocationPermissionManager
    private var locationGranted by mutableStateOf(false)
    private val locationRepository by lazy { LocationRepository(this) }
    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(LocationRepository(this))
    }
    private val metAlertsViewModel: MetAlertsViewModel by viewModels {
        MetAlertsViewModelFactory(MetAlertsRepository(RetrofitInstance.metAlertsApi))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "kPH7fJZHXa4Pj6d1oIuw", WellKnownTileServer.MapTiler)
        enableEdgeToEdge()

        locationPermissionManager = LocationPermissionManager(this)
        locationPermissionManager.checkAndRequestPermission { granted ->
            if (granted) {
                mapViewModel.fetchUserLocation(this)
                locationGranted = true
            }
        }

        setContent {
            TEAM46Theme {
                Scaffold { contentPadding ->
                    MapScreen(
                        modifier = Modifier.padding(contentPadding),
                        locationGranted,
                        locationRepository = locationRepository,
                        mapViewModel = mapViewModel,
                        metAlertsViewModel = metAlertsViewModel

                    )
                }
            }
        }
    }
}