package com.powergrid.exemployee.presentation.settings

import androidx.lifecycle.ViewModel
import com.powergrid.exemployee.common.ThemePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _currentTheme = MutableStateFlow(ThemePrefs.AppTheme.LIGHT)
    val currentTheme: StateFlow<ThemePrefs.AppTheme> = _currentTheme

    fun loadTheme(theme: ThemePrefs.AppTheme) {
        _currentTheme.value = theme
    }
}
