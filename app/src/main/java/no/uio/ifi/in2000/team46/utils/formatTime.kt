package no.uio.ifi.in2000.team46.utils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


import java.util.*
import java.util.concurrent.TimeUnit

/*
these helper functions formats the time and calculates remaining time to the event  */

 fun formatTime(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Handle the timezone part manually
    val dateStr = dateString.substring(0, 19)

    val outputFormat = SimpleDateFormat("d. MMM yyyy HH:mm", Locale("no"))
    return try {
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

fun timeUntilStart(startTime: String, endTime: String?): String? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    return try {
        val start = inputFormat.parse(startTime.substring(0, 19)) ?: return null
        val end = endTime?.let { inputFormat.parse(it.substring(0, 19)) }
        val now = Date()

        return when {
            now.before(start) -> {
                val diff = start.time - now.time
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                when {
                    hours > 0 -> "Starter om ${hours}t ${minutes}min"
                    minutes > 0 -> "Starter om ${minutes} min"
                    else -> null
                }
            }
            end != null && now.before(end) -> "Pågår nå"
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}