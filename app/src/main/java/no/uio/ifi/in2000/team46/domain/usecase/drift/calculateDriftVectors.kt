package no.uio.ifi.in2000.team46.domain.usecase.drift

import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.domain.grib.DriftVector
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import kotlin.math.atan2
import kotlin.math.sqrt

fun calculateDriftVectors(
    windVectors: List<WindVector>,
    currentVectors: List<CurrentVector>
): List<DriftVector> {
    return windVectors.mapNotNull { wind ->
        val matchingCurrent = currentVectors.find {
            it.lat == wind.lat && it.lon == wind.lon
        }
        matchingCurrent?.let { current ->
            val windU = wind.speed * kotlin.math.sin(Math.toRadians(wind.direction))
            val windV = wind.speed * kotlin.math.cos(Math.toRadians(wind.direction))
            val currentU = current.speed * kotlin.math.sin(Math.toRadians(current.direction))
            val currentV = current.speed * kotlin.math.cos(Math.toRadians(current.direction))

            val totalU = windU + currentU
            val totalV = windV + currentV

            val totalSpeed = sqrt(totalU * totalU + totalV * totalV)
            val totalDirection = (Math.toDegrees(atan2(totalU, totalV)) + 360) % 360

            DriftVector(
                lon = wind.lon,
                lat = wind.lat,
                speed = totalSpeed,
                direction = totalDirection,
                windSpeed = wind.speed,
                windDirection = wind.direction,
                currentSpeed = current.speed,
                currentDirection = current.direction
            )
        }
    }
}

