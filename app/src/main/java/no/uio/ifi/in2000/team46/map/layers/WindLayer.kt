package no.uio.ifi.in2000.team46.map.layers

import no.uio.ifi.in2000.team46.data.repository.GribRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WindLayer(application: Application) : AndroidViewModel(application) {
    private val repository = GribRepository(application)
    private val parser = GribParser()
    var windData: List<WindData> = emptyList()

    fun loadWindData() {
        viewModelScope.launch(Dispatchers.IO) {
            val file = repository.downloadGribFile("oslofjord")
            if (file != null) {
                windData = parser.parseWindData(file)
            }
        }
    }
}
