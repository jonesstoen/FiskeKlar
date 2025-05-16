package no.uio.ifi.in2000.team46.data.remote.datasource

import no.uio.ifi.in2000.team46.data.remote.api.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

// geocodingapi defines the stadia maps geocoding endpoint for location search
// supports optional filters for focus, size, country, and more

interface GeocodingApi {
    @GET("geocoding/v1/search")
    suspend fun search(
        @Query("text") query: String,
        @Query("api_key") apiKey: String,
        @Query("focus.point.lat") focusLat: Double? = null,
        @Query("focus.point.lon") focusLon: Double? = null,
        @Query("size") size: Int = 5,
        @Query("layers") layers: String? = null,
        @Query("boundary.country") countryCode: String? = null,
        @Query("focus.point.weight") focusPointWeight: Int? = null,
        @Query("autocomplete") autocomplete: Boolean = false,
        @Query("sources") sources: String? = null
    ): GeocodingResponse
}