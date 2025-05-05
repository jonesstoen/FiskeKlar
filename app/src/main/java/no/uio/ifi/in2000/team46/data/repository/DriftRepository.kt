package no.uio.ifi.in2000.team46.data.repository

import no.uio.ifi.in2000.team46.domain.grib.DriftVector
import no.uio.ifi.in2000.team46.domain.usecase.drift.calculateDriftVectors

class DriftRepository(
    private val gribRepository: GribRepository,
    private val currentRepository: CurrentRepository
) {
    suspend fun getDriftVectors(): Result<List<DriftVector>> {
        val windResult = gribRepository.getWindData()
        val currentResult = currentRepository.getCurrentData()

        if (windResult is Result.Success && currentResult is Result.Success) {
            val driftVectors = calculateDriftVectors(windResult.data, currentResult.data)
            return Result.Success(driftVectors)
        } else {
            return Result.Error(Exception("Failed to load wind or current data"))
        }
    }
}
