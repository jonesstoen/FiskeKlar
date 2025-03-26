package no.uio.ifi.in2000.team46.data.repository


import no.uio.ifi.in2000.team46.data.remote.MetAlertsDatasource
import retrofit2.HttpException

class MetAlertsRepository(private val api: MetAlertsDatasource) {
    suspend fun fetchMetAlertsJson(): String? {
        return try {
            val response = api.getAlerts()
            if (response.isSuccessful) {
                response.body()?.string()
            } else {
                // Log the error response
                null
            }
        } catch (e: HttpException) {
            // Log the HTTP exception
            null
        } catch (e: Exception) {
            // Log the general exception
            null
        }
    }
}