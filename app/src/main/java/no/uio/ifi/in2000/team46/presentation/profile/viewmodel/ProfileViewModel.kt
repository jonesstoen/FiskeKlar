package no.uio.ifi.in2000.team46.presentation.profile.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import no.uio.ifi.in2000.team46.data.repository.UserRepository
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.database.entities.User
import java.time.LocalDate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.dao.MostCaughtFish
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository

// this viewmodel provides profile-related state and logic such as user data, theme setting,
// and statistics about fishing logs (most caught fish and favorite location)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val dataStore: DataStore<Preferences>,
    private val fishLogRepository: FishLogRepository
) : ViewModel() {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
    }

    // current user stored as stateflow
    val user: StateFlow<User?> = userRepository.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    // current theme setting stored in datastore
    val theme: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: "system"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    // most frequently caught fish based on local database
    val mostCaughtFish: StateFlow<MostCaughtFish?> = fishLogRepository.getMostCaughtFishFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // favorite fishing location based on frequency
    val favoriteLocation: StateFlow<FavoriteLocation?> = fishLogRepository.getFavoriteLocationFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // updates the current theme in datastore
    fun setTheme(newTheme: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[THEME_KEY] = newTheme
            }
        }
    }

    // saves a new or updated user profile
    fun saveUser(name: String, username: String, imageUri: String?) {
        viewModelScope.launch {
            val memberSince = generateMemberSince()
            userRepository.saveUser(
                User(
                    id = 1,
                    name = name,
                    username = username,
                    memberSince = memberSince,
                    profileImageUri = imageUri
                )
            )
        }
    }

    // deletes user profile from storage
    fun clearUser() {
        viewModelScope.launch {
            userRepository.clearUser()
        }
    }

    // formats current date to a string like "May 2025"
    private fun generateMemberSince(): String {
        val now = LocalDate.now()
        val monthFormatted = now.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthFormatted ${now.year}"
    }
}

// factory for constructing profileviewmodel with custom arguments
class ProfileViewModelFactory(
    private val repo: UserRepository,
    private val dataStore: DataStore<Preferences>,
    private val fishLogRepository: FishLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repo, dataStore, fishLogRepository) as T
    }
}
