package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.remote.forbud.BarentsWatchForbudService
import no.uio.ifi.in2000.team46.data.remote.ais.BarentsWatchRetrofitInstance
import okhttp3.ResponseBody

class ForbudViewModel : ViewModel() {
    private val TAG = "ForbudViewModel"

    private val _geoJson = MutableStateFlow<String?>(null)
    val geoJson: StateFlow<String?> = _geoJson

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val authService = BarentsWatchForbudService()
    private val api = BarentsWatchRetrofitInstance.fishHealthApi

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value

        if (_isLayerVisible.value && _geoJson.value == null) {
            fetchForbudData()
        }
    }

    fun fetchForbudData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val token = authService.getAccessToken()

                if (token == null) {
                    _error.value = "Kunne ikke hente token"
                    _isLoading.value = false
                    return@launch
                }

                val response = api.hentForbudGeoJson(token)

                if (response.isSuccessful) {
                    val jsonData: String? = response.body()?.string()
                    _geoJson.value = jsonData
                } else {
                    _error.value = "Feil ved henting: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception under henting av forbudsområder", e)
                _error.value = "Uventet feil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hideLayer() {
        _isLayerVisible.value = false
    }

    fun showLayer() {
        _isLayerVisible.value = true
        if (_geoJson.value == null) {
            fetchForbudData()
        }
    }
}
