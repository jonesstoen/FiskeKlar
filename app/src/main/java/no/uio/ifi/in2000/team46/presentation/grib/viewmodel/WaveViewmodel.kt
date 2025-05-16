package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.data.repository.WaveRepository

// this viewmodel manages wave layer visibility, loading, threshold filtering, time selection,
// and slider controls for adjusting display of GRIB-based wave data

class WaveViewModel(private val repo: WaveRepository) : ViewModel() {

    // stores the result from wave data fetch (success, loading, or error)
    private val _waves = MutableStateFlow<Result<List<WaveVector>>?>(null)
    val waveData: StateFlow<Result<List<WaveVector>>?> = _waves

    // controls visibility of the wave layer on the map
    private val _visible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _visible

    // indicates if raster loading is active (e.g. background image tiles)
    private val _isRasterLoading = MutableStateFlow(false)
    val isRasterLoading: StateFlow<Boolean> = _isRasterLoading

    // threshold value for wave height (e.g. for color or icon filtering)
    private val _waveThreshold = MutableStateFlow(4.0)
    val waveThreshold: StateFlow<Double> = _waveThreshold

    // determines if sliders should be shown in the UI
    private val _showWaveSliders = MutableStateFlow(false)
    val showWaveSliders: StateFlow<Boolean> = _showWaveSliders

    fun setShowWaveSliders(show: Boolean) {
        _showWaveSliders.value = show
    }

    // selected timestamp used to filter wave vectors
    private val _selectedTimestamp = MutableStateFlow<Long?>(null)
    val selectedTimestamp: StateFlow<Long?> = _selectedTimestamp

    // filters the list of wave vectors to match the selected timestamp
    val filteredWaveVectors: StateFlow<List<WaveVector>> = combine(
        waveData,
        selectedTimestamp
    ) { result, selectedTime ->
        if (result is Result.Success && selectedTime != null) {
            result.data.filter { it.timestamp == selectedTime }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setSelectedTimestamp(timestamp: Long) {
        _selectedTimestamp.value = timestamp
    }

    // toggles visibility of the wave layer and loads data if activating
    fun toggleLayer() {
        _visible.value = !_visible.value
        if (_visible.value) fetchWaves()
    }

    // deactivates the wave layer and clears data
    fun deactivateLayer() {
        _visible.value = false
        _waves.value = null
    }


    // sets new threshold value for wave height
    fun setWaveThreshold(value: Double) {
        _waveThreshold.value = value
    }

    // fetches wave vector data from the repository
    private fun fetchWaves() = viewModelScope.launch {
        val result = repo.getWaveData()

        Log.d("WaveViewModel", "Raw waveResult = $result")

        if (result is Result.Success) {
            Log.d("WaveViewModel", "wave vector count: ${result.data.size}")
            Log.d("WaveViewModel", "first 5: ${result.data.take(5)}")

            // selects default timestamp if available
            val firstTimestamp = result.data.firstOrNull()?.timestamp
            if (firstTimestamp != null) {
                _selectedTimestamp.value = firstTimestamp
            }
        }

        _waves.value = result
    }
}

// factory for creating waveviewmodel with injected repository
class WaveViewModelFactory(
    private val repository: WaveRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
