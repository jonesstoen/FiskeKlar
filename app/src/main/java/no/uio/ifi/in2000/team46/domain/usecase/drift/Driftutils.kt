package no.uio.ifi.in2000.team46.domain.usecase.drift

import no.uio.ifi.in2000.team46.domain.grib.DriftVector


fun calculateDriftImpactForVector(
    driftVector: DriftVector,
    boatLength: Double = 7.5,
    boatBeam: Double = 2.5,
    draft: Double = 1.0,
    freeboardHeight: Double = 0.8,
    boatMass: Double = 2.5,
    waveHeight: Double = 1.0,
    wavePeriod: Double = 6.0
): Double {
    return calculateDriftImpact(
        windSpeed        = driftVector.windSpeed,
        windDirection    = driftVector.windDirection,
        currentSpeed     = driftVector.currentSpeed,
        boatLength       = boatLength,
        boatBeam         = boatBeam,
        draft            = draft,
        freeboardHeight  = freeboardHeight,
        boatMass         = boatMass,
        waveHeight       = waveHeight,
        wavePeriod       = wavePeriod
    )
}
