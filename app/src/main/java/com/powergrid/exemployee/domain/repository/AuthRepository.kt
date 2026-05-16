package com.powergrid.exemployee.domain.repository

import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.CaptchaData

interface AuthRepository {
    suspend fun fetchCaptcha(): UiState<CaptchaData>
    suspend fun loginWithPassword(username: String, password: String, captchaToken: String, captchaAnswer: Int): UiState<String>
    suspend fun sendOtp(username: String, captchaToken: String, captchaAnswer: Int): UiState<Unit>
    suspend fun verifyOtp(username: String, otp: String): UiState<String>
}
