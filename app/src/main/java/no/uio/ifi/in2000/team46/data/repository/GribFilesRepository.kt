package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.data.remote.metgrib.GribRetrofitInstance

class GribFilesRepository {
    suspend fun getCurrentGribFile(): ByteArray? {
        return try {
            val response = GribRetrofitInstance.gribApi.getCurrentGribFile()
            if (response.isSuccessful) {
                // Download the file as bytes for further processing
                response.body()?.bytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}