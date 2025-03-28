package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.domain.model.metalerts.MetAlertsResponse
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.model.metalerts.Feature


class MetAlertsViewModel(private val repository: MetAlertsRepository) : ViewModel() {

    private val _metAlertsJson = MutableStateFlow<String?>(null)
    val metAlertsJson: StateFlow<String?> = _metAlertsJson

    private val _metAlertsResponse = MutableStateFlow<MetAlertsResponse?>(null)
    val metAlertsResponse: StateFlow<MetAlertsResponse?> = _metAlertsResponse

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    // Selected feature for display in detail view
    private val _selectedFeature = MutableStateFlow<Feature?>(null)
    val selectedFeature: StateFlow<Feature?> = _selectedFeature
    // Select a feature by ID
    fun selectFeature(featureId: String?) {
        _selectedFeature.value = featureId?.let { id ->
            _metAlertsResponse.value?.features?.find { it.properties.id == id }
        }
    }

    init {
        fetchMetAlerts()
    }

    private fun fetchMetAlerts() {
        viewModelScope.launch {
            when (val result = repository.getAlerts()) {
                is Result.Success -> {
                    // Store the complete response
                    _metAlertsResponse.value = result.data

                    // Create GeoJSON for map display
                    val geoJson = mapOf(
                        "type" to "FeatureCollection",
                        "features" to result.data.features
                    )
                    _metAlertsJson.value = Gson().toJson(geoJson)
                }
                is Result.Error -> {
                    Log.e("MetAlertsViewModel", "Error fetching alerts: ${result.exception.message}")
                }
            }
        }
    }

    fun activateLayer() {
        if (_isLayerVisible.value != true) {
            _isLayerVisible.value = true
        }
    }

    fun deactivateLayer() {
        if (_isLayerVisible.value == true) {
            _isLayerVisible.value = false
        }
    }

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
    }
}
