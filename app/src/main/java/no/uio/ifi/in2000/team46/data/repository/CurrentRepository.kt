package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import no.uio.ifi.in2000.team46.domain.grib.VectorType
import no.uio.ifi.in2000.team46.data.remote.datasource.GribDataSource
import java.io.File
import java.io.IOException

class CurrentRepository(private val api: GribDataSource,
                        private val context: Context) {
    private val localGribFile = File(context.filesDir, "gribfile_current_oslofjord.grib")
    private val parser = GribParser()

    suspend fun getCurrentData(forceRefresh: Boolean = false): Result<List<CurrentVector>> {
        return withContext(Dispatchers.IO) {
            try {
                // Sjekk cache først
                if (!localGribFile.exists() || isCacheExpired() || forceRefresh) {
                    downloadGribFile(content = "current")
                }
                //list variablene
                parser.listVariablesInGrib(localGribFile)


                val currentVectors = parser.parseVectorFile(
                    localGribFile,
                    "u-component_of_current_depth_below_sea",
                    "v-component_of_current_depth_below_sea",
                    VectorType.CURRENT
                ).filterIsInstance<CurrentVector>()
                Result.Success(currentVectors)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private suspend fun downloadGribFile(content: String): File {
        // Bruk samme logikk som i GribRepository, men content = "current"
        val response = api.getGribFiles(content = "current")
        if (response.isSuccessful && response.body() != null) {
            response.body()!!.byteStream().use { input ->
                localGribFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("CurrentRepository", "Trying to download GRIB file with content=current")
            return localGribFile
        } else {
            throw IOException("Failed to download GRIB file: ${response.code()}")
        }

    }

    /** Sjekker om lokal fil er eldre enn 3 timer */
    private fun isCacheExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - localGribFile.lastModified()
        return ageMs > 3 * 60 * 60 * 1000 // 3 timer
    }
}