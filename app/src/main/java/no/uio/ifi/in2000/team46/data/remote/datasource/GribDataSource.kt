package no.uio.ifi.in2000.team46.data.remote.datasource

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// gribdatasource defines the endpoint for downloading grib weather files from met norway
// supports specifying content type and area for the requested grib data

interface GribDataSource {

    // fetches raw grib file response based on content type and area
    @GET("weatherapi/gribfiles/1.1/")
    suspend fun getGribFiles(
        @Query("content") content: String, // type of grib content (e.g. wind, current, wave)
        @Query("area") area: String = "west_norway" // optional area (default is west norway)
    ): Response<ResponseBody>
}
