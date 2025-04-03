package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.grib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.model.grib.WindData

class WindDataViewModel(
    private val gribRepository: GribRepository = GribRepository()
) : ViewModel() {

    private val _windDataState = MutableStateFlow<Result<WindData>?>(null)
    val windDataState: StateFlow<Result<WindData>?> = _windDataState

    init {
        fetchWindData()
    }

    fun fetchWindData() {
        viewModelScope.launch {
            val responseBody = gribRepository.getGribFiles("oslofjord", "weather")
            if (responseBody != null) {
                val raw = responseBody.string()
                val windData = WindData(raw)
                _windDataState.value = Result.Success(windData)
            } else {
                _windDataState.value = Result.Error(Exception("Failed to fetch wind data"))
            }
        }
    }
}