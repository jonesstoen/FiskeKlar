package no.uio.ifi.in2000.team46.data.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team46.data.remote.grib.GribRetrofitInstance
import okhttp3.ResponseBody

class GribRepository {
    private val api = GribRetrofitInstance.GribApi

    suspend fun getGribFiles(area: String, content: String): ResponseBody? {
        return withContext(Dispatchers.IO) {
            val response = api.getGribFiles(area, content)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}