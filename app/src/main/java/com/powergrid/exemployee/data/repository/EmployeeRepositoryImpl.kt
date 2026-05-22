package com.powergrid.exemployee.data.repository

import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.data.remote.EmployeeApi
import com.powergrid.exemployee.domain.model.*
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("unused", "UNUSED_PARAMETER")
class EmployeeRepositoryImpl @Inject constructor(private val api: EmployeeApi) : EmployeeRepository {

    override suspend fun getEmployeeInfo(token: String): UiState<Employee> = safeCall {
        // ── TODO: Replace stub ────────────────────────────────────────────────
        // val resp = api.getEmployeeInfo("Bearer $token")
        // if (resp.isSuccessful && resp.body()?.success == true) {
        //     val d = resp.body()!!.data!!
        //     UiState.Success(Employee(d.employeeId,d.name,d.age,d.email,d.phone,d.department,d.designation,d.dob,d.photoUrl))
        // } else UiState.Error(resp.body()?.message ?: "Failed to load profile")
        // ──────────────────────────────────────────────────────────────────────
        UiState.Success(Employee("EMP-001","Rajesh Kumar",62,"rajesh.kumar@powergrid.in",
            "+91 98765 43210","Transmission Planning","Executive Director","15 March 1963",null))
    }

    override suspend fun getNotices(token: String): UiState<List<Notice>> = safeCall {
        UiState.Success(listOf(
            Notice("1","Pension Revision 2025","Revised pension rates effective January 2025 have been notified. Please check HR portal for updated amounts.","10 May 2025",true),
            Notice("2","Medical Reimbursement","Annual medical reimbursement for FY 2024-25 can be submitted till 31 May 2025.","05 May 2025",false),
            Notice("3","CPF Interest Rate","The CPF interest rate for FY 2024-25 has been declared at 8.25% per annum.","01 Apr 2025",false),
        ))
    }

    override suspend fun getDependants(token: String): UiState<List<Dependant>> = safeCall {
        UiState.Success(listOf(
            Dependant("d1","Sunita Kumar","Spouse",58,"active"),
            Dependant("d2","Amit Kumar","Son",32,"active"),
            Dependant("d3","Priya Sharma","Daughter",28,"active"),
        ))
    }

    override suspend fun getVerificationItems(token: String): UiState<List<VerificationDoc>> = safeCall {
        UiState.Success(listOf(
            VerificationDoc("v1","Aadhaar Card","verified","12 Jan 2024",null),
            VerificationDoc("v2","PAN Card","verified","12 Jan 2024",null),
            VerificationDoc("v3","Service Book","pending",null,"Awaiting HR approval"),
            VerificationDoc("v4","Pension Payment Order","rejected",null,"Incorrect details — resubmit"),
        ))
    }
}

private inline fun <T> safeCall(block: () -> UiState<T>): UiState<T> = try {
    block()
} catch (e: Exception) {
    UiState.Error(e.message ?: "Unknown error")
}
