package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog

@Dao
interface FishingLogDao {
    @Query("SELECT * FROM fishing_log")
    suspend fun getAllLogs(): List<FishingLog>

    @Query("SELECT * FROM fishing_log")
    fun getAllLogsFlow(): Flow<List<FishingLog>>

    @Query("SELECT * FROM fishing_log WHERE location = :location")
    fun getLogsByLocationFlow(location: String): Flow<List<FishingLog>>

    @Insert
    suspend fun insert(fishingLog: FishingLog)

    @Delete
    suspend fun delete(fishingLog: FishingLog)

    @Query("DELETE FROM fishing_log")
    suspend fun deleteAllLogs()
}