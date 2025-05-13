package no.uio.ifi.in2000.team46.data.remote.datasource

import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse
import retrofit2.http.GET

// metalertsdatasource defines the endpoint for fetching current metalerts from met norway
// returns structured weather alert data in json format

interface MetAlertsDatasource {
    @GET("weatherapi/metalerts/2.0/current.json")
    suspend fun getAlerts(): MetAlertsResponse
}