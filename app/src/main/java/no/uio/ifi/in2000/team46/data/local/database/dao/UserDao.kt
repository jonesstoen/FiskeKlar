package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.User

// userdao provides methods to get, insert/update, and delete the user entry in the database
// it assumes there is only one user stored, identified with id = 1

@Dao
interface UserDao {

    // retrieves the current user (with id = 1) as a flow
    @Query("SELECT * FROM user WHERE id = 1")
    fun getCurrentUser(): Flow<User?>

    // inserts or updates the user, replacing existing data on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: User)

    // deletes all user data from the user table
    @Query("DELETE FROM user")
    suspend fun clearUser()
}
