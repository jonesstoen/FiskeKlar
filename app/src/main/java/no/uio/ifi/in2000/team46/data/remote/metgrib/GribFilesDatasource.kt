package no.uio.ifi.in2000.team46.data.remote.metgrib

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


//Parameters
//area
//Valid areas are: oslofjord, skagerrak, sorlandet, west_norway, n-northsea, troms-finnmark, nordland
//
//content
//Valid datatypes are: weather, current and waves


interface GribFilesDatasource {
    // Download the current GRIB file for the specified area (default: oslofjord)
    @GET("weatherapi/gribfiles/1.1/")
    suspend fun getCurrentGribFile(
        @Query("area") area: String = "oslofjord",
        @Query("content") content: String = "weather"
    ): Response<ResponseBody>
}