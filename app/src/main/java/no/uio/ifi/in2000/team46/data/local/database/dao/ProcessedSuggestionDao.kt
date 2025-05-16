package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.ProcessedSuggestion
/**
 * processedsuggestiondao provides access to the processed_suggestions table.
 * it allows reading all processed suggestions and inserting new ones while ignoring duplicates.
 */
@Dao
interface ProcessedSuggestionDao {

    // returns all processed suggestions as a flow for reactive updates
    @Query("SELECT * FROM processed_suggestions")
    fun getAll(): Flow<List<ProcessedSuggestion>>

    // inserts a new processed suggestion, ignores if it already exists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(processedSuggestion: ProcessedSuggestion)
}
