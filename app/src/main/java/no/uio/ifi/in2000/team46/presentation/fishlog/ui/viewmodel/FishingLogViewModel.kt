package no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel

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
) : ViewModel(), FishingLogUiContract {


    //all fishing logs prepopulated in the database
    override val entries: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllEntries()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    //all fish types prepopulated in the database
    override val fishTypes: StateFlow<List<FishType>> = fishTypeRepo
        .allTypes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    override fun addEntry(
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

    override fun removeEntry(entry: FishingLog) {
        viewModelScope.launch {
            fishLogRepo.removeEntry(entry)
        }
    }

    // factory for creating the ViewModel
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