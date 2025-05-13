package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.data.remote.datasource.MetAlertsDatasource
import no.uio.ifi.in2000.team46.data.remote.api.MetAlertsRetrofitInstance
import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse

// metalertsrepository fetches weather alert data from met norway using the metalerts api
// wraps the api call in a kotlin result class to handle success and error uniformly

class MetAlertsRepository(
    private val datasource: MetAlertsDatasource = MetAlertsRetrofitInstance.metAlertsApi
) {
    // returns current weather alerts or error wrapped in a Result
    suspend fun getAlerts(): Result<MetAlertsResponse> {
        return try {
            val alerts: MetAlertsResponse = datasource.getAlerts()
            Result.Success(alerts)
        } catch (e: Exception) {
            // optionally log error
            Result.Error(e)
        }
    }
}
