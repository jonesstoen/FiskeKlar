package no.uio.ifi.in2000.team46.presentation.map.components.controls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// This class is responsible for managing the state of the legend panel in the map view.
class LegendController {
    private var _openLegend by mutableStateOf<Int?>(null)

    fun toggle(id: Int) {
        _openLegend = if (_openLegend == id) null else id
    }

    fun isOpen(id: Int): Boolean = _openLegend == id

    fun close() {
        _openLegend = null
    }
}
