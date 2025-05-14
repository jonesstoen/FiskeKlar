package no.uio.ifi.in2000.team46.presentation.weather.utils

import kotlinx.datetime.*

object DateTimeFormatter {
    fun formatDate(dateString: String): String {
        try {
            val instant = Instant.parse(dateString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val dayOfWeek = when (localDateTime.dayOfWeek) {
                DayOfWeek.MONDAY -> "Mandag"
                DayOfWeek.TUESDAY -> "Tirsdag"
                DayOfWeek.WEDNESDAY -> "Onsdag"
                DayOfWeek.THURSDAY -> "Torsdag"
                DayOfWeek.FRIDAY -> "Fredag"
                DayOfWeek.SATURDAY -> "Lørdag"
                DayOfWeek.SUNDAY -> "Søndag"
            }
            
            val month = formatMonth(localDateTime.monthNumber)
            return "$dayOfWeek ${localDateTime.dayOfMonth}. $month"
        } catch (e: Exception) {
            // if date if in format "YYYY-MM-DD"
            try {
                val date = LocalDate.parse(dateString)
                val dayOfWeek = when (date.dayOfWeek) {
                    DayOfWeek.MONDAY -> "Mandag"
                    DayOfWeek.TUESDAY -> "Tirsdag"
                    DayOfWeek.WEDNESDAY -> "Onsdag"
                    DayOfWeek.THURSDAY -> "Torsdag"
                    DayOfWeek.FRIDAY -> "Fredag"
                    DayOfWeek.SATURDAY -> "Lørdag"
                    DayOfWeek.SUNDAY -> "Søndag"
                }
                
                val month = formatMonth(date.monthNumber)
                return "$dayOfWeek ${date.dayOfMonth}. $month"
            } catch (e: Exception) {
                return dateString
            }
        }
    }
    
    fun formatTime(timeString: String): String {
        return try {
            val instant = Instant.parse(timeString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val hour = localDateTime.hour
            
            String.format("%02d:%02d", hour, localDateTime.minute)
        } catch (e: Exception) {
            timeString
        }
    }
    
    private fun formatMonth(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "januar"
            2 -> "februar"
            3 -> "mars"
            4 -> "april"
            5 -> "mai"
            6 -> "juni"
            7 -> "juli"
            8 -> "august"
            9 -> "september"
            10 -> "oktober"
            11 -> "november"
            12 -> "desember"
            else -> ""
        }
    }
} 