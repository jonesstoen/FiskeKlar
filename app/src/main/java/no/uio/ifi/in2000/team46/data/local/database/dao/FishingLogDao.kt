package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog

@Dao
interface FishingLogDao {
    @Insert
    suspend fun insertLog(log: FishingLog)

    @Query("SELECT * FROM fishing_log")
    fun getAllLogsFlow(): Flow<List<FishingLog>>

    @Delete
    suspend fun deleteLog(log: FishingLog)
}