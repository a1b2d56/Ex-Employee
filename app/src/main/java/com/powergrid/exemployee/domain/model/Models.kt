package com.powergrid.exemployee.domain.model

@Suppress("unused")
data class CaptchaData(val token: String, val num1: Int, val num2: Int) {
    val question: String get() = "$num1  +  $num2  =  ?"
    val answer:   Int    get() = num1 + num2
}

data class Employee(
    val employeeId:  String, val name:        String,
    val age:         Int,    val email:       String,
    val phone:       String, val department:  String,
    val designation: String, val dob:         String,
    val photoUrl:    String?,
)

data class Notice(val id: String, val title: String, val content: String, val date: String, val urgent: Boolean)

data class Dependant(val id: String, val name: String, val relation: String, val age: Int, val status: String)

data class VerificationDoc(val id: String, val docType: String, val status: String, val verifiedOn: String?, val remarks: String?)
