package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result as RepoResult




class PrecipitationViewModel(
    private val repo: GribRepository
) : ViewModel() {

    // tracks if precipitation data is loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // timestamp selected for filtering (epoch millis)
    private val _selectedTimestamp = MutableStateFlow<Long?>(null)
    val selectedTimestamp: StateFlow<Long?> = _selectedTimestamp

    fun setSelectedTimestamp(timestamp: Long) {
        _selectedTimestamp.value = timestamp
    }

    // threshold in mm for highlighting values
    private val _precipThreshold = MutableStateFlow(5.0)
    val precipThreshold: StateFlow<Double> = _precipThreshold

    fun setPrecipThreshold(value: Double) {
        _precipThreshold.value = value
    }

    // full dataset from repository (raw precipitation points)
    private val _data = MutableStateFlow<RepoResult<List<PrecipitationPoint>>?>(null)
    val data: StateFlow<RepoResult<List<PrecipitationPoint>>?> = _data

    // filters the data by selected timestamp (if set)
    val filteredPrecipPoints: StateFlow<List<PrecipitationPoint>> = combine(
        data,
        selectedTimestamp
    ) { result, timestamp ->
        if (result is RepoResult.Success && timestamp != null) {
            result.data.filter { it.timestamp == timestamp }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // controls whether the precipitation layer is visible
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) fetch()
    }

    fun deactivateLayer() {
        _isLayerVisible.value = false
        _data.value = null
    }

    // manages whether sliders (timestamp and threshold) should be visible
    private val _showPrecipSliders = MutableStateFlow(false)
    val showPrecipSliders: StateFlow<Boolean> = _showPrecipSliders

    fun setShowPrecipSliders(visible: Boolean) {
        _showPrecipSliders.value = visible
    }

    // fetches the GRIB precipitation data
    private fun fetch() = viewModelScope.launch {
        _isLoading.value = true
        val result = repo.getPrecipitationData()

        Log.d("PrecipitationVM", "Raw fetch result: $result")

        when (result) {
            is RepoResult.Success -> {
                Log.d("PrecipitationVM", "Precipitation points count: ${result.data.size}")
                result.data.take(5).forEach {
                    Log.d("PrecipitationVM", "(${it.lat}, ${it.lon}): ${it.precipitation} mm at ${it.timestamp}")
                }
                _data.value = result
                result.data.map { it.timestamp }.distinct().minOrNull()?.let {
                    setSelectedTimestamp(it)
                }
            }

            is RepoResult.Error -> {
                Log.e("PrecipitationVM", "Error fetching precipitation data", result.exception)
                _data.value = result
            }
        }

        _isLoading.value = false
    }
}

class PrecipitationViewModelFactory(
    private val repo: GribRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrecipitationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrecipitationViewModel(repo) as T
        }
        throw IllegalArgumentException("unknown ViewModel class")
    }
}


