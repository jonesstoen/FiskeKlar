package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.data.repository.Result

class CurrentViewModel(private val repository: CurrentRepository) : ViewModel() {
    private val _currentData = MutableStateFlow<Result<List<CurrentVector>>?>(null)

    val currentData: StateFlow<Result<List<CurrentVector>>?> = _currentData


    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    private val _currentThreshold = MutableStateFlow(1.0)
    val currentThreshold: StateFlow<Double> = _currentThreshold

    fun setCurrentThreshold(value: Double) {
        _currentThreshold.value = value
    }


    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) {
            fetchCurrentData()
        }
    }
    fun deactivateLayer() {
        _isLayerVisible.value = false
        _currentData.value = null
    }

    private fun fetchCurrentData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val result = repository.getCurrentData(forceRefresh)
            _currentData.value = result
            Log.d("CurrentViewModel", "Fetched current data: $result")

        }
    }

}
class CurrentViewModelFactory(
    private val repository: CurrentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

