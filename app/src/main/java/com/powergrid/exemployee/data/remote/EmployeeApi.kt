package com.powergrid.exemployee.data.remote

import com.powergrid.exemployee.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

@Suppress("unused")
interface EmployeeApi {
    @GET("employee/info")
    suspend fun getEmployeeInfo(@Header("Authorization") token: String): Response<ApiEnvelope<EmployeeResponse>>

    @GET("employee/noticeboard")
    suspend fun getNotices(@Header("Authorization") token: String): Response<ApiEnvelope<List<NoticeResponse>>>

    @GET("employee/dependants")
    suspend fun getDependants(@Header("Authorization") token: String): Response<ApiEnvelope<List<DependantResponse>>>

    @GET("employee/verification")
    suspend fun getVerificationItems(@Header("Authorization") token: String): Response<ApiEnvelope<List<VerificationItem>>>
}
