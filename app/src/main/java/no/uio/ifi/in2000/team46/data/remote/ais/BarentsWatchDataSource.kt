package no.uio.ifi.in2000.team46.data.remote.ais

import no.uio.ifi.in2000.team46.domain.model.ais.AisVesselPosition
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

interface BarentsWatchDataSource {
    // Hent siste posisjon av alle fartøy
    @GET("v1/latest/combined")
    suspend fun getLatestPositions(
        @Header("Authorization") token: String
    ): Response<List<AisVesselPosition>>

    // Strøm av alle fartøy
    @GET("v1/combined")
    suspend fun getVesselStream(
        @Header("Authorization") token: String
    ): Response<List<AisVesselPosition>>

    // For filtrert strøm av fartøy
    @POST("v1/combined")
    suspend fun getFilteredVessels(
        @Header("Authorization") token: String,
        @Body filter: VesselFilter,
        @Query("modelType") modelType: String = "Full",
        @Query("modelFormat") modelFormat: String = "Geojson"
    ): Response<List<AisVesselPosition>>

    // Alternativ metode som returnerer rå ResponseBody for debugging
    @GET("v1/latest/combined")
    suspend fun getLatestPositionsRaw(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}

// Data class for filtrering
data class VesselFilter(
    val shipTypes: List<Int>? = null,
    val countryCodes: List<String>? = null,
    val Downsample: Boolean = false
)