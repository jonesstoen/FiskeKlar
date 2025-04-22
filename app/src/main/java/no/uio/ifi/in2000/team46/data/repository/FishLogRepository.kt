package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog

class FishLogRepository(private val context: Context) {
    // fetching the DAO from the database
    private val fishingLogDao = AppDatabase.getDatabase(context).fishingLogDao()
    // fetching all entries from the database
    fun getAllEntries(): Flow<List<FishingLog>> {

        return fishingLogDao.getAllLogsFlow()
    }

    suspend fun addEntry(entry: FishingLog) {
        fishingLogDao.insertLog(entry)
    }

    suspend fun removeEntry(entry: FishingLog) {
        fishingLogDao.deleteLog(entry)
    }
}
