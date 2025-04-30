package no.uio.ifi.in2000.team46.presentation.grib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.parser.PrecipitationPoint
import no.uio.ifi.in2000.team46.data.repository.GribRepository
import no.uio.ifi.in2000.team46.data.repository.Result as RepoResult

class PrecipitationViewModel(
    private val repo: GribRepository
) : ViewModel() {

    private val _data = MutableStateFlow<RepoResult<List<PrecipitationPoint>>?>(null)
    val data: StateFlow<RepoResult<List<PrecipitationPoint>>?> = _data

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: StateFlow<Boolean> = _isLayerVisible

    fun toggleLayer() {
        _isLayerVisible.value = !_isLayerVisible.value
        if (_isLayerVisible.value) fetch()
    }

    private fun fetch() = viewModelScope.launch {
        _data.value = repo.getPrecipitationData()
    }
}

class PrecipitationViewModelFactory(
    private val repo: GribRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrecipitationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrecipitationViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
