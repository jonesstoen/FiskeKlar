package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.data.remote.datasource.GribDataSource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import no.uio.ifi.in2000.team46.BuildConfig


// gribretrofitinstance provides a retrofit client for fetching grib weather data from met norway
// it sets a required user-agent header and initializes the gribdatasource interface

object GribRetrofitInstance {
    private const val BASE_URL = "https://in2000.api.met.no/"

    // okhttp client with custom user-agent header required by met.no
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val userAgent = "${BuildConfig.MET_USER_AGENT_NAME} (${BuildConfig.MET_USER_AGENT_EMAIL})"
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", userAgent)
                .build()
            chain.proceed(request)
        }
        .build()

    // lazily creates retrofit implementation of gribdatasource
    val GribApi: GribDataSource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GribDataSource::class.java)
    }
}
