package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.AppDatabase
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog

class FishLogRepository(private val context: Context) {
    // Hent DAO fra databasen
    private val fishingLogDao = AppDatabase.getDatabase(context).fishingLogDao()

    fun getAllEntries(): Flow<List<FishingLog>> {
        // Anta at DAO har en funksjon som returnerer Flow<List<FishingLog>>
        // Hvis ikke, kan du selv wrappe resultater i en flow med for eksempel flow { emit(...) }
        return fishingLogDao.getAllLogsFlow()
    }

    suspend fun addEntry(entry: FishingLog) {
        fishingLogDao.insertLog(entry)
    }

    suspend fun removeEntry(entry: FishingLog) {
        fishingLogDao.deleteLog(entry)
    }
}
