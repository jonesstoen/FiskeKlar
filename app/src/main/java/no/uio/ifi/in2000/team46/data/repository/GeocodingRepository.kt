package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.data.remote.datasource.GeocodingApi
import no.uio.ifi.in2000.team46.data.remote.api.GeocodingResponse
import no.uio.ifi.in2000.team46.data.remote.api.GeocodingRetrofitInstance

// geocodingrepository provides access to location search via the stadia maps geocoding api
// wraps the api call with default parameters and optional focus location

class GeocodingRepository {
    private val api: GeocodingApi = GeocodingRetrofitInstance.geocodingApi

    // performs a geocoding search using optional focus coordinates
    suspend fun search(
        query: String,
        apiKey: String,
        focusLat: Double? = null,
        focusLon: Double? = null
    ): GeocodingResponse {
        return api.search(
            query = query,
            apiKey = apiKey,
            focusLat = focusLat,
            focusLon = focusLon,
            size = 25, // max results
            layers = "address,street,venue,neighbourhood,locality,county,macroregion", // search layers
            focusPointWeight = 3,
            autocomplete = true
            // countryCode = "NO" // can be enabled if needed
        )
    }
}
