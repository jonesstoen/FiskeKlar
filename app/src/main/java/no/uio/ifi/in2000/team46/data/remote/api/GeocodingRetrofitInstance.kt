package no.uio.ifi.in2000.team46.data.remote.api

import no.uio.ifi.in2000.team46.data.remote.datasource.GeocodingApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeocodingRetrofitInstance {
    private const val BASE_URL = "https://api.stadiamaps.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geocodingApi: GeocodingApi by lazy {
        retrofit.create(GeocodingApi::class.java)
    }
}