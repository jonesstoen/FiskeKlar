package no.uio.ifi.in2000.team46.data.remote.forbud

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface ForbudDataSource {

    // Henter GeoJSON for områder med fiskeforbud (f.eks. torskeforbud)
    @GET("/v1/geodata/forbiddenfishingzone")
    suspend fun hentForbudGeoJson(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}
