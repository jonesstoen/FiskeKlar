package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.data.remote.datasource.GeocodingApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// geocodingretrofitinstance sets up a retrofit client for the stadia maps geocoding api
// it includes logging for debugging and provides access to geocoding endpoints

object GeocodingRetrofitInstance {
    private const val BASE_URL = "https://api.stadiamaps.com/"

    // enables body-level logging for all requests and responses
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // okhttp client with logging
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // retrofit instance with gson converter and custom client
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // lazily initialized api interface for geocoding
    val geocodingApi: GeocodingApi by lazy {
        retrofit.create(GeocodingApi::class.java)
    }
}
