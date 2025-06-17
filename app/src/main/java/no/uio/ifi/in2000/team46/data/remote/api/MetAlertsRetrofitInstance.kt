package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.BuildConfig
import no.uio.ifi.in2000.team46.data.remote.datasource.MetAlertsDatasource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MetAlertsRetrofitInstance sets up a Retrofit client for MET Norway's MetAlerts API,
// including a required User-Agent header defined in local.properties via BuildConfig.

object MetAlertsRetrofitInstance {
    private const val BASE_URL = "https://in2000.api.met.no/"

    // OkHttpClient with dynamic User-Agent header
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val userAgent = "${BuildConfig.MET_USER_AGENT_NAME} (${BuildConfig.MET_USER_AGENT_EMAIL})"
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", userAgent)
                .build()
            chain.proceed(request)
        }
        .build()

    // Lazily initialized Retrofit instance for MetAlerts API
    val metAlertsApi: MetAlertsDatasource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MetAlertsDatasource::class.java)
    }
}
