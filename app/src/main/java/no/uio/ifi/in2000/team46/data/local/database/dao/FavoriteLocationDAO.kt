package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation

/**
 * DAO interface for managing favorite locations in the database.
 * Provides methods to insert, update, delete, and retrieve favorite locations.
 */

@Dao
interface FavoriteLocationDao {

    // retrieves all favorite locations as a list (suspend function for background thread)
    @Query("SELECT * FROM favorite_location")
    suspend fun getAllFavorites(): List<FavoriteLocation>

    // retrieves all favorite locations as a Flow for observing changes
    @Query("SELECT * FROM favorite_location")
    fun getAllFavoritesFlow(): Flow<List<FavoriteLocation>>

    // inserts a new favorite location into the database
    @Insert
    suspend fun insert(favorite: FavoriteLocation)

    // updates an existing favorite location
    @Update
    suspend fun update(favorite: FavoriteLocation)

    // deletes a specific favorite location
    @Delete
    suspend fun delete(favorite: FavoriteLocation)

    // retrieves a specific favorite by its id as a Flow
    @Query("SELECT * FROM favorite_location WHERE id = :id")
    fun getFavoriteById(id: Int): Flow<FavoriteLocation?>

    // retrieves all favorites of a specific type as a Flow
    @Query("SELECT * FROM favorite_location WHERE locationType = :type")
    fun getFavoritesByType(type: String): Flow<List<FavoriteLocation>>

    // deletes all favorite locations from the table
    @Query("DELETE FROM favorite_location")
    suspend fun deleteAllFavorite()
}
