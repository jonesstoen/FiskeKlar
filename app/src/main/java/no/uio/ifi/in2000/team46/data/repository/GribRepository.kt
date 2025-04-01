package no.uio.ifi.in2000.team46.data.repository

import android.content.Context
import no.uio.ifi.in2000.team46.data.remote.metgrib.GribRetrofitInstance

import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GribRepository(private val context: Context) {
    suspend fun downloadGribFile(area: String = "oslofjord"): File? {
        val response = GribRetrofitInstance.gribApi.getCurrentGribFile(area = area)

        if (response.isSuccessful) {
            response.body()?.let { body ->
                return saveFile(body, "wind_data.grib")
            }
        }
        return null
    }

    private fun saveFile(body: ResponseBody, filename: String): File {
        val file = File(context.filesDir, filename)
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return file
    }
}
