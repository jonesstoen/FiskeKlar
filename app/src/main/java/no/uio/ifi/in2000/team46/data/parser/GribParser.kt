package no.uio.ifi.in2000.team46.data.parser

import jgrib.GribFile
import jgrib.GribRecord

import java.io.File
import kotlin.math.*

class GribParser {
    fun parseWindData(gribFile: File): List<WindData> {
        val windDataList = mutableListOf<WindData>()
        val grib = GribFile(gribFile)

        val uWindRecords = mutableMapOf<Pair<Double, Double>, Double>()
        val vWindRecords = mutableMapOf<Pair<Double, Double>, Double>()

        for (record in grib.records) {
            val lat = record.latitude
            val lon = record.longitude
            val key = Pair(lat, lon)

            when (record.parameter) {
                "10 metre U wind component" -> uWindRecords[key] = record.value
                "10 metre V wind component" -> vWindRecords[key] = record.value
            }
        }

        // Combine U/V components and calculate wind direction + speed
        for ((key, u) in uWindRecords) {
            val v = vWindRecords[key] ?: continue

            val windSpeed = sqrt(u * u + v * v)  // Wind speed (m/s)
            val windDirection = atan2(-u, -v) * (180 / Math.PI) // Wind direction in degrees
            if (windDirection < 0) windDirection += 360 // Normalize to 0-360°

            windDataList.add(WindData(key.first, key.second, windSpeed, windDirection))
        }

        return windDataList
    }
}

data class WindData(val latitude: Double, val longitude: Double, val speed: Double, val direction: Double)
