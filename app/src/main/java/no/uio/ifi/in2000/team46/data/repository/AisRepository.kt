package no.uio.ifi.in2000.team46.data.repository

import android.util.Log
import no.uio.ifi.in2000.team46.data.remote.api.BarentsWatchAuthService
import no.uio.ifi.in2000.team46.data.remote.api.BarentsWatchRetrofitInstance
import no.uio.ifi.in2000.team46.domain.ais.AisVesselPosition
import retrofit2.HttpException

// aisrepository handles fetching vessel positions from barentswatch using authentication
// supports optional client-side filtering by bounding box coordinates

class AisRepository {
    private val TAG = "AisRepository"
    private val authService = BarentsWatchAuthService()
    private val api = BarentsWatchRetrofitInstance.aisApi

    // retrieves vessel positions, optionally filters by bounding box if coordinates are provided
    suspend fun getVesselPositions(
        minLon: Double? = null,
        minLat: Double? = null,
        maxLon: Double? = null,
        maxLat: Double? = null
    ): Result<List<AisVesselPosition>> {
        try {
            val token = authService.getAccessToken()
                ?: return Result.Error(Exception("Failed to get access token"))

            // call barentswatch api for latest vessel positions
            val response = api.getLatestPositions(token)

            if (response.isSuccessful) {
                val vessels = response.body() ?: emptyList()

                // optional client-side filtering using provided coordinate bounds
                val filteredVessels = if (minLon != null && minLat != null && maxLon != null && maxLat != null) {
                    vessels.filter { vessel ->
                        val lon = vessel.longitude
                        val lat = vessel.latitude
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
