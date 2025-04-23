package no.uio.ifi.in2000.team46.data.repository


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.local.parser.GribParser
import no.uio.ifi.in2000.team46.data.remote.grib.GribDataSource
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import java.io.File
import java.io.IOException



class GribRepository(
    private val api: GribDataSource,
    private val context: Context
) {
    private val localGribFile = File(context.filesDir, "gribfile_weather_oslofjord.grib")
    private val parser = GribParser()

    /**
     * Last ned GRIB-fil (hvis nødvendig) og parse til vind-data.
     * @param forceRefresh true = tving ny nedlasting
     */
    suspend fun getWindData(forceRefresh: Boolean = false): Result<List<WindVector>> {
        return withContext(Dispatchers.IO) {
            try {
                // Sjekk cache først
                if (!localGribFile.exists() || isCacheExpired() || forceRefresh) {
                    downloadGribFile()
                }
                //list variablene
                parser.listVariablesInGrib(localGribFile)

                // Parse GRIB-filen til data (vindvektorer)
                val windVectors = parser.parseGribFile(localGribFile)
                Result.Success(windVectors)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /** Last ned GRIB-filen fra API og lagre lokalt */
    private suspend fun downloadGribFile() {
        val response = api.getGribFiles()
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

    /** Sjekker om lokal fil er eldre enn 24 timer */
    private fun isCacheExpired(): Boolean {
        val ageMs = System.currentTimeMillis() - localGribFile.lastModified()
        return ageMs > 24 * 60 * 60 * 1000 // 24 timer
    }
}