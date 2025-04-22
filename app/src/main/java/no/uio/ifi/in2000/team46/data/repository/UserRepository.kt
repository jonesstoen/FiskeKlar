package no.uio.ifi.in2000.team46.data.repository


import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.UserDao
import no.uio.ifi.in2000.team46.data.local.database.entities.User

class UserRepository(private val userDao: UserDao) {

    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()

    suspend fun saveUser(user: User) = userDao.insertOrUpdate(user)

    suspend fun clearUser() = userDao.clearUser()
}
