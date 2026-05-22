package com.powergrid.exemployee.data.remote

import com.powergrid.exemployee.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

@Suppress("unused")
interface AuthApi {
    /** STUB: Returns a captcha token + two numbers whose sum is the answer. */
    @GET("auth/captcha")
    suspend fun getCaptcha(): Response<ApiEnvelope<CaptchaResponse>>

    /** STUB: Password login — returns auth token on success. */
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): Response<ApiEnvelope<AuthResponse>>

    /** STUB: Send OTP to username's registered mobile. */
    @POST("auth/otp/send")
    suspend fun sendOtp(@Body req: OtpRequest): Response<ApiEnvelope<Unit>>

    /** STUB: Verify OTP entered by user. Returns auth token. */
    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body req: OtpVerifyRequest): Response<ApiEnvelope<AuthResponse>>
}
