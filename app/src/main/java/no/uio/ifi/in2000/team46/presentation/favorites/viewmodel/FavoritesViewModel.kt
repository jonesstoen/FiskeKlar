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
import no.uio.ifi.in2000.team46.data.local.database.dao.ProcessedSuggestionDao
import no.uio.ifi.in2000.team46.data.local.database.dao.SavedSuggestionDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.data.local.database.entities.ProcessedSuggestion
import no.uio.ifi.in2000.team46.data.local.database.entities.SavedSuggestionEntity
import no.uio.ifi.in2000.team46.data.repository.FavoriteRepository
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository

class FavoritesViewModel (
    private val favoriteRepo: FavoriteRepository,
    private val fishLogRepo: FishLogRepository,
    private val favoriteDao: FavoriteLocationDao,
    private val fishingLogDao: FishingLogDao,
    private val processedSuggestionDao: ProcessedSuggestionDao,
    private val savedSuggestionDao: SavedSuggestionDao
) : ViewModel() {

    // ----------- Favoritter og forslag -----------
    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites

    private val _suggestedLocations = MutableStateFlow<List<SuggestedLocation>>(emptyList())
    val suggestedLocations: StateFlow<List<SuggestedLocation>> = _suggestedLocations

    private val _showInitialNotification = MutableStateFlow(false)
    val showInitialNotification: StateFlow<Boolean> = _showInitialNotification.asStateFlow()

    private val _hasOpenedBellDialog = MutableStateFlow(false)
    val hasOpenedBellDialog: StateFlow<Boolean> = _hasOpenedBellDialog.asStateFlow()

    // ----------- Lagrede forslag -----------
    val savedSuggestions: StateFlow<List<SuggestedLocation>> = savedSuggestionDao.getAll()
        .map { list ->
            list.map { entity ->
                SuggestedLocation(
                    name = entity.name,
                    fishCount = entity.fishCount,
                    latitude = entity.latitude,
                    longitude = entity.longitude
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    // ----------- Prosesserte forslag og varsler -----------
    private val _processedSuggestions = MutableStateFlow<Set<String>>(emptySet())
    private val processedSuggestions = _processedSuggestions.asStateFlow()

    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = savedSuggestionDao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun increaseNotificationCount() { _notificationCount.value++ }
    fun resetNotificationCount() { _notificationCount.value = 0 }
    fun setBellOpened(opened: Boolean) { _hasOpenedBellDialog.value = opened }


    // ----------- Data class for forslag -----------
    data class SuggestedLocation(
        val name: String,
        val fishCount: Int,
        val latitude: Double,
        val longitude: Double
    )

    // ----------- Fangstlogger -----------
    private val fishingLogs: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ----------- Init: Laster favoritter, logger og prosesserte forslag -----------
    init {
        viewModelScope.launch {
            favoriteDao.getAllFavoritesFlow().collect { favorites ->
                _favorites.value = favorites
                checkForNewSuggestions()
            }
        }

        viewModelScope.launch {
            fishingLogDao.getAllLogsFlow().collect {
                checkForNewSuggestions()
            }
        }

        viewModelScope.launch {
            processedSuggestionDao.getAll().collect { processedList ->
                _processedSuggestions.value = processedList.map { it.locationName }.toSet()
                println("Loaded processed suggestions: ${_processedSuggestions.value}")
            }
        }
    }


    // ----------- Sjekk og oppdater forslag -----------
    private fun checkForNewSuggestions() {
        viewModelScope.launch {
            val currentFavorites = _favorites.value.map { it.name }.toSet()
            val logs = fishingLogDao.getAllLogs()
            val locationGroups = logs.groupBy { it.location }
            val newSuggestions = locationGroups
                .filter { (location, logsForLocation) ->
                    logsForLocation.size >= 2 &&
                            location !in currentFavorites &&
                            location !in processedSuggestions.value
                }
                .map { (location, logsForLocation) ->
                    val avgLat = logsForLocation.map { it.latitude }.average()
                    val avgLng = logsForLocation.map { it.longitude }.average()
                    SuggestedLocation(
                        name = location,
                        fishCount = logsForLocation.size,
                        latitude = avgLat,
                        longitude = avgLng
                    )
                }
            _suggestedLocations.value = newSuggestions
            _showInitialNotification.value = newSuggestions.isNotEmpty()
        }
    }
    fun refreshSuggestions() { checkForNewSuggestions() }


    // ----------- Forslags- og favoritt-håndtering -----------
    fun markSuggestionAsProcessed(locationName: String) {
        viewModelScope.launch {
            processedSuggestionDao.insert(ProcessedSuggestion(locationName))
        }
    }

    fun dismissSuggestion(locationName: String, saveForLater: Boolean) {
        val suggestion = _suggestedLocations.value.find { it.name == locationName }
        if (saveForLater && suggestion != null) {
            viewModelScope.launch {
                savedSuggestionDao.insert(
                    SavedSuggestionEntity(
                        name = suggestion.name,
                        fishCount = suggestion.fishCount,
                        latitude = suggestion.latitude,
                        longitude = suggestion.longitude,
                        isRead = false
                    )
                )
            }
        }
        _suggestedLocations.value = _suggestedLocations.value.filter { it.name != locationName }
        _showInitialNotification.value = false
        markSuggestionAsProcessed(locationName)
    }
    fun markAllSuggestionsAsRead() {
        viewModelScope.launch {
            savedSuggestionDao.markAllAsRead()
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
            val logsForLocation = logs.filter { it.location.equals(favorite.name, ignoreCase = true) }
            FavoriteWithStats(
                favorite = favorite,
                catchCount = logsForLocation.size,
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

    fun removeSavedSuggestion(name: String) {
        viewModelScope.launch {
            savedSuggestionDao.deleteByName(name)
        }
    }

    fun updateFavoriteNotes(favorite: FavoriteLocation, newNotes: String) {
        viewModelScope.launch {
            val updated = favorite.copy(notes = newNotes)
            favoriteDao.update(updated)
        }
    }

    class Factory(
        private val favoriteRepo: FavoriteRepository,
        private val fishLogRepo: FishLogRepository,
        private val favoriteDao: FavoriteLocationDao,
        private val fishingLogDao: FishingLogDao,
        private val processedSuggestionDao: ProcessedSuggestionDao,
        private val savedSuggestionDao: SavedSuggestionDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FavoritesViewModel(
                    favoriteRepo, fishLogRepo, favoriteDao, fishingLogDao, processedSuggestionDao, savedSuggestionDao
                ) as T
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