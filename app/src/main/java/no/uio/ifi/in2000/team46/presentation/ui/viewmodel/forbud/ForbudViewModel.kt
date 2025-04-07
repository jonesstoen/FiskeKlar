package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.ForbudRepository

class ForbudViewModel : ViewModel() {
    private val repository = ForbudRepository()

    private val _geoJson = MutableStateFlow<String?>(null)
    val geoJson: StateFlow<String?> = _geoJson

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    private var fetchJob: Job? = null

    // Sett dette til faktisk GeoJSON-endepunkt
    private val geoJsonUrl = "https://eksempel.no/torskeforbud.geojson" // sett inn riktig url her

    fun toggleLayerVisibility(token: String) {
        val visible = _isLayerVisible.value
        _isLayerVisible.value = !visible

        if (!visible) {
            fetchForbudsområder(token)
        } else {
            _geoJson.value = null
        }
    }

    private fun fetchForbudsområder(token: String) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val json = repository.hentGeoJson(token, geoJsonUrl)
                if (json != null) {
                    _geoJson.value = json
                } else {
                    _error.value = "Klarte ikke å hente GeoJSON-data"
                }
            } catch (e: Exception) {
                _error.value = "Feil: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}
