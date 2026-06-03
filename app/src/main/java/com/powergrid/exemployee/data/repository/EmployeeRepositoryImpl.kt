package com.powergrid.exemployee.data.repository

import android.content.Context
import com.powergrid.exemployee.common.UiState

import com.powergrid.exemployee.data.remote.model.EmployeeMockContainer
import com.powergrid.exemployee.domain.model.*
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : EmployeeRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedMockData: EmployeeMockContainer? = null

    private fun loadMockData(): EmployeeMockContainer {
        cachedMockData?.let { return it }
        val open = context.assets.open("mock/employee_mock.json")
        val reader = BufferedReader(InputStreamReader(open, "UTF-8"))
        val content = reader.use { it.readText() }
        val data = json.decodeFromString<EmployeeMockContainer>(content)
        cachedMockData = data
        return data
    }

    override suspend fun getEmployeeInfo(token: String): UiState<Employee> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")
        val container = loadMockData()
        val d = container.employee
        UiState.Success(
            Employee(
                employeeId = d.employeeId,
                name = d.name,
                designation = d.designation,
                dob = d.dob,
                phone = d.phone,
                postingRegion = d.postingRegion,
                email = d.email,
                photo = d.photo ?: d.photoUrl
            )
        )
    }

    override suspend fun getNotices(token: String): UiState<List<Notice>> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")
        val open = context.assets.open("mock/notices_mock.json")
        val reader = BufferedReader(InputStreamReader(open, "UTF-8"))
        val content = reader.use { it.readText() }
        val list = json.decodeFromString<List<com.powergrid.exemployee.data.remote.model.NoticeResponse>>(content)
        val notices = list.map { d ->
            Notice(
                id = d.id,
                title = d.title,
                content = d.content,
                date = d.date,
                urgent = d.urgent,
                pdfPath = d.pdfPath
            )
        }
        UiState.Success(notices)
    }

    override suspend fun getDependants(token: String): UiState<List<Dependant>> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")
        val container = loadMockData()
        val list = container.family.mapIndexed { index, d ->
            val statusStr = when (index) {
                0 -> "Needs verification"
                1 -> "In process"
                else -> "No need to update"
            }
            Dependant(
                id = d.name,
                name = d.name,
                relation = d.relation,
                age = d.age,
                dob = d.dob,
                status = statusStr
            )
        }
        UiState.Success(list)
    }

    override suspend fun getVerificationItems(token: String): UiState<List<VerificationDoc>> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")
        val container = loadMockData()
        val list = container.certificateStatus.map { d ->
            val statusStr = when (d.status) {
                2 -> "verified"
                1 -> "pending"
                0 -> "rejected"
                else -> "pending"
            }
            val remarks = if (d.status == 0) "Incorrect details — resubmit" else if (d.status == 1) "Awaiting HR approval" else null
            val verifiedOn = if (d.status == 2) "12 Jan 2024" else null
            VerificationDoc(
                id = d.id,
                docType = d.docType,
                status = statusStr,
                verifiedOn = verifiedOn,
                remarks = remarks
            )
        }
        UiState.Success(list)
    }

    override suspend fun getFamilyMembers(token: String): UiState<List<FamilyMember>> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")
        val container = loadMockData()
        val list = container.family.map { d ->
            FamilyMember(
                name = d.name,
                age = d.age,
                dob = d.dob,
                photo = d.photo,
                relation = d.relation
            )
        }
        UiState.Success(list)
    }
}

private inline fun <T> safeCall(block: () -> UiState<T>): UiState<T> = try {
    block()
} catch (e: Exception) {
    UiState.Error(e.message ?: "Unknown error")
}
