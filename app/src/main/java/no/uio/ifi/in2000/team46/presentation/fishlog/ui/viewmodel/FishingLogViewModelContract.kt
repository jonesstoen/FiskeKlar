package no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel



import kotlinx.coroutines.flow.StateFlow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType
import java.time.LocalDate
import java.time.LocalTime

interface FishingLogUiContract {

    val entries: StateFlow<List<FishingLog>>
    val fishTypes: StateFlow<List<FishType>>
    fun addEntry(
        date: LocalDate,
        time: LocalTime,
        location: String,
        fishType: String,
        weight: Double,
        notes: String,
        imageUri: String?)


    fun removeEntry(entry: FishingLog)
}