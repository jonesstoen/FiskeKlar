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
import no.uio.ifi.in2000.team46.domain.model.ais.AisVesselPosition
import no.uio.ifi.in2000.team46.domain.model.ais.VesselTypes
import no.uio.ifi.in2000.team46.data.repository.Result


class AisViewModel : ViewModel() {
    private val TAG = "AisViewModel"
    private val repository = AisRepository()

    private val _vesselPositions = MutableStateFlow<List<AisVesselPosition>>(emptyList())
    val vesselPositions: MutableStateFlow<List<AisVesselPosition>> = _vesselPositions

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: MutableStateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: MutableStateFlow<String?> = _error

    private val _isLayerVisible = MutableStateFlow(false)
    val isLayerVisible: MutableStateFlow<Boolean> = _isLayerVisible

    private val _selectedVesselTypes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedVesselTypes: MutableStateFlow<Set<Int>> = _selectedVesselTypes

    private var updateJob: Job? = null
    private var currentMinLon: Double? = null
    private var currentMinLat: Double? = null
    private var currentMaxLon: Double? = null
    private var currentMaxLat: Double? = null

    private val _selectedVessel = mutableStateOf<AisVesselPosition?>(null)
    val selectedVessel: State<AisVesselPosition?> = _selectedVessel

    fun showVesselInfo(mmsi: String, name: String, speed: Double, course: Double, heading: Double, shipType: Int) {
        _selectedVessel.value = AisVesselPosition(
            msgtime = mmsi,  // MMSI is used as message time string
            mmsi = mmsi.toLong(),  // Convert MMSI to Long
            name = name,
            speedOverGround = speed,
            courseOverGround = course,
            trueHeading = heading.toInt(),  // Convert heading to Int
            navigationalStatus = 0,  // Default value
            shipType = shipType,
            longitude = 0.0,
            latitude = 0.0,
            rateOfTurn = 0.0  // Add missing required parameter
        )
    }

    fun hideVesselInfo() {
        _selectedVessel.value = null
    }

    init {
        // Initialisere med alle fartøystyper valgt som standard
        selectAllVesselTypes()
    }

    // Aktiverer laget uten å endre valgte fartøystyper
    fun activateLayer() {
        if (_isLayerVisible.value != true) {
            _isLayerVisible.value = true
            startUpdateJob()
        }
    }

    // Deaktiverer laget uten å endre valgte fartøystyper
    fun deactivateLayer() {
        if (_isLayerVisible.value == true) {
            _isLayerVisible.value = false
            stopUpdateJob()
            // Tøm fartøysposisjonene når laget skjules
            _vesselPositions.value = emptyList()
        }
    }

    fun toggleLayerVisibility() {
        val currentVisibility = _isLayerVisible.value ?: false
        _isLayerVisible.value = !currentVisibility

        if (!currentVisibility) {
            startUpdateJob()
        } else {
            stopUpdateJob()
            // Tøm fartøysposisjonene når laget skjules
            _vesselPositions.value = emptyList()
        }
    }

    // Velg kun en spesifikk fartøystype, fjern alle andre
    fun selectOnlyVesselType(vesselType: Int) {
        _selectedVesselTypes.value = setOf(vesselType)
    }

    // Fjern en spesifikk fartøystype fra utvalget
    fun deselectVesselType(vesselType: Int) {
        val currentTypes = _selectedVesselTypes.value ?: emptySet()
        val newTypes = currentTypes - vesselType
        _selectedVesselTypes.value = newTypes

        // Hvis ingen fartøystyper er valgt, deaktiver AIS-laget
        if (newTypes.isEmpty() && _isLayerVisible.value == true) {
            deactivateLayer()
        } else if (_isLayerVisible.value == true) {
            fetchVesselPositions()
        }
    }

    fun isVesselTypeSelected(vesselType: Int): Boolean {
        return _selectedVesselTypes.value?.contains(vesselType) ?: false
    }

    fun selectAllVesselTypes() {
        _selectedVesselTypes.value = VesselTypes.ALL_TYPES.values.toSet()
        if (_isLayerVisible.value) {
            fetchVesselPositions()
        }
    }

    fun clearSelectedVesselTypes() {
        _selectedVesselTypes.value = emptySet()
        if (_isLayerVisible.value) {
            _vesselPositions.value = emptyList()
        }
    }

    fun toggleVesselType(vesselType: Int) {
        val currentTypes = _selectedVesselTypes.value
        val newTypes = if (currentTypes.contains(vesselType)) {
            currentTypes - vesselType
        } else {
            currentTypes + vesselType
        }
        _selectedVesselTypes.value = newTypes

        if (newTypes.isEmpty() && _isLayerVisible.value) {
            deactivateLayer()
        } else if (_isLayerVisible.value) {
            fetchVesselPositions()
        }
    }

    fun updateVisibleRegion(minLon: Double, minLat: Double, maxLon: Double, maxLat: Double) {
        currentMinLon = minLon
        currentMinLat = minLat
        currentMaxLon = maxLon
        currentMaxLat = maxLat

        if (_isLayerVisible.value == true) {
            fetchVesselPositions()
        }
    }

    private fun startUpdateJob() {
        stopUpdateJob()

        updateJob = viewModelScope.launch {
            while (isActive && (_isLayerVisible.value == true)) {
                fetchVesselPositions()
                delay(30000) // Oppdater hvert 30. sekund
            }
        }
    }

    private fun stopUpdateJob() {
        updateJob?.cancel()
        updateJob = null
    }

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
                        val selectedTypes = _selectedVesselTypes.value ?: emptySet()

                        // Hvis ingen typer er valgt, vis ingen fartøy
                        val filteredVessels = if (selectedTypes.isEmpty()) {
                            emptyList()
                        } else {
                            vessels.filter { vessel -> selectedTypes.contains(vessel.shipType) }
                        }

                        _vesselPositions.value = filteredVessels
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = "Feil ved henting av fartøydata: ${result.exception.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Feil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshVesselPositions() {
        if (_isLayerVisible.value == true) {
            fetchVesselPositions()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdateJob()
    }
}