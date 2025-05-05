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
import java.util.Date
import java.util.concurrent.TimeUnit

class BarentsWatchAuthService {
    private val TAG = "BarentsWatchAuth"
    private val mutex = Mutex()

    private var accessToken: String? = null
    private var tokenExpiry: Date? = null

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getAccessToken(): String? = mutex.withLock {
        val now = Date()
        // Bruk cached token hvis det fortsatt er gyldig
        if (accessToken != null && tokenExpiry != null && tokenExpiry!!.time > now.time + 60000) {
            return accessToken
        }


        try {
            val clientSecret = "Gruppe46in2000"  // Din client secret fra skjermbildet

            // Bruk withContext for å kjøre på IO-tråd (ikke hovedtråd)
            return withContext(Dispatchers.IO) {
                val formBody = FormBody.Builder()
                    .add("client_id", "artina@uio.no:Prosjektoppgave")
                    .add("client_secret", clientSecret)
                    .add("scope", "ais")
                    .add("grant_type", "client_credentials")
                    .build()

                val request = Request.Builder()
                    .url("https://id.barentswatch.no/connect/token")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val token = jsonObject.getString("access_token")
                        val expiresIn = jsonObject.getInt("expires_in")

                        accessToken = "Bearer $token"
                        tokenExpiry = Date(now.time + (expiresIn * 1000))

                        return@withContext accessToken
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing token response", e)
                        return@withContext null
                    }
                } else {
                    Log.e(TAG, "Failed to get token: ${response.code} ${response.message}")
                    Log.e(TAG, "Response body: $responseBody")
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting token", e)
            return null
        }
    }
}
