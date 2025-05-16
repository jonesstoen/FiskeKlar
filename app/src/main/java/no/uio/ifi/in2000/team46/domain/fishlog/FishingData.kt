package no.uio.ifi.in2000.team46.domain.fishlog

import java.time.LocalDate
import java.time.LocalTime
// fishingdata represents general metadata for a fishing entry
// used for UI display or grouping before combining with detailed catch info

data class FishingData (
    val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val location: String,
    val area: String,
    val notes: String? = null
)