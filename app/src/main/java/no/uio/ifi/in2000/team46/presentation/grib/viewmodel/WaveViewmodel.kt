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

class WaveViewModel(private val repo: WaveRepository) : ViewModel() {
    private val _waves = MutableStateFlow<Result<List<WaveVector>>?>(null)
    val waveData: StateFlow<Result<List<WaveVector>>?> = _waves

    private val _visible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _visible

    private val _isRasterLoading = MutableStateFlow(false)
    val isRasterLoading: StateFlow<Boolean> = _isRasterLoading
    //treshold
    private val _waveThreshold = MutableStateFlow(4.0)
    val waveThreshold: StateFlow<Double> = _waveThreshold

    fun toggleLayer() {
        _visible.value = !_visible.value
        if (_visible.value) fetchWaves()
    }
    fun deactivateLayer() {
        _visible.value = false
        _waves.value = null
    }
    fun setRasterLoading(loading: Boolean) {
        _isRasterLoading.value = loading
    }
    fun setWaveThreshold(value: Double) {
        _waveThreshold.value = value
    }

    private fun fetchWaves() = viewModelScope.launch {
        val result = repo.getWaveData()
        Log.d("WaveViewModel", "Raw waveResult = $result")
        if (result is Result.Success) {
            Log.d("WaveViewModel", "Antall bølgevektorer: ${result.data.size}")
            Log.d("WaveViewModel", "Første 5: ${result.data.take(5)}")
        }
        _waves.value = result
    }

}
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