package no.uio.ifi.in2000.team46.domain.usecase.drift



fun calculateDriftImpact(
    windSpeed: Double,                   // m/s
    currentSpeed: Double = 0.0,          // m/s
    windDirection: Double = 0.0,         // degrees, for vector addition
    boatLength: Double,                  // m
    boatBeam: Double,                    // m
    draft: Double,                       // m
    freeboardHeight: Double,             // m
    boatMass: Double,                    // tonnes
    C_d_air: Double = 1.2,               // air drag coefficient
    C_d_water: Double = 1.0,             // water drag coefficient
    waterDensity: Double = 1025.0,       // kg/m³
    airDensity: Double = 1.225,          // kg/m³
    kinematicViscosity: Double = 1e-6,   // m²/s
    waveHeight: Double = 0.0,            // m
    wavePeriod: Double = 0.0             // s
): Double {
    // 1) compute projected areas
    val A_wind = boatLength * freeboardHeight
    val A_sub  = boatLength * draft

    // 2) wind force
    val F_wind = 0.5 * airDensity * C_d_air * A_wind * windSpeed * windSpeed

    // 3) water‐resistive force at drift speed Vd (unknown);
    //    at equilibrium F_wind ≈ F_water
    //    ⇒ V_d = sqrt( F_wind / (0.5 * ρw * Cdw * A_sub) )
    val F_coef = 0.5 * waterDensity * C_d_water * A_sub
    val driftSpeed = kotlin.math.sqrt(F_wind / F_coef)

    // 4) add currents & wave drift
    val stokesDrift = if (waveHeight > 0 && wavePeriod > 0) {
        // approximate: λ ≈ gT²/(2π)
        val lambda = 9.80665 * wavePeriod * wavePeriod / (2 * Math.PI)
        Math.PI * waveHeight*waveHeight / (2 * wavePeriod * lambda)
    } else 0.0

    val totalDriftSpeed = driftSpeed + currentSpeed + stokesDrift

    // 5) return meters per hour
    return totalDriftSpeed * 3600.0
}
