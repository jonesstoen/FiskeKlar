package no.uio.ifi.in2000.team46.data.remote.datasource

import no.uio.ifi.in2000.team46.domain.ais.AisVesselPosition
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

// barentswatchdatasource defines retrofit endpoints for accessing vessel tracking data from barentswatch
// it supports latest positions, live stream, filtered queries, and raw data access for debugging

interface BarentsWatchDataSource {

    // retrieves the latest known position for all vessels
    @GET("v1/latest/combined")
    suspend fun getLatestPositions(
        @Header("Authorization") token: String
    ): Response<List<AisVesselPosition>>

}

