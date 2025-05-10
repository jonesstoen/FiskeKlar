package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.map.components.layermenu.GribMenuState

class GribViewModel(
    private val repository: GribRepository
) : ViewModel() {
    private val _selectedTimestamp = MutableStateFlow<Long?>(null)
    val selectedTimestamp: StateFlow<Long?> = _selectedTimestamp

    private val _windData = MutableStateFlow<Result<List<WindVector>>?>(null)
    val windData: StateFlow<Result<List<WindVector>>?> = _windData

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    // holds the threshold above which wind vectors are considered hazardous
    private val _windThreshold = MutableStateFlow(12.0) // default to 12 m/s
    val windThreshold: StateFlow<Double> = _windThreshold

    private val _showWindSliders = MutableStateFlow(false)
    val showWindSliders: StateFlow<Boolean> = _showWindSliders

    private val _gribMenuState = MutableStateFlow<GribMenuState>(GribMenuState.Main)
    val gribMenuState: StateFlow<GribMenuState> = _gribMenuState.asStateFlow()

    fun setGribMenuState(state: GribMenuState) {
        _gribMenuState.value = state
    }



    fun setShowWindSliders(visible: Boolean) {
        _showWindSliders.value = visible
    }

    // updates the threshold value (called when user adjusts slider or setting)
    fun setWindThreshold(value: Double) {
        _windThreshold.value = value
    }

    fun setSelectedTimestamp(timestamp: Long) {
        _selectedTimestamp.value = timestamp
    }

    val filteredWindVectors: StateFlow<List<WindVector>> = combine(
        windData,
        selectedTimestamp
    ) { result, selectedTime ->
        if (result is Result.Success && selectedTime != null) {
            result.data.filter { it.timestamp == selectedTime }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun fetchWindData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val result = repository.getWindData(forceRefresh)
            _windData.value = result

            if (result is Result.Success && result.data.isNotEmpty()) {
                // Velg første timestamp hvis ingen er valgt
                if (_selectedTimestamp.value == null) {
                    val firstTimestamp = result.data.first().timestamp
                    _selectedTimestamp.value = firstTimestamp
                }
            }
        }
    }


    fun activateLayer() {
        _isLayerVisible.value = true
        fetchWindData(forceRefresh = false)
    }

    fun deactivateLayer() {
        _isLayerVisible.value = false
        _windData.value = null
    }
}

class GribViewModelFactory(
    private val repository: GribRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GribViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GribViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
