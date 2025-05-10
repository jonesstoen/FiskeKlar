package no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel

import android.util.Log
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

    val entries: StateFlow<List<FishingLog>> = fishLogRepo
        .getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val fishTypes: StateFlow<List<FishType>> = fishTypeRepo
        .allTypes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addEntry(
        date: LocalDate,
        time: LocalTime,
        location: String,
        fishType: String,
        weight: Double,
        notes: String?,
        imageUri: String?,
        latitude: Double,
        longitude: Double,
        count: Int
    ) {
        viewModelScope.launch {
            Log.d("FishingLog", "Attempting to save fishing log entry")
            Log.d("FishingLog", "location=$location, fishType=$fishType, weight=$weight, count=$count")
            val entry = FishingLog(
                date = date.toString(),
                time = time.toString(),
                location = location,
                fishType = fishType,
                weight = weight,
                notes = notes,
                imageUri = imageUri,
                latitude = latitude,
                longitude = longitude,
                count = count
            )
            Log.d("FishingLog", "Created FishingLog entry: $entry")
            fishLogRepo.insert(entry)
            Log.d("FishingLog", "Successfully saved fishing log entry")
        }
    }

    fun removeEntry(entry: FishingLog) {
        viewModelScope.launch {
            fishLogRepo.delete(entry)
        }
    }
    fun addFishType(name: String) {
        viewModelScope.launch {
            fishTypeRepo.insert(FishType(name = name))
        }
    }

    fun deleteAllLogs() {
        viewModelScope.launch {
            fishLogRepo.deleteAllLogs()
        }
    }


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