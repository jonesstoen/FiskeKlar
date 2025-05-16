package no.uio.ifi.in2000.team46.data.remote.datasource

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ForbudDataSource {

    //fetches GeoJSON for areas with fishing bans (e.g. cod ban)
    @GET("v1/geodata/forbiddenfishingzone")
    suspend fun hentForbudGeoJson(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}
