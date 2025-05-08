package no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapOnboardingViewModel : ViewModel() {
    private val _showMapOnboarding = MutableStateFlow(false)
    val showMapOnboarding: StateFlow<Boolean> = _showMapOnboarding

    fun showMapOnboarding() {
        _showMapOnboarding.value = true
    }

    fun hideMapOnboarding() {
        _showMapOnboarding.value = false
    }

    fun checkFirstLaunch(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val hasSeenMapOnboarding = prefs.getBoolean("has_seen_map_onboarding", false)
            
            if (!hasSeenMapOnboarding) {
                _showMapOnboarding.value = true
                prefs.edit().putBoolean("has_seen_map_onboarding", true).apply()
            }
        }
    }
} 