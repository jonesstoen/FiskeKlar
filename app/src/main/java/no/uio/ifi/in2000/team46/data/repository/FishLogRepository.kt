package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.dao.FishingLogDao
import no.uio.ifi.in2000.team46.data.local.database.dao.MostCaughtFish
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import javax.inject.Inject

// fishlogrepository provides access to fishing log data and analytics
// supports inserting, deleting, and observing fishing logs, most caught fish, and top location

class FishLogRepository @Inject constructor(
    private val fishingLogDao: FishingLogDao
) {
    // returns all fishing logs as a reactive flow
    fun getAllLogsFlow(): Flow<List<FishingLog>> {
        return fishingLogDao.getAllLogsFlow()
    }

    // returns the most frequently caught fish type
    fun getMostCaughtFishFlow(): Flow<MostCaughtFish?> {
        return fishingLogDao.getMostCaughtFishFlow()
    }

    // returns the most frequently used fishing location
    fun getFavoriteLocationFlow(): Flow<FavoriteLocation?> {
        return fishingLogDao.getFavoriteLocationFlow()
    }

    // inserts a new fishing log
    suspend fun insert(fishingLog: FishingLog) {
        fishingLogDao.insert(fishingLog)
    }

    // deletes a specific fishing log
    suspend fun delete(fishingLog: FishingLog) {
        fishingLogDao.delete(fishingLog)
    }

    // deletes all fishing logs
    suspend fun deleteAllLogs() {
        fishingLogDao.deleteAllLogs()
    }
}
