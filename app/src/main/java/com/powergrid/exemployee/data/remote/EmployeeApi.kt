package com.powergrid.exemployee.data.remote

import com.powergrid.exemployee.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface EmployeeApi {
    @GET("v1/employee/info")
    suspend fun getEmployeeInfo(
        @Header("Authorization") token: String
    ): Response<ApiEnvelope<EmployeeMockContainer>>

    @GET("v1/employee/noticeboard")
    suspend fun getNotices(
        @Header("Authorization") token: String
    ): Response<ApiEnvelope<List<NoticeResponse>>>
}
