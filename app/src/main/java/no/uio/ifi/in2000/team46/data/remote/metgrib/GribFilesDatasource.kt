package no.uio.ifi.in2000.team46.data.remote.metgrib

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GribFilesDatasource {
    // Download the current GRIB file for the specified area (default: oslofjord)
    @GET("weatherapi/gribfiles/1.1/current")
    suspend fun getCurrentGribFile(
        @Query("area") area: String = "oslofjord"
    ): Response<ResponseBody>
}