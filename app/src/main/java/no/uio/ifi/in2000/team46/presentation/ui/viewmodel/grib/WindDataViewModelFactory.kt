package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.grib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team46.data.repository.GribRepository

class WindDataViewModelFactory(private val gribRepository: GribRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WindDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WindDataViewModel(gribRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}