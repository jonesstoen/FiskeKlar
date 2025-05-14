package no.uio.ifi.in2000.team46.presentation.map.metalerts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.MetAlertsRepository
import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.metalerts.Feature


class MetAlertsViewModel(private val repository: MetAlertsRepository) : ViewModel() {

    private val _metAlertsJson = MutableStateFlow<String?>(null)
    val metAlertsJson: StateFlow<String?> = _metAlertsJson

    private val _metAlertsResponse = MutableStateFlow<MetAlertsResponse?>(null)
    val metAlertsResponse: StateFlow<MetAlertsResponse?> = _metAlertsResponse

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    // Selected feature for display in detail view
    private val _selectedMetalert = MutableStateFlow<Feature?>(null)
    val selectedFeature: StateFlow<Feature?> = _selectedMetalert
    // Select a feature by ID
    fun selectFeature(featureId: String?) {
        _selectedMetalert.value = featureId?.let { id ->
            _metAlertsResponse.value?.features?.find { it.properties.id == id }
        }
    }
    //method  to filter out marine alerts, we dont need alerts for forest fires or other land related alerts
    fun filterSeaAlerts(response: MetAlertsResponse): MetAlertsResponse {
        val filteredFeatures = response.features.filter {
            it.properties.geographicDomain == "marine" // only show marine related warnings
        }
        return response.copy(features = filteredFeatures)
    }
    //method for filtering and assigning icons to the features
    fun filterAndAssignIcons(response: MetAlertsResponse): List<Pair<Feature, String>> {
        // Definerer mapping mellom event-typer og icon-basename
        val eventTypeToIconMap = mapOf(
            "Wind" to "icon-warning-wind",
            "Rain" to "icon-warning-rain",
            "Flood" to "icon-warning-flood",
            "StormSurge" to "icon-warning-stormsurge",
            "Generic" to "icon-warning-generic"
        )

        // we link the icon to the event type and the risk color
        return response.features.mapNotNull { feature ->
            val eventType = feature.properties.awarenessType.split(";").getOrNull(1)?.trim() // Ekstraherer event-type
            val riskColor = feature.properties.riskMatrixColor // Ekstraherer riskMatrixColor
            val iconBase = eventTypeToIconMap[eventType] // Slå opp i mappingen

            if (iconBase != null && riskColor in listOf("Yellow", "Orange", "Red")) {
                val icon = "$iconBase-${riskColor.lowercase()}" // F.eks. "icon-warning-wind-yellow"
                feature to icon
            } else {
                null
            }
        }
    }
    // inject icon property to the geojson response in order to display the icon on the map
    private fun injectIconProperty(response: MetAlertsResponse, iconsMapping: List<Pair<Feature, String>>): String {
        val gson = Gson()
        //converts the response to a json object
        val jsonObject = gson.toJsonTree(response).asJsonObject
        val featuresArray = jsonObject.getAsJsonArray("features")

        //building a mapping from feature id to icon name
        val iconById = iconsMapping.associate { it.first.properties.id to it.second }
        // iterating through the features and adding the icon property to the properties
        featuresArray.forEach { jsonElement ->
            val featureObj = jsonElement.asJsonObject
            val properties = featureObj.getAsJsonObject("properties")

            val featureId = properties.get("id").asString
            iconById[featureId]?.let { iconValue ->
                properties.addProperty("icon", iconValue)
            }
        }
        return gson.toJson(jsonObject)
    }

    init {
        fetchMetAlerts()
    }

    private fun fetchMetAlerts() {
        viewModelScope.launch {
            when (val result = repository.getAlerts()) {
                is Result.Success -> {

                    // filtering out the land related warnings
                    val filteredResponse = filterSeaAlerts(result.data)
                    _metAlertsResponse.value = result.data

                    // assign icons to the features
                    val featuresWithIcons = filterAndAssignIcons(filteredResponse)


                    //injecting the icon property to the geojson response
                    val geoJsonWithIcons = injectIconProperty(filteredResponse, featuresWithIcons)


                    _metAlertsJson.value = geoJsonWithIcons
                }
                is Result.Error -> {
                    Log.e("MetAlertsViewModel", "Error fetching alerts: ${result.exception.message}")
                }
            }
        }
    }

    fun activateLayer() {
        if (!_isLayerVisible.value) {
            _isLayerVisible.value = true
        }
    }

    fun deactivateLayer() {
        if (_isLayerVisible.value) {
            _isLayerVisible.value = false
        }
    }

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
    }
}
