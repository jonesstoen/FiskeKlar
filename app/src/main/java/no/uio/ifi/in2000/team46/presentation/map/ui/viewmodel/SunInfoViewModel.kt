package no.uio.ifi.in2000.team46.presentation.map.ui.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.repository.SunInfoRepository
import no.uio.ifi.in2000.team46.data.repository.SunInfoResult
import no.uio.ifi.in2000.team46.domain.model.metalerts.SunInfo
import java.util.*
import java.text.SimpleDateFormat


@RequiresApi(Build.VERSION_CODES.O)
class SunInfoViewModel : ViewModel() {
    private val sunInfoRepository = SunInfoRepository()

    private val _sunInfo = MutableLiveData<SunInfo>()
    val sunInfo: LiveData<SunInfo> get() = _sunInfo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        fetchSunInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchSunInfo() {
        viewModelScope.launch {
            val currentdate = getCurrentDateTimeWithOffset()
            when (val result = sunInfoRepository.getSunInfo(59.933333, 10.716667, currentdate)) {
                is SunInfoResult.Success -> _sunInfo.value = result.data
                is SunInfoResult.Error -> _error.value = result.exception.message
                else -> _error.value = "Unknown error occurred"
            }
        }
    }
    private fun getCurrentDateTimeWithOffset(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
        return dateFormat.format(Date())
    }


}
