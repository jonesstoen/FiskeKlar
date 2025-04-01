package no.uio.ifi.in2000.team46.data.remote.grib

import no.uio.ifi.in2000.team46.data.remote.metalerts.MetAlertsDatasource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object GribRetrofitInstance {
    private const val BASE_URL = "https://in2000.api.met.no/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "IN2000test/1.0 (johastoe@uio.no)")
                .build()
            chain.proceed(request)
        }

        .build()

    val GribApi: GribDataSource by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GribDataSource::class.java)
    }
}