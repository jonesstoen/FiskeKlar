package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.dao.FishingLogDao
import no.uio.ifi.in2000.team46.data.local.database.dao.MostCaughtFish
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import javax.inject.Inject

class FishLogRepository @Inject constructor(
    private val fishingLogDao: FishingLogDao
) {
    fun getAllLogsFlow(): Flow<List<FishingLog>> {
        return fishingLogDao.getAllLogsFlow()
    }
    
    fun getMostCaughtFishFlow(): Flow<MostCaughtFish?> {
        return fishingLogDao.getMostCaughtFishFlow()
    }
    
    fun getFavoriteLocationFlow(): Flow<FavoriteLocation?> {
        return fishingLogDao.getFavoriteLocationFlow()
    }

    suspend fun insert(fishingLog: FishingLog) {
        fishingLogDao.insert(fishingLog)
    }

    suspend fun delete(fishingLog: FishingLog) {
        fishingLogDao.delete(fishingLog)
    }

    suspend fun deleteAllLogs() {
        fishingLogDao.deleteAllLogs()
    }
}

