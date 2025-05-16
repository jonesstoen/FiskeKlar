package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.data.repository.Result

// this viewmodel handles state for displaying current (ocean stream) data in the map layer
// it manages visibility, loading, time filtering, and user controls like sliders and thresholds

class CurrentViewModel(private val repository: CurrentRepository) : ViewModel() {

    // stores the result from the repository (success, error or loading)
    private val _currentData = MutableStateFlow<Result<List<CurrentVector>>?>(null)
    val currentData: StateFlow<Result<List<CurrentVector>>?> = _currentData

    // toggles visibility of the current data layer
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    // holds the selected timestamp (in epoch millis)
    private val _selectedTimestamp = MutableStateFlow<Long?>(null)
    val selectedTimestamp: StateFlow<Long?> = _selectedTimestamp

    // threshold value to control filtering of current intensity
    private val _currentThreshold = MutableStateFlow(1.0)
    val currentThreshold: StateFlow<Double> = _currentThreshold

    // controls visibility of UI slider components
    private val _showCurrentSliders = MutableStateFlow(false)
    val showCurrentSliders: StateFlow<Boolean> = _showCurrentSliders

    // indicates whether data is being loaded
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setShowCurrentSliders(show: Boolean) {
        _showCurrentSliders.value = show
    }

    fun setSelectedTimestamp(timestamp: Long) {
        _selectedTimestamp.value = timestamp
    }

    // filters the list of current vectors based on the selected timestamp
    val filteredCurrentVectors: StateFlow<List<CurrentVector>> = combine(
        currentData,
        selectedTimestamp
    ) { result, selectedTime ->
        if (result is Result.Success && selectedTime != null) {
            result.data.filter { it.timestamp == selectedTime }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setCurrentThreshold(value: Double) {
        _currentThreshold.value = value
    }

    // toggles the visibility of the current layer and fetches data if becoming visible
    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) {
            fetchCurrentData()
        }
    }

    // hides the layer and resets the data
    fun deactivateLayer() {
        _isLayerVisible.value = false
        _currentData.value = null
    }

    // fetches current data from repository, optionally forcing refresh
    private fun fetchCurrentData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                repository.getCurrentData(forceRefresh)
            }
            _currentData.value = result

            Log.d("CurrentViewModel", "Fetched current data: $result")

            // set first timestamp as default if none is selected yet
            if (result is Result.Success && result.data.isNotEmpty()) {
                if (_selectedTimestamp.value == null) {
                    _selectedTimestamp.value = result.data.first().timestamp
                }
            }
            _isLoading.value = false
        }
    }
}

// factory for creating the CurrentViewModel with a repository instance
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
