package no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.AisRepository
import no.uio.ifi.in2000.team46.data.model.AisVesselPosition
import no.uio.ifi.in2000.team46.data.model.VesselTypes

class AisViewModel : ViewModel() {
    private val TAG = "AisViewModel"
    private val repository = AisRepository()

    private val _vesselPositions = MutableLiveData<List<AisVesselPosition>>()
    val vesselPositions: LiveData<List<AisVesselPosition>> = _vesselPositions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLayerVisible = MutableLiveData(false)
    val isLayerVisible: LiveData<Boolean> = _isLayerVisible

    private val _selectedVesselTypes = MutableLiveData<Set<Int>>(emptySet())
    val selectedVesselTypes: LiveData<Set<Int>> = _selectedVesselTypes

    private var updateJob: Job? = null
    private var currentMinLon: Double? = null
    private var currentMinLat: Double? = null
    private var currentMaxLon: Double? = null
    private var currentMaxLat: Double? = null

    init {
        // Initialisere med alle fartøystyper valgt som standard
        selectAllVesselTypes()
    }

    // Aktiverer laget uten å endre valgte fartøystyper
    fun activateLayer() {
        if (_isLayerVisible.value != true) {
            // Hvis ingen fartøystyper er valgt når laget aktiveres, velg alle
            if (_selectedVesselTypes.value?.isEmpty() == true) {
                selectAllVesselTypes()
            }

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

        if (!currentVisibility) {
            // Hvis ingen fartøystyper er valgt når laget slås på, velg alle
            if (_selectedVesselTypes.value?.isEmpty() == true) {
                selectAllVesselTypes()
            }
            _isLayerVisible.value = true
            startUpdateJob()
        } else {
            _isLayerVisible.value = false
            stopUpdateJob()
            // Tøm fartøysposisjonene når laget skjules
            _vesselPositions.value = emptyList()
        }
    }

    fun toggleVesselType(vesselType: Int) {
        val currentTypes = _selectedVesselTypes.value ?: emptySet()
        val newTypes = if (currentTypes.contains(vesselType)) {
            currentTypes - vesselType
        } else {
            currentTypes + vesselType
        }
        _selectedVesselTypes.value = newTypes

        // Sjekk om vi har fjernet alle fartøystyper
        if (newTypes.isEmpty() && _isLayerVisible.value == true) {
            // Hvis ingen fartøystyper er valgt, deaktiver AIS-laget
            deactivateLayer()
        } else {
            // Oppdater fartøysposisjoner hvis AIS-laget er synlig
            if (_isLayerVisible.value == true) {
                fetchVesselPositions()
            }
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
        val allTypes = VesselTypes.ALL_TYPES.values.toSet()
        _selectedVesselTypes.value = allTypes

        // Hent nye posisjoner hvis laget er synlig
        if (_isLayerVisible.value == true) {
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

                result.onSuccess { vessels ->
                    val selectedTypes = _selectedVesselTypes.value ?: emptySet()

                    // Hvis ingen typer er valgt, vis ingen fartøy
                    val filteredVessels = if (selectedTypes.isEmpty()) {
                        emptyList()
                    } else {
                        vessels.filter { vessel -> selectedTypes.contains(vessel.shipType) }
                    }

                    _vesselPositions.value = filteredVessels
                    _error.value = null
                }.onFailure { exception ->
                    _error.value = "Feil ved henting av fartøydata: ${exception.message}"
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