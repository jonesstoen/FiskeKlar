package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.ProcessedSuggestion

@Dao
interface ProcessedSuggestionDao {
    @Query("SELECT * FROM processed_suggestions")
    fun getAll(): Flow<List<ProcessedSuggestion>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(processedSuggestion: ProcessedSuggestion)
}
