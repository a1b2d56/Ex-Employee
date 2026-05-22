package com.powergrid.exemployee.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.CaptchaData
import com.powergrid.exemployee.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepo: AuthRepository) : ViewModel() {
    private val _captcha = MutableStateFlow<UiState<CaptchaData>>(UiState.Idle)
    val captcha: StateFlow<UiState<CaptchaData>> = _captcha.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginState: StateFlow<UiState<String>> = _loginState.asStateFlow()

    private val _otpSent = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val otpSent: StateFlow<UiState<Unit>> = _otpSent.asStateFlow()

    private val _otpVerify = MutableStateFlow<UiState<String>>(UiState.Idle)
    val otpVerify: StateFlow<UiState<String>> = _otpVerify.asStateFlow()

    fun loadCaptcha() = viewModelScope.launch {
        _captcha.value = UiState.Loading
        _captcha.value = authRepo.fetchCaptcha()
    }

    fun loginPassword(username: String, password: String, captchaToken: String, captchaAnswer: Int) =
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            _loginState.value = authRepo.loginWithPassword(username, password, captchaToken, captchaAnswer)
        }

    fun sendOtp(username: String, captchaToken: String, captchaAnswer: Int) = viewModelScope.launch {
        _otpSent.value = UiState.Loading
        _otpSent.value = authRepo.sendOtp(username, captchaToken, captchaAnswer)
    }

    fun verifyOtp(username: String, otp: String) = viewModelScope.launch {
        _otpVerify.value = UiState.Loading
        _otpVerify.value = authRepo.verifyOtp(username, otp)
    }

    fun resetLogin() { _loginState.value = UiState.Idle }
    fun resetOtpSent() { _otpSent.value = UiState.Idle }
    fun resetOtpVerify() { _otpVerify.value = UiState.Idle }
}
