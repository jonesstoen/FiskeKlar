package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository

class MetAlertsViewModelFactory(private val repository: MetAlertsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetAlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MetAlertsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}