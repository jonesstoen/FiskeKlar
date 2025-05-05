package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import no.uio.ifi.in2000.team46.data.local.parser.WaveVector
import no.uio.ifi.in2000.team46.data.remote.grib.GribDataSource
import java.io.File
import java.io.IOException

class WaveRepository(
    private val api: GribDataSource,
    private val context: Context
) {
    private val parser   = GribParser()
    private val cacheFile = File(context.filesDir, "grib_waves_skagerrak.grib")

    suspend fun getWaveData(forceRefresh: Boolean = false): Result<List<WaveVector>> =
        withContext(Dispatchers.IO) {
            try {
                if (forceRefresh || !cacheFile.exists() || isExpired(cacheFile)) {
                    downloadWaves()

                }
                parser.listVariablesInGrib(cacheFile)
                val waves = parser.parseWaveFile(cacheFile)
                Result.Success(waves)
            } catch (t: Throwable) {
                Result.Error(t)
            }
        }

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

    private fun isExpired(file: File): Boolean {
        return true//System.currentTimeMillis() - file.lastModified() > 3 * 60 * 60 * 1000
    }
}
