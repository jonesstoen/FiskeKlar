package no.uio.ifi.in2000.team46.presentation.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team46.data.remote.api.Feature
import no.uio.ifi.in2000.team46.data.repository.GeocodingRepository
import android.util.Log

// summary: handles place search logic with debounce, result refinement, and history management

class SearchViewModel : ViewModel() {
    // repository used for geocoding API requests
    private val repository = GeocodingRepository()
    // current search job for debounce and cancellation
    private var searchJob: Job? = null

    // state flow for search results list
    private val _searchResults = MutableStateFlow<List<Feature>>(emptyList())
    val searchResults: StateFlow<List<Feature>> = _searchResults.asStateFlow()

    // state flow indicating whether a search request is ongoing
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // state flow storing recent search history
    private val _searchHistory = MutableStateFlow<List<Feature>>(emptyList())
    val searchHistory: StateFlow<List<Feature>> = _searchHistory.asStateFlow()

    // state flow controlling display of history vs results
    private val _showingHistory = MutableStateFlow(false)
    val showingHistory: StateFlow<Boolean> = _showingHistory.asStateFlow()

    // api key for geocoding service
    private val apiKey = "6f364c45-6e52-499b-95d1-f310d775e490"

    /**
     * performs search with debounce, refines response, and updates state
     * @param query search text input
     * @param focusLat optional latitude to prioritize nearby results
     * @param focusLon optional longitude to prioritize nearby results
     */
    fun search(query: String, focusLat: Double? = null, focusLon: Double? = null) {
        if (query.isEmpty()) {
            // show history when query blank, limit to top 5
            _showingHistory.value = true
            _searchResults.value = _searchHistory.value.take(5)
            return
        } else {
            _showingHistory.value = false
        }

        if (query.length < 2) {
            // require at least 2 chars for search
            _searchResults.value = emptyList()
            return
        }

        // cancel previous in-flight search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _isSearching.value = true
                delay(300) // debounce interval

                // fetch raw results from repository
                val response = repository.search(
                    query = query,
                    apiKey = apiKey,
                    focusLat = focusLat,
                    focusLon = focusLon
                )

                // refine results: remove duplicates and sort by relevance and distance
                val refinedResults = response.features
                    .distinctBy { it.properties.name } // drop duplicate names
                    .sortedWith(
                        compareBy<Feature> {
                            // prioritize name matches containing or starting with query
                            when {
                                it.properties.name.contains(query, ignoreCase = true) -> 0
                                it.properties.name.startsWith(query, ignoreCase = true) -> 1
                                it.properties.name.contains(" $query", ignoreCase = true) -> 2
                                else -> 3
                            }
                        }.thenBy { feature ->
                            // secondary sort by distance if focus provided
                            if (focusLat != null && focusLon != null && feature.geometry.coordinates.size >= 2) {
                                val lat = feature.geometry.coordinates[1]
                                val lon = feature.geometry.coordinates[0]
                                calculateDistance(focusLat, focusLon, lat, lon)
                            } else {
                                Double.MAX_VALUE
                            }
                        }
                    )

                _searchResults.value = refinedResults
            } catch (e: Exception) {
                // log search errors and clear results
                Log.e("SearchViewModel", "search error: ${e.message}")
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * adds a feature to search history and maintains max size of 10
     * removes duplicate names and inserts at top
     */
    fun addToHistory(feature: Feature) {
        val currentHistory = _searchHistory.value.toMutableList()
        currentHistory.removeIf { it.properties.name == feature.properties.name }
        currentHistory.add(0, feature)
        _searchHistory.value = currentHistory.take(10)
    }

    /** clears all saved search history */
    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    /** cancels ongoing search, clears results and history display state */
    fun clearResults() {
        _searchResults.value = emptyList()
        _isSearching.value = false
        _showingHistory.value = false
        searchJob?.cancel()
    }

    //disclaimer this formula was given by chatGPT
    /** calculates distance in meters between two lat/lon points using haversine formula */
    private fun calculateDistance(startLat: Double, startLon: Double, endLat: Double, endLon: Double): Double {
        // haversine formula to calculate distance in meters between two geographic coordinates
        val R = 6371e3 // earth radius in meters
        val phi1 = startLat * Math.PI / 180
        val phi2 = endLat * Math.PI / 180
        val deltaPhi = (endLat - startLat) * Math.PI / 180
        val deltaLambda = (endLon - startLon) * Math.PI / 180

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}
