package com.powergrid.exemployee.data.remote

import com.powergrid.exemployee.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @GET("attd-api-v2/Auth/get-captcha")
    suspend fun getCaptcha(
        @Header("AuthToken") token: String
    ): Response<CaptchaResponse>

    @GET("attd-api-v2/Auth/get-auth-token")
    suspend fun getOtpAuthToken(
        @Query("appVersion") appVersion: String
    ): Response<String>

    @POST("attd-api-v2/Auth/post-v2")
    suspend fun loginWithCaptcha(
        @Body req: EmployeeLoginCaptchaDto
    ): Response<AuthResponse>

    @POST("attd-api-v2/Auth/otp")
    suspend fun sendOtp(
        @Header("AuthToken") token: String,
        @Body req: OtpSendRequest
    ): Response<Unit>

    @POST("attd-api-v2/Auth/authenticate-otp")
    suspend fun verifyOtp(
        @Header("AuthToken") token: String,
        @Body req: OtpVerifyRequest
    ): Response<AuthResponse>
}
