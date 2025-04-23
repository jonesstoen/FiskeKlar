package no.uio.ifi.in2000.team46.presentation.grib
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import no.uio.ifi.in2000.team46.data.repository.Result

class GribViewModel(
    private val repository: GribRepository
) : ViewModel() {

    private val _windData = MutableStateFlow<Result<List<WindVector>>?>(null)
    val windData: StateFlow<Result<List<WindVector>>?> = _windData

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    fun fetchWindData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val result = repository.getWindData(forceRefresh)
            _windData.value = result
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
