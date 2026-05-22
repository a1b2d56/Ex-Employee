package com.powergrid.exemployee.ui.settings

import androidx.lifecycle.ViewModel
import com.powergrid.exemployee.security.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val biometric: BiometricHelper
) : ViewModel() {
    fun isBiometricAvailable() = biometric.isAvailable()
    fun hasBiometricSecret() = biometric.hasStoredSecret()
}
