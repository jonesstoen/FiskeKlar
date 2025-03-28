package no.uio.ifi.in2000.team46.data.repository


import android.util.Log
import no.uio.ifi.in2000.team46.data.remote.ais.BarentsWatchAuthService
import no.uio.ifi.in2000.team46.data.remote.ais.BarentsWatchRetrofitInstance
import no.uio.ifi.in2000.team46.domain.model.ais.AisVesselPosition
import retrofit2.HttpException

class AisRepository {
    private val TAG = "AisRepository"
    private val authService = BarentsWatchAuthService()
    private val api = BarentsWatchRetrofitInstance.aisApi

    suspend fun getVesselPositions(
        minLon: Double? = null,
        minLat: Double? = null,
        maxLon: Double? = null,
        maxLat: Double? = null
    ): Result<List<AisVesselPosition>> {
        try {
            val token = authService.getAccessToken()
            if (token == null) {
                return Result.Error(Exception("Failed to get access token"))
            }

            // Hent siste fartøysposisjoner
            val response = api.getLatestPositions(token)

            if (response.isSuccessful) {
                val vessels = response.body() ?: emptyList()

                // Filtrer basert på koordinater hvis de er gitt
                // Her filtrerer vi på klientsiden siden API-et kanskje ikke støtter dette direkte
                val filteredVessels = if (minLon != null && minLat != null && maxLon != null && maxLat != null) {
                    vessels.filter { vessel ->
                        // Tilpass feltnavnene til din AisVesselPosition-modell
                        val lon = vessel.longitude // eller hvilket felt som har longitude
                        val lat = vessel.latitude  // eller hvilket felt som har latitude

                        lon in minLon..maxLon && lat in minLat..maxLat
                    }
                } else {
                    vessels
                }

                return Result.Success(filteredVessels)
            } else {
                Log.e(TAG, "API request failed: ${response.code()} ")
                return Result.Error(HttpException(response))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting vessel positions", e)
            return Result.Error(e)
        }
    }
}