package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.FishLogRepository
import no.uio.ifi.in2000.team46.domain.model.fishlog.FishingData
import java.time.LocalDate
import java.time.LocalTime

class FishingLogViewModel (private val repository: FishLogRepository) : ViewModel() {
    private val _entries = MutableStateFlow<List<FishingData>>(emptyList())
    val entries: StateFlow<List<FishingData>> = _entries.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _entries.value = entries
            }
        }
    }

    fun addEntry(date: LocalDate, time: LocalTime, location: String, area: String, notes: String?) {
        viewModelScope.launch {
            val entry = FishingData(
                id = System.currentTimeMillis(),
                date = date,
                time = time,
                location = location,
                area = area,
                notes = notes
            )

            repository.addEntry(entry)
        }
    }

    fun removeEntry(id : Long) {
        viewModelScope.launch {
            repository.removeEntry(id)
        }
    }
}