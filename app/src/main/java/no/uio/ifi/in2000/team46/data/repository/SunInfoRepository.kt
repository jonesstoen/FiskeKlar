package no.uio.ifi.in2000.team46.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.team46.data.remote.metalerts.RetrofitInstance
import no.uio.ifi.in2000.team46.domain.model.metalerts.SunInfo
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

sealed class SunInfoResult<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
class SunInfoRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSunInfo(lat: Double, lon: Double, time: String): Result<SunInfo> {
        return try {
            // Parse the time string to extract the offset
            val offset = OffsetDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME).offset.toString()

            // Use the extracted offset in the API call
            val response = RetrofitInstance.sunInfoApi.getSunInfo(lat, lon, time, offset)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}