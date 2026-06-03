package com.powergrid.exemployee.data.repository

import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.data.remote.AuthApi

import com.powergrid.exemployee.domain.model.CaptchaData
import com.powergrid.exemployee.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(private val api: AuthApi) : AuthRepository {

    override suspend fun fetchCaptcha(): UiState<CaptchaData> = safeCall {
        // ── TODO: Replace stub with real API call ─────────────────────────────
        // val resp = api.getCaptcha()
        // if (resp.isSuccessful && resp.body()?.success == true) {
        //     val d = resp.body()!!.data!!
        //     UiState.Success(CaptchaData(d.token, d.num1, d.num2))
        // } else UiState.Error(resp.body()?.message ?: "Failed to load captcha")
        // ──────────────────────────────────────────────────────────────────────
        val n1 = (1..9).random(); val n2 = (1..9).random()
        UiState.Success(CaptchaData("stub_captcha_token", n1, n2))
    }

    override suspend fun loginWithPassword(
        username: String, password: String, captchaToken: String, captchaAnswer: Int
    ): UiState<String> = safeCall {
        // ── TODO: Replace stub ────────────────────────────────────────────────
        // val resp = api.login(LoginRequest(username, password, captchaToken, captchaAnswer))
        // if (resp.isSuccessful && resp.body()?.success == true)
        //     UiState.Success(resp.body()!!.data!!.authToken)
        // else UiState.Error(resp.body()?.message ?: "Login failed")
        // ──────────────────────────────────────────────────────────────────────
        if (username.isBlank() || password.isBlank()) UiState.Error("Username and password required")
        else UiState.Success("stub_auth_token_$username")
    }

    override suspend fun sendOtp(
        username: String, captchaToken: String, captchaAnswer: Int
    ): UiState<Unit> = safeCall {
        // ── TODO: Replace stub ────────────────────────────────────────────────
        // val resp = api.sendOtp(OtpRequest(username, captchaToken, captchaAnswer))
        // if (resp.isSuccessful && resp.body()?.success == true) UiState.Success(Unit)
        // else UiState.Error(resp.body()?.message ?: "Failed to send OTP")
        // ──────────────────────────────────────────────────────────────────────
        if (username.isBlank()) UiState.Error("Username required")
        else UiState.Success(Unit)
    }

    override suspend fun verifyOtp(username: String, otp: String): UiState<String> = safeCall {
        // ── TODO: Replace stub ────────────────────────────────────────────────
        // val resp = api.verifyOtp(OtpVerifyRequest(username, otp))
        // if (resp.isSuccessful && resp.body()?.success == true)
        //     UiState.Success(resp.body()!!.data!!.authToken)
        // else UiState.Error(resp.body()?.message ?: "Invalid OTP")
        // ──────────────────────────────────────────────────────────────────────
        if (otp == "000000") UiState.Error("Invalid OTP")
        else UiState.Success("stub_auth_token_otp_$username")
    }
}

private inline fun <T> safeCall(block: () -> UiState<T>): UiState<T> = try {
    block()
} catch (e: Exception) {
    UiState.Error(e.message ?: "Unknown error")
}
