package no.uio.ifi.in2000.team46.presentation.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository

class MapViewModelFactory(
    private val locationRepository: LocationRepository,
    private val metAlertsRepository: MetAlertsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(locationRepository, metAlertsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}