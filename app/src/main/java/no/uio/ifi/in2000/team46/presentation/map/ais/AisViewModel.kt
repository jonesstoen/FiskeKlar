package no.uio.ifi.in2000.team46.presentation.map.ais

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.AisRepository
import no.uio.ifi.in2000.team46.domain.ais.AisVesselPosition
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import no.uio.ifi.in2000.team46.data.repository.Result

// summary: manages fetching and filtering of ais vessel positions and controls layer visibility
// main function: exposes stateflows for vessel data, loading and error states, handles periodic data updates based on map viewport

class AisViewModel : ViewModel() {
    // tag used for logging errors or debug info
    private val tag = "AisViewModel"
    // repository for fetching ais data
    private val repository = AisRepository()

    // holds current list of vessel positions
    private val _vesselPositions = MutableStateFlow<List<AisVesselPosition>>(emptyList())
    val vesselPositions: MutableStateFlow<List<AisVesselPosition>> = _vesselPositions

    // indicates whether data is currently loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: MutableStateFlow<Boolean> = _isLoading

    // holds error message if fetch fails
    private val _error = MutableStateFlow<String?>(null)
    val error: MutableStateFlow<String?> = _error

    // controls whether ais layer should be visible on map
    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: MutableStateFlow<Boolean> = _isLayerVisible

    // holds currently selected vessel types for filtering
    private val _selectedVesselTypes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedVesselTypes: MutableStateFlow<Set<Int>> = _selectedVesselTypes

    // job for periodic updates
    private var updateJob: Job? = null
    // current viewport bounds for fetching data
    private var currentMinLon: Double? = null
    private var currentMinLat: Double? = null
    private var currentMaxLon: Double? = null
    private var currentMaxLat: Double? = null

    // selected vessel for info dialog
    private val _selectedVessel = mutableStateOf<AisVesselPosition?>(null)
    val selectedVessel: State<AisVesselPosition?> = _selectedVessel

    init {
        // initialize by selecting all vessel types by default
        selectAllVesselTypes()
    }

    // set selected vessel and prepare dialog info
    fun showVesselInfo(
        mmsi: String,
        name: String,
        speed: Double,
        course: Double,
        heading: Double,
        shipType: Int
    ) {
        _selectedVessel.value = AisVesselPosition(
            msgtime = mmsi,  // using mmsi string for message time
            mmsi = mmsi.toLong(),  // convert mmsi to long
            name = name,
            speedOverGround = speed,
            courseOverGround = course,
            trueHeading = heading.toInt(),  // convert heading to int
            navigationalStatus = 0,  // default nav status
            shipType = shipType,
            longitude = 0.0,  // placeholder coord
            latitude = 0.0,
            rateOfTurn = 0.0  // default rate of turn
        )
    }

    // clear selected vessel info
    fun hideVesselInfo() {
        _selectedVessel.value = null
    }

    // activate ais layer and start periodic updates
    fun activateLayer() {
        if (!_isLayerVisible.value) {
            _isLayerVisible.value = true
            startUpdateJob()
        }
    }

    // deactivate layer and stop updates, clear data
    fun deactivateLayer() {
        if (_isLayerVisible.value) {
            _isLayerVisible.value = false
            stopUpdateJob()
            _vesselPositions.value = emptyList()  // clear positions
        }
    }

    // toggle layer visibility and manage update job
    fun toggleLayerVisibility() {
        val current = _isLayerVisible.value
        _isLayerVisible.value = !current
        if (!_isLayerVisible.value) {
            stopUpdateJob()
            _vesselPositions.value = emptyList()
        } else {
            startUpdateJob()
        }
    }

    // select all available vessel types for filtering
    fun selectAllVesselTypes() {
        _selectedVesselTypes.value = VesselTypes.ALL_TYPES.values.toSet()
        if (_isLayerVisible.value) {
            fetchVesselPositions()
        }
    }

    // clear all vessel type filters
    fun clearSelectedVesselTypes() {
        _selectedVesselTypes.value = emptySet()
        if (_isLayerVisible.value) {
            _vesselPositions.value = emptyList()
        }
    }

    // add or remove a vessel type from filters
    fun toggleVesselType(vesselType: Int) {
        val current = _selectedVesselTypes.value
        val newSet = if (current.contains(vesselType)) current - vesselType else current + vesselType
        _selectedVesselTypes.value = newSet
        if (newSet.isEmpty() && _isLayerVisible.value) deactivateLayer() else if (_isLayerVisible.value) fetchVesselPositions()
    }

    // update viewport bounds and fetch data if visible
    fun updateVisibleRegion(minLon: Double, minLat: Double, maxLon: Double, maxLat: Double) {
        currentMinLon = minLon
        currentMinLat = minLat
        currentMaxLon = maxLon
        currentMaxLat = maxLat
        if (_isLayerVisible.value) fetchVesselPositions()
    }

    // start periodic data update job
    private fun startUpdateJob() {
        stopUpdateJob()
        updateJob = viewModelScope.launch {
            while (isActive && _isLayerVisible.value) {
                fetchVesselPositions()
                delay(30000)  // wait 30 seconds
            }
        }
    }

    // cancel update job
    private fun stopUpdateJob() {
        updateJob?.cancel()
        updateJob = null
    }

    // fetch vessel positions from repository and apply filters
    private fun fetchVesselPositions() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.getVesselPositions(
                    minLon = currentMinLon,
                    minLat = currentMinLat,
                    maxLon = currentMaxLon,
                    maxLat = currentMaxLat
                )
                when (result) {
                    is Result.Success -> {
                        val vessels = result.data
                        val filtered = vessels.filter { vessel ->
                            _selectedVesselTypes.value.contains(vessel.shipType)
                        }
                        _vesselPositions.value = filtered
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = "error fetching vessel data: ${result.exception.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // manual refresh of data if layer visible
    fun refreshVesselPositions() {
        if (_isLayerVisible.value) fetchVesselPositions()
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdateJob()  // ensure no jobs remain
    }
}
