package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
/**
 * fishinglogdao handles all database operations related to fishing log entries.
 * it includes methods for inserting, deleting, retrieving all logs, and aggregating data
 * such as the most caught fish and the most popular fishing location.
 */
@Dao
interface FishingLogDao {

    // retrieves all fishing logs as a one-time list
    @Query("SELECT * FROM fishing_log")
    suspend fun getAllLogs(): List<FishingLog>

    // retrieves all fishing logs as a flow for reactive updates
    @Query("SELECT * FROM fishing_log")
    fun getAllLogsFlow(): Flow<List<FishingLog>>

    // retrieves fishing logs filtered by location, returned as a flow
    @Query("SELECT * FROM fishing_log WHERE location = :location")
    fun getLogsByLocationFlow(location: String): Flow<List<FishingLog>>

    // returns the most caught fish type with total count, using group by and limit
    @Query("SELECT fishType, SUM(count) as totalCount FROM fishing_log GROUP BY fishType ORDER BY totalCount DESC LIMIT 1")
    fun getMostCaughtFishFlow(): Flow<MostCaughtFish?>

    // returns the most frequently used fishing location based on number of entries
    @Query("SELECT location, COUNT(*) as entryCount FROM fishing_log GROUP BY location ORDER BY entryCount DESC LIMIT 1")
    fun getFavoriteLocationFlow(): Flow<FavoriteLocation?>

    // inserts a new fishing log entry into the database
    @Insert
    suspend fun insert(fishingLog: FishingLog)

    // deletes a specific fishing log entry from the database
    @Delete
    suspend fun delete(fishingLog: FishingLog)

    // deletes all fishing log entries from the database
    @Query("DELETE FROM fishing_log")
    suspend fun deleteAllLogs()
}

// holds result for the most caught fish type and its total count
data class MostCaughtFish(
    val fishType: String,
    val totalCount: Int
)

// holds result for the favorite fishing location and its entry count
data class FavoriteLocation(
    val location: String,
    val entryCount: Int
)
