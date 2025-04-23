package no.uio.ifi.in2000.team46.data.remote.metalerts

import retrofit2.http.GET
import retrofit2.http.Query

interface SunInfoDataSource {
    @GET("weatherapi/sunrise/3.0/sun")
    suspend fun getSunInfo(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String,
        @Query("offset") offset: String
    ): SunInfo
}