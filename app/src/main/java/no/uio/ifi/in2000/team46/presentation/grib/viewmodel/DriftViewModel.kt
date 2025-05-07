package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.DriftVector
import no.uio.ifi.in2000.team46.domain.usecase.drift.calculateDriftVectors
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result
import androidx.lifecycle.ViewModelProvider
import org.maplibre.geojson.Point


class DriftViewModel(
    private val gribRepository: GribRepository,
    private val currentRepository: CurrentRepository
) : ViewModel() {

    private val _driftData = MutableStateFlow<Result<List<DriftVector>>?>(null)
    val driftData: StateFlow<Result<List<DriftVector>>?> = _driftData

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible
    private val _selectedDriftVector = MutableStateFlow<SelectedDriftVector?>(null)
    val selectedDriftVector: StateFlow<SelectedDriftVector?> = _selectedDriftVector

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) {
            fetchDriftData()
        }
    }
    fun deactivateLayer() {
        _isLayerVisible.value = false
        _driftData.value = null
    }
    fun selectDriftVectorInfo(speed: Double, direction: Double, driftImpact: Double, point: Point) {
        _selectedDriftVector.value = SelectedDriftVector(speed, direction, driftImpact, point)
    }

    fun clearDriftVectorInfo() {
        _selectedDriftVector.value = null
    }

    fun fetchDriftData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val windResult = gribRepository.getWindData(forceRefresh)
            val currentResult = currentRepository.getCurrentData(forceRefresh)

            if (windResult is Result.Success && currentResult is Result.Success) {
                val windVectors = windResult.data
                val currentVectors = currentResult.data
                val driftVectors = calculateDriftVectors(windVectors, currentVectors)
                _driftData.value = Result.Success(driftVectors)
            } else {
                val errorMsg = (windResult as? Result.Error)?.exception?.message
                    ?: (currentResult as? Result.Error)?.exception?.message
                    ?: "Unknown error"
                _driftData.value = Result.Error(Exception(errorMsg))
            }
        }
    }
}
class DriftViewModelFactory(
    private val gribRepository: GribRepository,
    private val currentRepository: CurrentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DriftViewModel(gribRepository, currentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
data class SelectedDriftVector(
    val speed: Double,
    val direction: Double,
    val driftImpact: Double,
    val point: Point
)