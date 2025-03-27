package no.uio.ifi.in2000.team46.data.repository

import android.util.Log
import no.uio.ifi.in2000.team46.data.remote.BarentsWatchAuthService
import no.uio.ifi.in2000.team46.data.remote.BarentsWatchKtorInstance
import no.uio.ifi.in2000.team46.data.model.AisVesselPosition

class AisRepository {
    private val TAG = "AisRepository"
    private val authService = BarentsWatchAuthService()
    private val apiClient = BarentsWatchKtorInstance.aisApiClient

    suspend fun getVesselPositions(
        minLon: Double? = null,
        minLat: Double? = null,
        maxLon: Double? = null,
        maxLat: Double? = null
    ): Result<List<AisVesselPosition>> {
        return try {
            val token = authService.getAccessToken()
                ?: return Result.failure(Exception("Failed to get access token"))

            // Hent siste fartøysposisjoner
            val vesselResult = apiClient.getLatestPositions(token)

            vesselResult.map { vessels ->
                // Filtrer basert på koordinater hvis de er gitt
                // Her filtrerer vi på klientsiden siden API-et kanskje ikke støtter dette direkte
                if (minLon != null && minLat != null && maxLon != null && maxLat != null) {
                    vessels.filter { vessel ->
                        // Tilpass feltnavnene til din AisVesselPosition-modell
                        val lon = vessel.longitude // eller hvilket felt som har longitude
                        val lat = vessel.latitude  // eller hvilket felt som har latitude

                        lon in minLon..maxLon && lat in minLat..maxLat
                    }
                } else {
                    vessels
                }
            }.onFailure { e ->
                Log.e(TAG, "Exception getting vessel positions", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting vessel positions", e)
            Result.failure(e)
        }
    }
}