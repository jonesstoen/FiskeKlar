package no.uio.ifi.in2000.team46.data.repository



import no.uio.ifi.in2000.team46.data.remote.datasource.MetAlertsDatasource
import no.uio.ifi.in2000.team46.data.remote.api.MetAlertsRetrofitInstance
import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse


class MetAlertsRepository(
    private val datasource: MetAlertsDatasource = MetAlertsRetrofitInstance.metAlertsApi
) {
    suspend fun getAlerts(): Result<MetAlertsResponse> {
        return try {
            val alerts: MetAlertsResponse = datasource.getAlerts()
            Result.Success(alerts)
        } catch (e: Exception) {
            // Logg feilen om ønskelig
            Result.Error(e)
        }
    }
}