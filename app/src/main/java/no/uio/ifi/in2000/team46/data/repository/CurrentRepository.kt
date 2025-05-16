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

// currentrepository handles downloading, caching, and parsing ocean current data from grib files
// uses a local file cache and the gribparser to extract vector data of type currentvector

// WARNNINGS: the wearings in this file are related to  setup for fucntionality that can be added in the future
// for example filtering area of interest, or downloading grib files for other content types
class CurrentRepository(
    private val api: GribDataSource,
    private val context: Context
) {
    private val localGribFile = File(context.filesDir, "gribfile_current_oslofjord.grib")
    private val parser = GribParser()

    // returns parsed current vectors from local or freshly downloaded grib file
    suspend fun getCurrentData(forceRefresh: Boolean = false): Result<List<CurrentVector>> {
        return withContext(Dispatchers.IO) {
            try {
                // check if file needs refresh
                if (!localGribFile.exists() || isCacheExpired() || forceRefresh) {
                    downloadGribFile(content = "current")
                }

                // optional debug: print available GRIB variables
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

    // downloads the grib file for currents and writes it to local cache
    private suspend fun downloadGribFile(content: String): File {
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

    // checks if the cached file is older than 3 hours
    private fun isCacheExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - localGribFile.lastModified()
        return ageMs > 3 * 60 * 60 * 1000 // 3 hours
    }
}
