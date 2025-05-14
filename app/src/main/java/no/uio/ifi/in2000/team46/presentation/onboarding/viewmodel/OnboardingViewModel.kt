package no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//  manages whether onboarding should be shown and persists first-launch flag
// main function: exposes stateflow to control onboarding visibility and checks shared preferences on first launch

class OnboardingViewModel : ViewModel() {
    // holds current onboarding visibility state
    private val _showOnboarding = MutableStateFlow(false)
    // public read-only stateflow for composables to observe
    val showOnboarding: StateFlow<Boolean> = _showOnboarding

    // call to display onboarding
    fun showOnboarding() {
        _showOnboarding.value = true
    }

    // call to hide onboarding
    fun hideOnboarding() {
        _showOnboarding.value = false
    }

    // checks if app is launched for first time and updates preference
    fun checkFirstLaunch(context: Context) {
        viewModelScope.launch {
            // retrieve shared preferences
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            // read flag if onboarding has been seen before
            val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)

            if (!hasSeenOnboarding) {
                // mark onboarding as shown and update state
                _showOnboarding.value = true
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
            }
        }
    }
}
