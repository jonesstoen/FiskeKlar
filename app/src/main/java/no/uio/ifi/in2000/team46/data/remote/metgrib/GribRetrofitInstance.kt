package no.uio.ifi.in2000.team46.data.remote.metgrib

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

import java.util.concurrent.TimeUnit

object GribRetrofitInstance {
    private const val BASE_URL = "https://api.met.no/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "IN2000test/1.0 (johastoe@uio.no)")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val gribApi: GribFilesDatasource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(GribFilesDatasource::class.java)
    }
}