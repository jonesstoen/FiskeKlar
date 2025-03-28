package no.uio.ifi.in2000.team46.data.repository



import no.uio.ifi.in2000.team46.data.remote.metalerts.MetAlertsDatasource
import no.uio.ifi.in2000.team46.data.remote.metalerts.RetrofitInstance
import no.uio.ifi.in2000.team46.domain.model.metalerts.MetAlertsResponse
import retrofit2.HttpException


class MetAlertsRepository(
    private val datasource: MetAlertsDatasource = RetrofitInstance.metAlertsApi
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