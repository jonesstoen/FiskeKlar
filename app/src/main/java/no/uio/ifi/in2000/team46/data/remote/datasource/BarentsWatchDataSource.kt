package no.uio.ifi.in2000.team46.data.remote.datasource

import no.uio.ifi.in2000.team46.domain.ais.AisVesselPosition
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

// barentswatchdatasource defines retrofit endpoints for accessing vessel tracking data from barentswatch
// it supports latest positions, live stream, filtered queries, and raw data access for debugging

interface BarentsWatchDataSource {

    // retrieves the latest known position for all vessels
    @GET("v1/latest/combined")
    suspend fun getLatestPositions(
        @Header("Authorization") token: String
    ): Response<List<AisVesselPosition>>

    // retrieves a continuous stream of vessel data
    @GET("v1/combined")
    suspend fun getVesselStream(
        @Header("Authorization") token: String
    ): Response<List<AisVesselPosition>>

    // retrieves filtered vessel data based on filter parameters
    @POST("v1/combined")
    suspend fun getFilteredVessels(
        @Header("Authorization") token: String,
        @Body filter: VesselFilter,
        @Query("modelType") modelType: String = "Full",
        @Query("modelFormat") modelFormat: String = "Geojson"
    ): Response<List<AisVesselPosition>>

    // alternative method for retrieving the raw response body (for debugging)
    @GET("v1/latest/combined")
    suspend fun getLatestPositionsRaw(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}

// data class used to filter vessels in post requests
data class VesselFilter(
    val shipTypes: List<Int>? = null, // optional filter by ship type ids
    val countryCodes: List<String>? = null, // optional filter by country codes
    val Downsample: Boolean = false // whether to downsample the dataset
)
