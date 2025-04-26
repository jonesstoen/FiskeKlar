package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_location")
    suspend fun getAllFavorites(): List<FavoriteLocation>

    @Query("SELECT * FROM favorite_location")
    fun getAllFavoritesFlow(): Flow<List<FavoriteLocation>>

    @Insert
    suspend fun insert(favorite: FavoriteLocation)

    @Update
    suspend fun update(favorite: FavoriteLocation)

    @Delete
    suspend fun delete(favorite: FavoriteLocation)

    @Query("SELECT * FROM favorite_location WHERE id = :id")
    fun getFavoriteById(id: Int): Flow<FavoriteLocation?>

    @Query("SELECT * FROM favorite_location WHERE locationType = :type")
    fun getFavoritesByType(type: String): Flow<List<FavoriteLocation>>

    @Query("DELETE FROM favorite_location")
    suspend fun deleteAllFavorite()

}