package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import java.time.LocalDate
import java.time.LocalTime

class FishingLogViewModel(private val repository: FishLogRepository) : ViewModel() {

    // Eksempel på at vi henter data fra repository (Flow konvertert til StateFlow)
    val entries = repository.getAllEntries() // Forutsetter at du har en funksjon getAllEntries() i repositoryen

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
            repository.addEntry(entry)
        }
    }

    fun removeEntry(entry: FishingLog) {
        viewModelScope.launch {
            repository.removeEntry(entry)
        }
    }
}
