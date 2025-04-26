package no.uio.ifi.in2000.team46.data.local.parser


fun main() {
    val windVectors = listOf(
        WindVector(lon = 10.0, lat = 60.0, speed = 5.0, direction = 90.0), // østlig vind
        WindVector(lon = 11.0, lat = 61.0, speed = 3.0, direction = 180.0) // sørlig vind
    )

    val currentVectors = listOf(
        CurrentVector(lon = 10.0, lat = 60.0, speed = 2.0, direction = 0.0),   // nordlig strøm
        CurrentVector(lon = 11.0, lat = 61.0, speed = 1.0, direction = 270.0)  // vestlig strøm
    )

    val driftVectors = calculateDriftVectors(windVectors, currentVectors)

    println("Drift vectors:")
    driftVectors.forEach { drift ->
        println("Lon: ${drift.lon}, Lat: ${drift.lat}, Speed: ${"%.2f".format(drift.speed)}, Direction: ${"%.2f".format(drift.direction)}")
    }
}
