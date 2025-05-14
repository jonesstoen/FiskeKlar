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

// viewmodel managing favorites and their related fishing log statistics

class FavoritesViewModel (
    private val favoriteRepo: FavoriteRepository,
    private val fishLogRepo: FishLogRepository
) : ViewModel() {

    // stateflow holding list of favorites
    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites

    // stateflow of all fishing logs from repository
    private val fishingLogs: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // initialize by collecting favorites into _favorites state
    init {
        viewModelScope.launch {
            favoriteRepo.getAllFavoritesFlow().collect { favs ->
                _favorites.value = favs
            }
        }
    }

    // filter type for favorites list (point or area or null for no filter)
    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    // combined state of favorites with calculated stats
    val favoritesWithStats: StateFlow<List<FavoriteWithStats>> = combine(
        favorites,
        fishingLogs
    ) { favs, logs ->
        favs.map { favorite ->
            // filter logs matching this favorite and count > 0
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

    // update the current filter type
    fun filterByType(type: String?) {
        _filterType.value = type
    }

    // add a new favorite to repository
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

    // delete an existing favorite
    fun deleteFavorite(favorite: FavoriteLocation) {
        viewModelScope.launch {
            favoriteRepo.deleteFavorite(favorite)
        }
    }

    // get flow of a single favorite by its id
    fun getFavoriteById(id: Int): Flow<FavoriteLocation?> {
        return favoriteRepo.getFavoriteById(id)
    }

    // get fishing logs for a specific favorite location name
    fun getFishingLogsForLocation(locationName: String): Flow<List<FishingLog>> {
        return fishingLogs.map { logs ->
            logs.filter { it.location.equals(locationName, ignoreCase = true) }
        }
    }

    // calculate area in square kilometers from list of lat-lng points
    fun calculateAreaInSquareKm(points: List<Pair<Double, Double>>): Double {
        return favoriteRepo.calculateAreaInSquareKm(points)
    }

    // update notes for a favorite and save to repository
    fun updateFavoriteNotes(favorite: FavoriteLocation, newNotes: String) {
        viewModelScope.launch {
            val updated = favorite.copy(notes = newNotes)
            favoriteRepo.updateFavorite(updated)
        }
    }

    // factory for creating this viewmodel with required repos
    class Factory(
        private val favoriteRepo: FavoriteRepository,
        private val fishLogRepo: FishLogRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FavoritesViewModel(favoriteRepo, fishLogRepo) as T
            }
            throw IllegalArgumentException("unknown viewmodel class")
        }
    }
}

// data class combining favorite with its stats

data class FavoriteWithStats(
    val favorite: FavoriteLocation,
    val catchCount: Int,
    val lastCatch: FishingLog?,
    val bestCatch: FishingLog?
)
