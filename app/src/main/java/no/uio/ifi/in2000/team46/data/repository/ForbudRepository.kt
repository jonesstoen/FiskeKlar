package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.data.remote.api.BarentsWatchRetrofitInstance
import no.uio.ifi.in2000.team46.data.remote.api.BarentsWatchForbudService

// Warnings : we decided to not use this code in the app, but it is here for reference
class FishHealthRepository {

    suspend fun fetchFishHealthData(localityNo: Int, year: Int) {
        val authService = BarentsWatchForbudService()
        val token = authService.getAccessToken()

        if (token != null) {
            val response = BarentsWatchRetrofitInstance.fishHealthApi.hentForbudGeoJson(token)
            if (response.isSuccessful) {
                val data = response.body()?.string()
                println("Fish Health Data: $data")
            } else {
                println("Failed to fetch data: ${response.code()}")
            }
        } else {
            println("Failed to get access token")
        }
    }
}
