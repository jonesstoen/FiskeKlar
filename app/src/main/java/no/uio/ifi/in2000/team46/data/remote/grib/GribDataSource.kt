package no.uio.ifi.in2000.team46.data.remote.grib

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GribDataSource {
    @GET("?")
    suspend fun getGribFiles(
        @Query("area") area: String = "oslofjord",
        @Query("content") content: String = "weather"
    ): Response<ResponseBody>
}