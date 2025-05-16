package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.data.remote.datasource.GribDataSource
import java.io.File
import java.io.IOException

// waverepository downloads, caches, and parses wave data from grib files
// uses gribparser to convert binary data into structured wavevector objects

//WARNINGS: the warning in this file are related to  setup for fucntionality that can be added in the future

class WaveRepository(
    private val api: GribDataSource,
    private val context: Context
) {
    private val parser = GribParser()
    private val cacheFile = File(context.filesDir, "grib_waves_skagerrak.grib")

    // fetches and parses wave data from local cache or remote if needed
    suspend fun getWaveData(forceRefresh: Boolean = false): Result<List<WaveVector>> =
        withContext(Dispatchers.IO) {
            try {
                // download if cache is missing or expired or forced
                if (forceRefresh || !cacheFile.exists() || isExpired(cacheFile)) {
                    downloadWaves()
                }

                // optionally log available variables in file
                parser.listVariablesInGrib(cacheFile)

                // parse GRIB file to wave vectors
                val waves = parser.parseWaveFile(cacheFile)
                Result.Success(waves)
            } catch (t: Throwable) {
                Result.Error(t)
            }
        }

    // downloads wave GRIB data and saves it to cache
    private suspend fun downloadWaves() {
        val resp = api.getGribFiles(content = "waves", area = "west_norway")
        if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.byteStream().use { inp ->
                cacheFile.outputStream().use { out -> inp.copyTo(out) }
            }
        } else {
            throw IOException("Failed to download waves: HTTP ${resp.code()}")
        }
    }

    // checks whether the cached file is older than 3 hours
    private fun isExpired(file: File): Boolean {
        return System.currentTimeMillis() - file.lastModified() > 3 * 60 * 60 * 1000
    }
}
