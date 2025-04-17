package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.data.repository.FishTypeRepository
import java.time.LocalDate
import java.time.LocalTime

class FishingLogViewModel(
    private val fishLogRepo: FishLogRepository,
    private val fishTypeRepo: FishTypeRepository
) : ViewModel() {

    /** Alle loggede fangster som StateFlow med eager start */
    val entries: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllEntries()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Alle pre‑populerte fisketyper som StateFlow */
    val fishTypes: StateFlow<List<FishType>> = fishTypeRepo
        .allTypes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addEntry(
        date: LocalDate,
        time: LocalTime,
        location: String,
        fishType: String,
        weight: Double,
        notes: String,
        imageUri: String?
    ) {
        viewModelScope.launch {
            val entry = FishingLog(
                date = date.toString(),
                time = time.toString(),
                location = location,
                fishType = fishType,
                weight = weight,
                notes = notes,
                imageUri = imageUri
            )
            fishLogRepo.addEntry(entry)
        }
    }

    fun removeEntry(entry: FishingLog) {
        viewModelScope.launch {
            fishLogRepo.removeEntry(entry)
        }
    }

    // Enkel factory for å kunne sende inn to repo-er
    class FishLogViewModelFactory(
        private val fishLogRepo: FishLogRepository,
        private val fishTypeRepo: FishTypeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FishingLogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FishingLogViewModel(fishLogRepo, fishTypeRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}