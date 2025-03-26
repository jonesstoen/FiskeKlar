package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository


class MetAlertsViewModel(private val repository: MetAlertsRepository) : ViewModel() {

    private val _metAlertsJson = MutableLiveData<String?>()
    val metAlertsJson: LiveData<String?> = _metAlertsJson

    private val _isLayerVisible = MutableLiveData(false)
    val isLayerVisible: LiveData<Boolean> = _isLayerVisible

    init {
        fetchMetAlerts()
    }

    private fun fetchMetAlerts() {
        viewModelScope.launch {
            val json = repository.fetchMetAlertsJson()
            if (json != null) {
                _metAlertsJson.value = json
            } else {
                // feilhåndtering
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
        val currentVisibility = _isLayerVisible.value ?: false
        _isLayerVisible.value = !currentVisibility
    }
}
