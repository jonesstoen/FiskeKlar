package no.uio.ifi.in2000.team46.domain.fishlog

import java.time.LocalDate
import java.time.LocalTime

data class FishingData (
    val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val location: String,
    val area: String,
    val notes: String? = null
)