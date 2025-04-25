package no.uio.ifi.in2000.team46.data.remote.grib

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GribDataSource {
    @GET("weatherapi/gribfiles/1.1/")
    suspend fun getGribFiles(
        @Query("content") content: String ,
        @Query("area") area: String = "skagerrak",

    ): Response<ResponseBody>
}