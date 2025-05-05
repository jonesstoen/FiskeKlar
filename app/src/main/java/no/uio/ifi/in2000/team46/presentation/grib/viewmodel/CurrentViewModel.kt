package no.uio.ifi.in2000.team46.presentation.grib.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
import no.uio.ifi.in2000.team46.data.repository.Result

class CurrentViewModel(private val repository: CurrentRepository) : ViewModel() {
    private val _currentData = MutableStateFlow<Result<List<CurrentVector>>?>(null)

    val currentData: StateFlow<Result<List<CurrentVector>>?> = _currentData


    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    fun toggleLayerVisibility() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) {
            fetchCurrentData()
        }
    }

    private fun fetchCurrentData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val result = repository.getCurrentData(forceRefresh)
            _currentData.value = result
            Log.d("CurrentViewModel", "Fetched current data: $result")

        }
    }

}