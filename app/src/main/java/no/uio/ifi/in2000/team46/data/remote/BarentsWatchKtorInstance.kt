package no.uio.ifi.in2000.team46.data.remote

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import okhttp3.logging.HttpLoggingInterceptor

object BarentsWatchKtorInstance {
    private const val BASE_URL = "https://live.ais.barentswatch.no/"

    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            // Logging Interceptor
            engine {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                addInterceptor(loggingInterceptor)
            }

            // Default Request Plugin for adding headers
            defaultRequest {
                url(BASE_URL)
                headers.append("Content-Type", "application/json")
            }

            // Content Negotiation for JSON
            install(ContentNegotiation) {
                gson()
            }

            // Timeout Configuration
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    // Create the API client using the configured HttpClient
    val aisApiClient by lazy {
        BarentsWatchApiClient(client)
    }
}