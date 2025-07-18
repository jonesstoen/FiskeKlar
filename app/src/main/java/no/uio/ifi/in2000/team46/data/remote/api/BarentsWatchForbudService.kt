package no.uio.ifi.in2000.team46.data.remote.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

// barentswatchforbudservice fetches and caches an access token for the forbud (fish health) api
// it ensures thread-safe access using a mutex and refreshes the token when expired

class BarentsWatchForbudService {
    private val tag = "ForbudAuthService"
    private val mutex = Mutex()

    private var accessToken: String? = null
    private var tokenExpiry: Date? = null

    // http client with connection and read timeouts
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // retrieves a valid access token, using cache if available and not close to expiry
    suspend fun getAccessToken(): String? = mutex.withLock {
        val now = Date()

        // return cached token if still valid for at least 1 minute
        if (accessToken != null && (tokenExpiry?.time ?: 0) > now.time + 60000) {
            return accessToken
        }

        return withContext(Dispatchers.IO) {
            try {
                val clientSecret = "Gruppe46in2000"
                val formBody = FormBody.Builder()
                    .add("client_id", "marihrog@uio.no:in2000gruppe")
                    .add("client_secret", clientSecret)
                    .add("scope", "api")
                    .add("grant_type", "client_credentials")
                    .build()

                val request = Request.Builder()
                    .url("https://id.barentswatch.no/connect/token")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d(tag, "Response: $responseBody")

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val json = JSONObject(responseBody)
                    val token = json.getString("access_token")
                    val expiresIn = json.getInt("expires_in")

                    accessToken = "Bearer $token"
                    tokenExpiry = Date(now.time + expiresIn * 1000)
                    return@withContext accessToken
                } else {
                    Log.e(tag, "Token-feil: ${response.code} ${response.message}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(tag, "Token exception", e)
                return@withContext null
            }
        }
    }
}
