package no.uio.ifi.in2000.team46.domain.grib

// this file defines a sealed interface used for representing different types of vectors (e.g. wind, current)
// the interface ensures all implementations share geographic coordinates and motion properties

sealed interface Vector {
    val lon: Double
    val lat: Double
    val speed: Double
    val direction: Double
}