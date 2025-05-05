package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result as RepoResult

class PrecipitationViewModel(
    private val repo: GribRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    companion object {
        private const val TAG = "PrecipitationVM"
    }

    private val _data = MutableStateFlow<RepoResult<List<PrecipitationPoint>>?>(null)
    val data: StateFlow<RepoResult<List<PrecipitationPoint>>?> = _data

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) fetch()
    }

    private fun fetch() = viewModelScope.launch {
        // Fetch precipitation data from repository
        _isLoading.value = true
        val result = repo.getPrecipitationData()

        // Log full result
        Log.d(TAG, "Raw fetch result: $result")

        when (result) {
            is RepoResult.Success -> {
                val list = result.data
                Log.d(TAG, "Precipitation points count: ${list.size}")
                if (list.isNotEmpty()) {
                    // Log first few points for inspection
                    val sample = list.take(5).joinToString(separator = "; ") { point ->
                        "(lat=${point.lat}, lon=${point.lon}): ${"%.2f".format(point.precipitation)}mm"
                    }
                    Log.d(TAG, "Sample points: $sample")
                }
                _data.value = result
            }
            is RepoResult.Error -> {
                Log.e(TAG, "Error fetching precipitation data", result.exception)
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
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
