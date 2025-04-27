package no.uio.ifi.in2000.team46.presentation.profile.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
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

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel(),ProfileUiContract {

    override val user: StateFlow<User?> = userRepository.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    override fun saveUser(name: String, username: String, imageUri: String?) {
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

    override fun clearUser() {
        viewModelScope.launch {
            userRepository.clearUser()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateMemberSince(): String {
        val now = LocalDate.now()
        val monthFormatted = now.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthFormatted ${now.year}"
    }
}

class ProfileViewModelFactory(private val repo: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repo) as T
    }
}