package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.data.remote.datasource.GeocodingApi
import no.uio.ifi.in2000.team46.data.remote.api.GeocodingResponse
import no.uio.ifi.in2000.team46.data.remote.api.GeocodingRetrofitInstance

class GeocodingRepository {
    private val api: GeocodingApi = GeocodingRetrofitInstance.geocodingApi

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
            size = 25,
            layers = "address,street,venue,neighbourhood,locality,county,macroregion",
            focusPointWeight = 3,
            autocomplete = true,
            //countryCode = "NO"
        )
    }
}