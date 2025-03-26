package no.uio.ifi.in2000.team46.data.remote.metalerts

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface MetAlertsDatasource {
    @GET("weatherapi/metalerts/2.0/current.json")
    suspend fun getAlerts(): Response<ResponseBody>
}