package com.powergrid.exemployee.domain.model

@Suppress("unused")
data class CaptchaData(val token: String, val num1: Int, val num2: Int, val operation: String = "+") {
    val question: String get() = "$num1  $operation  $num2  =  ?"
    val answer:   Int    get() = when(operation) {
        "+" -> num1 + num2
        "-" -> num1 - num2
        "*" -> num1 * num2
        else -> num1 + num2
    }
}

data class Employee(
    val employeeId:  String,
    val name:        String,
    val designation: String,
    val dob:         String,
    val phone:       String,
    val postingRegion: String,
    val email:       String,
    val photo:       String?,
) {
    val photoUrl: String? get() = photo
    @Suppress("unused")
    val department: String get() = postingRegion
    @Suppress("unused")
    val age: Int get() = try {
        val parts = dob.split("-")
        if (parts.size == 3) {
            val day = parts[0].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull() ?: 0
            val year = parts[2].toIntOrNull() ?: 0
            val dobDate = java.time.LocalDate.of(year, month, day)
            val years = java.time.Period.between(dobDate, java.time.LocalDate.now()).years
            if (years < 0) 0 else years
        } else 0
    } catch (e: Exception) {
        0
    }
}

data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val urgent: Boolean,
    val pdfPath: String? = null
)

data class FamilyMember(
    val name: String,
    val age: Int,
    val dob: String,
    val photo: String?,
    val relation: String
)

data class Dependant(val id: String, val name: String, val relation: String, val age: Int, val dob: String, val status: String)

data class VerificationDoc(val id: String, val docType: String, val status: String, val verifiedOn: String?, val remarks: String?)
