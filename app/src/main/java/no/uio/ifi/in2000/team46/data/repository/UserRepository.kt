package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.UserDao
import no.uio.ifi.in2000.team46.data.local.database.entities.User

// userrepository provides access to user-related operations in the local database
// supports observing the current user and saving or clearing user data

class UserRepository(private val userDao: UserDao) {

    // returns a flow emitting the current user (if any)
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()

    // saves or updates the current user in the database
    suspend fun saveUser(user: User) = userDao.insertOrUpdate(user)

    // clears the current user from the database
    suspend fun clearUser() = userDao.clearUser()
}
