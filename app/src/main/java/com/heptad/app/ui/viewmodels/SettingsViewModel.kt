package com.heptad.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heptad.app.data.preferences.ThemeMode
import com.heptad.app.data.preferences.UserPreferences
import com.heptad.app.data.preferences.UserPreferencesData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val preferences: StateFlow<UserPreferencesData> = userPreferences.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesData()
        )

    fun setDictionaryLevel(level: Int) {
        viewModelScope.launch {
            userPreferences.setDictionaryLevel(level)
        }
    }

    fun setIncludeS(include: Boolean) {
        viewModelScope.launch {
            userPreferences.setIncludeS(include)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setAllHintsAvailable(available: Boolean) {
        viewModelScope.launch {
            userPreferences.setAllHintsAvailable(available)
        }
    }
}
