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

// this viewmodel handles wind layer visibility, data loading, user settings like wind threshold,
// timestamp selection, and menu state for grib-based weather visualization

// WARNIGNS: the warning is related to functionality not yet implemented in the app

class GribViewModel(
    private val repository: GribRepository
) : ViewModel() {

    // stores the selected timestamp used to filter wind vectors
    private val _selectedTimestamp = MutableStateFlow<Long?>(null)
    val selectedTimestamp: StateFlow<Long?> = _selectedTimestamp

    // stores wind vector data result from repository
    private val _windData = MutableStateFlow<Result<List<WindVector>>?>(null)
    val windData: StateFlow<Result<List<WindVector>>?> = _windData

    // controls visibility of the wind layer on the map
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    // holds the threshold above which wind vectors are considered hazardous
    private val _windThreshold = MutableStateFlow(12.0) // default to 12 m/s
    val windThreshold: StateFlow<Double> = _windThreshold

    // shows or hides wind layer sliders in the UI
    private val _showWindSliders = MutableStateFlow(false)
    val showWindSliders: StateFlow<Boolean> = _showWindSliders

    // controls which menu section is currently visible in the GRIB settings UI
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

    // updates which timestamp is selected
    fun setSelectedTimestamp(timestamp: Long) {
        _selectedTimestamp.value = timestamp
    }

    // filters wind vectors to only include those from the selected timestamp
    val filteredWindVectors: StateFlow<List<WindVector>> = combine(
        windData,
        selectedTimestamp
    ) { result, selectedTime ->
        if (result is Result.Success && selectedTime != null) {
            result.data.filter { it.timestamp == selectedTime }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // fetches wind data from the repository (can force refresh if needed)
    private fun fetchWindData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val result = repository.getWindData(forceRefresh)
            _windData.value = result

            if (result is Result.Success && result.data.isNotEmpty()) {
                // set first timestamp if none selected
                if (_selectedTimestamp.value == null) {
                    val firstTimestamp = result.data.first().timestamp
                    _selectedTimestamp.value = firstTimestamp
                }
            }
        }
    }

    // makes the wind layer visible and loads data
    fun activateLayer() {
        _isLayerVisible.value = true
        fetchWindData(forceRefresh = false)
    }

    // hides the wind layer and resets data
    fun deactivateLayer() {
        _isLayerVisible.value = false
        _windData.value = null
    }
}

// factory for creating GribViewModel with custom repository
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
