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

// summary: manages favorite layer on map, responsible for loading favorites, controlling layer visibility and converting favorites to geojson

class FavoritesLayerViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // favorite locations state flow
    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites.asStateFlow()

    // visibility state flow for the favorites layer
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible.asStateFlow()

    // selected favorite for showing details
    private val _selectedFavorite = MutableStateFlow<FavoriteLocation?>(null)
    val selectedFavorite: StateFlow<FavoriteLocation?> = _selectedFavorite.asStateFlow()

    init {
        loadFavorites()
    }

    // loads all favorite locations from repository and updates state flow
    fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getAllFavoritesFlow().collect { favoritesList ->
                _favorites.value = favoritesList
            }
        }
    }

    // toggles visibility of the favorites layer on the map
    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
    }

    // sets visibility of the favorites layer explicitly
    fun setLayerVisibility(isVisible: Boolean) {
        _isLayerVisible.value = isVisible
    }

    // selects a favorite location to show its details
    fun selectFavorite(favorite: FavoriteLocation?) {
        _selectedFavorite.value = favorite
    }

    // converts favorite locations into GeoJSON string for map display
    fun getFavoritesGeoJson(): String {
        val features = JSONArray()

        favorites.value.forEach { favorite ->
            try {
                if (favorite.locationType == "POINT") {
                    // create feature for point location
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
                    // create feature for polygon area
                    try {
                        val areaPointsArray = JSONArray(favorite.areaPoints)
                        val coordinates = JSONArray()

                        for (i in 0 until areaPointsArray.length()) {
                            val point = areaPointsArray.getJSONObject(i)
                            // extract lng and lat for each point
                            val coord = JSONArray().apply {
                                put(point.getDouble("lng"))
                                put(point.getDouble("lat"))
                            }
                            coordinates.put(coord)
                        }

                        // close polygon by adding first point at end
                        if (areaPointsArray.length() > 0) {
                            val firstPoint = areaPointsArray.getJSONObject(0)
                            val firstCoord = JSONArray().apply {
                                put(firstPoint.getDouble("lng"))
                                put(firstPoint.getDouble("lat"))
                            }
                            coordinates.put(firstCoord)
                        }

                        // build polygon feature object
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
                        // handle parsing errors for areaPoints
                    }
                }
            } catch (e: Exception) {
                // handle any general errors during geojson conversion
            }
        }

        // assemble final feature collection
        val featureCollection = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }

        return featureCollection.toString()
    }
}
