package no.uio.ifi.in2000.team46.presentation.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocationDao
import no.uio.ifi.in2000.team46.data.local.database.dao.FishingLogDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository

class FavoritesViewModel (
    private val favoriteRepo: FavoriteRepository,
    private val fishLogRepo: FishLogRepository
) : ViewModel() {

    // ----------- Favoritter -----------
    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites

    // ----------- Fangstlogger -----------
    private val fishingLogs: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ----------- Init: Laster favoritter -----------
    init {
        viewModelScope.launch {
            favoriteRepo.getAllFavoritesFlow().collect { favorites ->
                _favorites.value = favorites
            }
        }
    }

    // ----------- Filtertype for favorittliste -----------
    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    // ----------- Kombinert favoritt + statistikk -----------
    val favoritesWithStats: StateFlow<List<FavoriteWithStats>> = combine(
        favorites,
        fishingLogs
    ) { favs, logs ->
        favs.map { favorite ->
            val logsForLocation = logs.filter { 
                it.location.equals(favorite.name, ignoreCase = true) && it.count > 0 
            }
            FavoriteWithStats(
                favorite = favorite,
                catchCount = logsForLocation.sumOf { it.count },
                lastCatch = logsForLocation.maxByOrNull { "${it.date} ${it.time}" },
                bestCatch = logsForLocation.maxByOrNull { it.weight }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun filterByType(type: String?) {
        _filterType.value = type
    }

    fun addFavorite(
        name: String,
        locationType: String,
        latitude: Double,
        longitude: Double,
        areaPoints: List<Pair<Double, Double>>? = null,
        notes: String? = null,
        targetFishTypes: List<String>? = null
    ) {
        viewModelScope.launch {
            val favorite = FavoriteLocation(
                name = name,
                locationType = locationType,
                latitude = latitude,
                longitude = longitude,
                areaPoints = areaPoints?.let { favoriteRepo.pointsToJsonString(it) },
                notes = notes,
                targetFishTypes = targetFishTypes?.joinToString(",")
            )
            favoriteRepo.insertFavorite(favorite)
        }
    }

    fun deleteFavorite(favorite: FavoriteLocation) {
        viewModelScope.launch {
            favoriteRepo.deleteFavorite(favorite)
        }
    }

    fun getFavoriteById(id: Int): Flow<FavoriteLocation?> {
        return favoriteRepo.getFavoriteById(id)
    }

    fun getFishingLogsForLocation(locationName: String): Flow<List<FishingLog>> {
        return fishingLogs.map { logs ->
            logs.filter { log -> log.location.equals(locationName, ignoreCase = true) }
        }
    }

    fun calculateAreaInSquareKm(points: List<Pair<Double, Double>>): Double {
        return favoriteRepo.calculateAreaInSquareKm(points)
    }

    fun updateFavoriteNotes(favorite: FavoriteLocation, newNotes: String) {
        viewModelScope.launch {
            val updated = favorite.copy(notes = newNotes)
            favoriteRepo.updateFavorite(updated)
        }
    }

    class Factory(
        private val favoriteRepo: FavoriteRepository,
        private val fishLogRepo: FishLogRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FavoritesViewModel(favoriteRepo, fishLogRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class FavoriteWithStats(
    val favorite: FavoriteLocation,
    val catchCount: Int,
    val lastCatch: FishingLog?,
    val bestCatch: FishingLog?
)