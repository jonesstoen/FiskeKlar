package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.data.remote.datasource.BarentsWatchDataSource
import no.uio.ifi.in2000.team46.data.remote.datasource.ForbudDataSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// barentswatchretrofitinstance provides singleton retrofit clients for ais and fish health apis
// it configures headers, logging, and timeouts for consistent network calls

object BarentsWatchRetrofitInstance {
    private const val BASE_URL = "https://live.ais.barentswatch.no/"
    private const val fiks_url = "https://www.barentswatch.no/bwapi/"

    // shared http client with logging and default headers
    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // logging output can be handled here if needed
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") // accept json responses
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // retrofit instance for vessel data (ais)
    val aisApi: BarentsWatchDataSource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BarentsWatchDataSource::class.java)
    }

    // retrofit instance for fish health / forbud (fiks) api
    val fishHealthApi: ForbudDataSource by lazy {
        Retrofit.Builder()
            .baseUrl(fiks_url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForbudDataSource::class.java)
    }
}
