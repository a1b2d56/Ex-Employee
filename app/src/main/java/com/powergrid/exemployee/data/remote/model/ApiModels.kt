package com.powergrid.exemployee.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class CaptchaResponse(
    @SerialName("token") val token: String,
    @SerialName("num1")  val num1:  Int,
    @SerialName("num2")  val num2:  Int,
)

@Serializable data class LoginRequest(
    @SerialName("username")       val username:      String,
    @SerialName("password")       val password:      String,
    @SerialName("captcha_token")  val captchaToken:  String,
    @SerialName("captcha_answer") val captchaAnswer: Int,
)

@Serializable data class OtpRequest(
    @SerialName("username")       val username:      String,
    @SerialName("captcha_token")  val captchaToken:  String,
    @SerialName("captcha_answer") val captchaAnswer: Int,
)

@Serializable data class OtpVerifyRequest(
    @SerialName("username") val username: String,
    @SerialName("otp")      val otp:      String,
)

@Serializable data class AuthResponse(
    @SerialName("auth_token")  val authToken:  String,
    @SerialName("employee_id") val employeeId: String,
)

@Serializable data class EmployeeResponse(
    @SerialName("employee_id")   val employeeId:  String,
    @SerialName("name")          val name:        String,
    @SerialName("age")           val age:         Int,
    @SerialName("email")         val email:       String,
    @SerialName("phone")         val phone:       String,
    @SerialName("department")    val department:  String,
    @SerialName("designation")   val designation: String,
    @SerialName("date_of_birth") val dob:         String,
    @SerialName("photo_url")     val photoUrl:    String? = null,
)

@Serializable data class NoticeResponse(
    @SerialName("id")      val id:      String,
    @SerialName("title")   val title:   String,
    @SerialName("content") val content: String,
    @SerialName("date")    val date:    String,
    @SerialName("urgent")  val urgent:  Boolean = false,
)

@Serializable data class DependantResponse(
    @SerialName("id")       val id:       String,
    @SerialName("name")     val name:     String,
    @SerialName("relation") val relation: String,
    @SerialName("age")      val age:      Int,
    @SerialName("status")   val status:   String,
)

@Serializable data class VerificationItem(
    @SerialName("id")          val id:         String,
    @SerialName("doc_type")    val docType:    String,
    @SerialName("status")      val status:     String,
    @SerialName("verified_on") val verifiedOn: String? = null,
    @SerialName("remarks")     val remarks:    String? = null,
)

@Serializable data class ApiEnvelope<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("data")    val data:    T?      = null,
    @SerialName("message") val message: String? = null,
)
