package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.SavedSuggestionEntity

@Dao
interface SavedSuggestionDao {
    @Query("SELECT * FROM saved_suggestions")
    fun getAll(): Flow<List<SavedSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedSuggestion: SavedSuggestionEntity)

    @Delete
    suspend fun delete(savedSuggestion: SavedSuggestionEntity)

    @Query("DELETE FROM saved_suggestions")
    suspend fun deleteAll()

    @Query("DELETE FROM saved_suggestions WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("UPDATE saved_suggestions SET isRead = 1 WHERE name = :name")
    suspend fun markAsRead(name: String)

    @Query("UPDATE saved_suggestions SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("SELECT COUNT(*) FROM saved_suggestions WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}
