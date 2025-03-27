package no.uio.ifi.in2000.team46.data.remote

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.uio.ifi.in2000.team46.data.model.AisVesselPosition

class BarentsWatchApiClient(private val client: HttpClient) {
    // Hent siste posisjon av alle fartøy
    suspend fun getLatestPositions(token: String): Result<List<AisVesselPosition>> {
        return try {
            val response: HttpResponse = client.get("v1/latest/combined") {
                header(HttpHeaders.Authorization, token)
            }

            if (response.status.isSuccess()) {
                val vessels = response.body<List<AisVesselPosition>>()
                Result.success(vessels)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Strøm av alle fartøy
    suspend fun getVesselStream(token: String): Result<List<AisVesselPosition>> {
        return try {
            val response: HttpResponse = client.get("v1/combined") {
                header(HttpHeaders.Authorization, token)
            }

            if (response.status.isSuccess()) {
                val vessels = response.body<List<AisVesselPosition>>()
                Result.success(vessels)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // For filtrert strøm av fartøy
    suspend fun getFilteredVessels(
        token: String,
        filter: VesselFilter,
        modelType: String = "Full",
        modelFormat: String = "Geojson"
    ): Result<List<AisVesselPosition>> {
        return try {
            val response: HttpResponse = client.post("v1/combined") {
                header(HttpHeaders.Authorization, token)
                contentType(ContentType.Application.Json)
                setBody(filter)
                parameter("modelType", modelType)
                parameter("modelFormat", modelFormat)
            }

            if (response.status.isSuccess()) {
                val vessels = response.body<List<AisVesselPosition>>()
                Result.success(vessels)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Alternativ metode som returnerer rå ResponseBody for debugging
    suspend fun getLatestPositionsRaw(token: String): Result<String> {
        return try {
            val response: HttpResponse = client.get("v1/latest/combined") {
                header(HttpHeaders.Authorization, token)
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Result.success(responseBody)
            } else {
                Result.failure(Exception("HTTP Error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Data class for filtrering (uendret)
data class VesselFilter(
    val shipTypes: List<Int>? = null,
    val countryCodes: List<String>? = null,
    val Downsample: Boolean = false
)