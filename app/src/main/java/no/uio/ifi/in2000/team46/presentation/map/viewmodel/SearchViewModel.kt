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

class SearchViewModel : ViewModel() {
    private val repository = GeocodingRepository()
    private var searchJob: Job? = null

    private val _searchResults = MutableStateFlow<List<Feature>>(emptyList())
    val searchResults: StateFlow<List<Feature>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Lagre søkehistorikk
    private val _searchHistory = MutableStateFlow<List<Feature>>(emptyList())
    val searchHistory: StateFlow<List<Feature>> = _searchHistory.asStateFlow()

    // Tilstand for om vi skal vise søkehistorikk
    private val _showingHistory = MutableStateFlow(false)
    val showingHistory: StateFlow<Boolean> = _showingHistory.asStateFlow()

    private val apiKey = "6f364c45-6e52-499b-95d1-f310d775e490"

    fun search(query: String, focusLat: Double? = null, focusLon: Double? = null) {
        if (query.isEmpty()) {
            _showingHistory.value = true
            _searchResults.value = _searchHistory.value.take(5)
            return
        } else {
            _showingHistory.value = false
        }

        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _isSearching.value = true
                delay(300) // Debounce delay

                val response = repository.search(
                    query = query,
                    apiKey = apiKey,
                    focusLat = focusLat,
                    focusLon = focusLon
                )

                // Forbedret sortering og filtrering av resultater
                val refinedResults = response.features
                    .distinctBy { it.properties.name } // Fjern duplikater basert på navn
                    .sortedWith(compareBy<Feature> {
                        // Prioriter adresser og gater høyere
                        when {
                            it.properties.name.contains(query, ignoreCase = true) -> 0
                            it.properties.name.startsWith(query, ignoreCase = true) -> 1
                            it.properties.name.contains(" $query", ignoreCase = true) -> 2
                            else -> 3
                        }
                    }.thenBy { feature ->
                        // Sekundært sorter på avstand
                        if (focusLat != null && focusLon != null && feature.geometry.coordinates.size >= 2) {
                            val lat = feature.geometry.coordinates[1]
                            val lon = feature.geometry.coordinates[0]
                            calculateDistance(focusLat, focusLon, lat, lon)
                        } else {
                            Double.MAX_VALUE
                        }
                    })

                _searchResults.value = refinedResults
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Søkefeil: ${e.message}")
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addToHistory(feature: Feature) {
        val currentHistory = _searchHistory.value.toMutableList()

        // Fjern duplikater
        currentHistory.removeIf { it.properties.name == feature.properties.name }

        // Legg til den nye på toppen
        currentHistory.add(0, feature)

        // Begrens til 10 elementer
        _searchHistory.value = currentHistory.take(10)
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _isSearching.value = false
        _showingHistory.value = false
        searchJob?.cancel()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth's radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}