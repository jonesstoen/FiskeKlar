package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.data.remote.datasource.MetAlertsDatasource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// metalertsretrofitinstance sets up a retrofit client to access met norway's metalerts api
// it includes a user-agent header required by the api and provides the metalertsdatasource interface

object MetAlertsRetrofitInstance {
    private const val BASE_URL = "https://in2000.api.met.no/"

    // http client with required user-agent header
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "IN2000test/1.0 (johastoe@uio.no)")
                .build()
            chain.proceed(request)
        }
        .build()

    // lazily initialized retrofit service for metalerts
    val metAlertsApi: MetAlertsDatasource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MetAlertsDatasource::class.java)
    }
}
