package com.powergrid.exemployee.domain.repository

import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.*

interface EmployeeRepository {
    suspend fun getEmployeeInfo(token: String): UiState<Employee>
    suspend fun getNotices(token: String): UiState<List<Notice>>
    suspend fun getDependants(token: String): UiState<List<Dependant>>
    suspend fun getVerificationItems(token: String): UiState<List<VerificationDoc>>
}
