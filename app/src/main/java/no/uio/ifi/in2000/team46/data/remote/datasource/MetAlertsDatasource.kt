package no.uio.ifi.in2000.team46.data.remote.datasource

import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse
import retrofit2.http.GET

interface MetAlertsDatasource {
    @GET("weatherapi/metalerts/2.0/current.json")
    suspend fun getAlerts(): MetAlertsResponse
}