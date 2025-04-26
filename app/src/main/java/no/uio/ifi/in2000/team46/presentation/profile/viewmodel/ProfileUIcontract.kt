package no.uio.ifi.in2000.team46.presentation.profile.viewmodel

import kotlinx.coroutines.flow.StateFlow
import no.uio.ifi.in2000.team46.data.local.database.entities.User

interface ProfileUiContract {
    val user: StateFlow<User?>
    fun saveUser(name: String, username: String, imageUri: String?)
    fun clearUser()
}