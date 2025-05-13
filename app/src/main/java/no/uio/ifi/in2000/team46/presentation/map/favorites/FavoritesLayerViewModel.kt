package no.uio.ifi.in2000.team46.presentation.map.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * ViewModel for håndtering av favorittlag på kartet.
 * Ansvarlig for å hente favoritter og styre synligheten til laget.
 */
class FavoritesLayerViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // Favorittlokasjoner
    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites.asStateFlow()

    // Synlighet for laget
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible.asStateFlow()

    // Valgt favoritt for visning av detaljer
    private val _selectedFavorite = MutableStateFlow<FavoriteLocation?>(null)
    val selectedFavorite: StateFlow<FavoriteLocation?> = _selectedFavorite.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Henter alle favorittlokasjoner fra repository
     */
    fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getAllFavoritesFlow().collect { favoritesList ->
                _favorites.value = favoritesList
            }
        }
    }

    /**
     * Skrur favorittlaget på eller av
     */
    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
    }

    /**
     * Setter synligheten til laget
     */
    fun setLayerVisibility(isVisible: Boolean) {
        _isLayerVisible.value = isVisible
    }

    /**
     * Velger en favoritt for visning av detaljer
     */
    fun selectFavorite(favorite: FavoriteLocation?) {
        _selectedFavorite.value = favorite
    }

    /**
     * Konverterer favorittlokasjoner til GeoJSON for kartet
     */
    fun getFavoritesGeoJson(): String {
        val features = JSONArray()
        
        favorites.value.forEach { favorite ->
            try {
                if (favorite.locationType == "POINT" && favorite.latitude != null && favorite.longitude != null) {
                    // Punkt
                    val feature = JSONObject().apply {
                        put("type", "Feature")
                        put("geometry", JSONObject().apply {
                            put("type", "Point")
                            put("coordinates", JSONArray().apply {
                                put(favorite.longitude)
                                put(favorite.latitude)
                            })
                        })
                        put("properties", JSONObject().apply {
                            put("id", favorite.id)
                            put("name", favorite.name)
                            put("type", "POINT")
                            put("notes", favorite.notes ?: "")
                        })
                    }
                    features.put(feature)
                } else if (favorite.locationType == "AREA" && favorite.areaPoints != null) {
                    // Område
                    try {
                        val areaPointsArray = JSONArray(favorite.areaPoints)
                        val coordinates = JSONArray()
                        
                        for (i in 0 until areaPointsArray.length()) {
                            val point = areaPointsArray.getJSONObject(i)
                            val coord = JSONArray().apply {
                                put(point.getDouble("lng"))
                                put(point.getDouble("lat"))
                            }
                            coordinates.put(coord)
                        }
                        
                        // Lukk polygonet ved å legge til første punkt igjen
                        if (areaPointsArray.length() > 0) {
                            val firstPoint = areaPointsArray.getJSONObject(0)
                            val firstCoord = JSONArray().apply {
                                put(firstPoint.getDouble("lng"))
                                put(firstPoint.getDouble("lat"))
                            }
                            coordinates.put(firstCoord)
                        }
                        
                        val feature = JSONObject().apply {
                            put("type", "Feature")
                            put("geometry", JSONObject().apply {
                                put("type", "Polygon")
                                put("coordinates", JSONArray().apply {
                                    put(coordinates)
                                })
                            })
                            put("properties", JSONObject().apply {
                                put("id", favorite.id)
                                put("name", favorite.name)
                                put("type", "AREA")
                                put("notes", favorite.notes ?: "")
                            })
                        }
                        features.put(feature)
                    } catch (e: Exception) {
                        // Håndter feil ved parsing av areaPoints
                    }
                }
            } catch (e: Exception) {
                // Håndter generelle feil
            }
        }
        
        val featureCollection = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }
        
        return featureCollection.toString()
    }
}
