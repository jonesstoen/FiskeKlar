package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud




import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.remote.forbud.BarentsWatchForbudService
import no.uio.ifi.in2000.team46.data.remote.ais.BarentsWatchRetrofitInstance
import org.json.JSONArray
import org.json.JSONObject

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
                    Log.d(TAG, "Forbud GeoJSON mottatt: $jsonData")

                    val parsed = parseGeoJsonToFeatureCollection(jsonData)

                    if (parsed != null) {
                        _geoJson.value = parsed
                        Log.d(TAG, "GeoJSON satt i stateflow")
                    } else {
                        _error.value = "Kunne ikke parse GeoJSON"
                        Log.e(TAG, "Parsing returnerte null")
                    }
                } else {
                    _error.value = "Feil ved henting: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "Feil fra API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception under henting av forbudsområder", e)
                _error.value = "Uventet feil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseGeoJsonToFeatureCollection(jsonData: String?): String? {
        return try {
            val jsonArray = JSONArray(jsonData)
            val wrapped = JSONObject().apply {
                put("type", "FeatureCollection")
                val features = JSONArray()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)

                    val feature = JSONObject().apply {
                        put("type", "Feature")
                        put("geometry", item.getJSONObject("geometry"))

                        val properties = JSONObject()
                        if (item.has("objectId")) properties.put("objectId", item.get("objectId"))
                        if (item.has("info")) properties.put("info", item.get("info"))

                        put("properties", properties)
                    }

                    features.put(feature)
                }

                put("features", features)
            }

            wrapped.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved parsing av GeoJSON: ${e.message}", e)
            null
        }
    }
}
