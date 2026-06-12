package com.powergrid.exemployee.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaptchaResponse(
    @SerialName("uniqueId") val uniqueId: String,
    @SerialName("number1") val number1: Int,
    @SerialName("number2") val number2: Int,
    @SerialName("operation") val operation: String
)

@Serializable
data class CaptchaResponseDto(
    @SerialName("uniqueId") val uniqueId: String,
    @SerialName("response") val response: Int
)

@Serializable
data class EmployeeLoginDto(
    @SerialName("userName") val userName: String,
    @SerialName("password") val password: String,
    @SerialName("appVersion") val appVersion: String
)

@Serializable
data class EmployeeLoginCaptchaDto(
    @SerialName("employeeLoginDto") val employeeLoginDto: EmployeeLoginDto,
    @SerialName("captchaResponseDto") val captchaResponseDto: CaptchaResponseDto
)

@Serializable
data class OtpSendRequest(
    @SerialName("userName") val userName: String,
    @SerialName("appVersion") val appVersion: String
)

@Serializable
data class OtpVerifyRequest(
    @SerialName("username") val username: String,
    @SerialName("otp") val otp: String
)

@Serializable
data class AuthResponse(
    @SerialName("auth_token") val authToken: String,
    @SerialName("employee_id") val employeeId: String
)

@Serializable
data class EmployeeResponse(
    @SerialName("employee_id") val employeeId: String,
    @SerialName("name") val name: String,
    @SerialName("age") val age: Int = 0,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("posting_region") val postingRegion: String,
    @SerialName("designation") val designation: String,
    @SerialName("dob") val dob: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("photo") val photo: String? = null,
    @SerialName("liveliness_status") val livelinessStatus: Int = 0
)

@Serializable
data class NoticeResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("date") val date: String,
    @SerialName("urgent") val urgent: Boolean = false,
    @SerialName("pdf_path") val pdfPath: String? = null
)

@Serializable
data class FamilyMemberResponse(
    @SerialName("name") val name: String,
    @SerialName("age") val age: Int,
    @SerialName("dob") val dob: String,
    @SerialName("photo") val photo: String? = null,
    @SerialName("relation") val relation: String,
    @SerialName("liveliness_status") val livelinessStatus: Int = 0
)

@Serializable
data class ProfileDocumentResponse(
    @SerialName("id") val id: String,
    @SerialName("doc_type") val docType: String,
    @SerialName("update_status") val updateStatus: Int
)

@Serializable
data class CertificateStatusResponse(
    @SerialName("id") val id: String,
    @SerialName("doc_type") val docType: String,
    @SerialName("status") val status: Int
)

@Serializable
data class EmployeeMockContainer(
    @SerialName("employee") val employee: EmployeeResponse,
    @SerialName("family") val family: List<FamilyMemberResponse>,
    @SerialName("profile_documents") val profileDocuments: List<ProfileDocumentResponse>,
    @SerialName("certificate_status") val certificateStatus: List<CertificateStatusResponse>
)

@Serializable
data class ApiEnvelope<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: T? = null,
    @SerialName("message") val message: String? = null
)
