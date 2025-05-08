package no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding

    fun showOnboarding() {
        _showOnboarding.value = true
    }

    fun hideOnboarding() {
        _showOnboarding.value = false
    }

    fun checkFirstLaunch(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
            
            if (!hasSeenOnboarding) {
                _showOnboarding.value = true
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
            }
        }
    }
} 