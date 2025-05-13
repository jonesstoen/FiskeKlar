package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.SavedSuggestionEntity

/**
 * savedsuggestiondao handles all database operations for saved suggestions.
 * it includes methods to insert, delete, update read status, and retrieve suggestions,
 * including unread count as a flow.
 */
@Dao
interface SavedSuggestionDao {

    // returns all saved suggestions as a flow for reactive observation
    @Query("SELECT * FROM saved_suggestions")
    fun getAll(): Flow<List<SavedSuggestionEntity>>

    // inserts a suggestion, replaces if one with the same primary key exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedSuggestion: SavedSuggestionEntity)

    // deletes a specific saved suggestion
    @Delete
    suspend fun delete(savedSuggestion: SavedSuggestionEntity)

    // deletes all saved suggestions from the table
    @Query("DELETE FROM saved_suggestions")
    suspend fun deleteAll()

    // deletes a saved suggestion based on its name
    @Query("DELETE FROM saved_suggestions WHERE name = :name")
    suspend fun deleteByName(name: String)

    // marks a suggestion with the given name as read
    @Query("UPDATE saved_suggestions SET isRead = 1 WHERE name = :name")
    suspend fun markAsRead(name: String)

    // marks all suggestions as read
    @Query("UPDATE saved_suggestions SET isRead = 1")
    suspend fun markAllAsRead()

    // returns a flow of the number of unread suggestions
    @Query("SELECT COUNT(*) FROM saved_suggestions WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}
