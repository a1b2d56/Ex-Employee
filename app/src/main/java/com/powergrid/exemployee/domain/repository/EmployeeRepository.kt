package com.powergrid.exemployee.domain.repository

import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.*

/**
 * Single bundle returned by the "fetch everything" API.
 * When real Retrofit is wired, this maps to the login response payload.
 */
data class AppData(
    val employee: Employee,
    val family: List<FamilyMember>,
    val dependants: List<Dependant>,
    val notices: List<Notice>,
    val verificationDocs: List<VerificationDoc>
)

interface EmployeeRepository {
    /** Single call that fetches everything the app needs after login. */
    suspend fun getAllData(token: String): UiState<AppData>

    // ── Convenience accessors (read from cached AppData) ──
    suspend fun getEmployeeInfo(token: String): UiState<Employee>
    suspend fun getNotices(token: String): UiState<List<Notice>>
    suspend fun getDependants(token: String): UiState<List<Dependant>>
    suspend fun getVerificationItems(token: String): UiState<List<VerificationDoc>>
    suspend fun getFamilyMembers(token: String): UiState<List<FamilyMember>>
}
