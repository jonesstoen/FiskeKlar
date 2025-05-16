package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.domain.grib.VectorType
import no.uio.ifi.in2000.team46.data.remote.datasource.GribDataSource
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import java.io.File
import java.io.IOException

// gribrepository handles downloading, caching, and parsing grib weather files (wind and precipitation)
// it uses a local file to reduce repeated downloads and parses data using the gribparser

//WARNINGS: the warning in this file are related to  setup for fucntionality that can be added in the future

class GribRepository(
    private val api: GribDataSource,
    private val context: Context
) {
    private val localGribFile = File(context.filesDir, "gribfile_weather_west_norway.grib")
    private val parser = GribParser()

    /**
     * downloads and parses wind data from grib file
     * uses cached file unless expired or forceRefresh is true
     */
    suspend fun getWindData(forceRefresh: Boolean = false): Result<List<WindVector>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!localGribFile.exists() || isCacheExpired() || forceRefresh) {
                    downloadGribFile()
                }

                parser.listVariablesInGrib(localGribFile)

                val windVectors = parser.parseVectorFile(
                    localGribFile,
                    "u-component_of_wind_height_above_ground",
                    "v-component_of_wind_height_above_ground",
                    VectorType.WIND
                ).filterIsInstance<WindVector>()

                Result.Success(windVectors)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /** downloads the GRIB file from the API and saves it locally */
    private suspend fun downloadGribFile() {
        val response = api.getGribFiles(content = "weather")
        if (response.isSuccessful && response.body() != null) {
            response.body()!!.byteStream().use { input ->
                localGribFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            throw IOException("Failed to download GRIB file: ${response.code()}")
        }
    }

    /** checks whether the local GRIB file is older than 3 hours */
    private fun isCacheExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - localGribFile.lastModified()
        return ageMs > 3 * 60 * 60 * 1000
    }

    /**
     * downloads and parses precipitation data from GRIB file
     * uses same file as wind data and same caching logic
     */
    suspend fun getPrecipitationData(forceRefresh: Boolean = false): Result<List<PrecipitationPoint>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!localGribFile.exists() || isCacheExpired() || forceRefresh) {
                    downloadGribFile()
                }

                parser.listVariablesInGrib(localGribFile)

                val list = parser.parsePrecipitationFile(localGribFile)

                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
