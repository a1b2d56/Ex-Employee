package com.powergrid.exemployee.data.repository

import android.content.Context
import com.powergrid.exemployee.common.UiState

import com.powergrid.exemployee.data.remote.model.EmployeeMockContainer
import com.powergrid.exemployee.domain.model.*
import com.powergrid.exemployee.domain.repository.AppData
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

    // ── Cached data from the single "getAllData" call ──
    private var cachedAppData: AppData? = null

    /**
     * Single API call that fetches everything.
     * For now it reads from mock JSON assets.
     */
    override suspend fun getAllData(token: String): UiState<AppData> = safeCall {
        if (token.isBlank()) return@safeCall UiState.Error("Invalid token")

        // Return cached data if available
        cachedAppData?.let { return@safeCall UiState.Success(it) }

        // Load employee + family + docs from mock
        val container = loadMockContainer()

        val employee = container.employee.let { d ->
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
        }

        val family = container.family.map { d ->
            FamilyMember(
                name = d.name,
                age = d.age,
                dob = d.dob,
                photo = d.photo,
                relation = d.relation
            )
        }

        val dependants = container.family.mapIndexed { index, d ->
            val statusStr = when (index) {
                0 -> "Needs verification"
                1 -> "In process"
                else -> "Updated"
            }
            Dependant(
                id = d.name,
                name = d.name,
                relation = d.relation,
                age = d.age,
                dob = d.dob,
                status = statusStr,
                photoUrl = d.photo
            )
        }

        // Load notices from separate mock file
        val notices = loadNoticesMock()

        val verificationDocs = container.certificateStatus.map { d ->
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

        val appData = AppData(
            employee = employee,
            family = family,
            dependants = dependants,
            notices = notices,
            verificationDocs = verificationDocs
        )
        cachedAppData = appData
        UiState.Success(appData)
    }

    // ── Convenience accessors — all read from cached AppData ──

    override suspend fun getEmployeeInfo(token: String): UiState<Employee> {
        val result = getAllData(token)
        return when (result) {
            is UiState.Success -> UiState.Success(result.data.employee)
            is UiState.Error -> UiState.Error(result.message)
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
    }

    override suspend fun getNotices(token: String): UiState<List<Notice>> {
        val result = getAllData(token)
        return when (result) {
            is UiState.Success -> UiState.Success(result.data.notices)
            is UiState.Error -> UiState.Error(result.message)
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
    }

    override suspend fun getDependants(token: String): UiState<List<Dependant>> {
        val result = getAllData(token)
        return when (result) {
            is UiState.Success -> UiState.Success(result.data.dependants)
            is UiState.Error -> UiState.Error(result.message)
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
    }

    override suspend fun getVerificationItems(token: String): UiState<List<VerificationDoc>> {
        val result = getAllData(token)
        return when (result) {
            is UiState.Success -> UiState.Success(result.data.verificationDocs)
            is UiState.Error -> UiState.Error(result.message)
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
    }

    override suspend fun getFamilyMembers(token: String): UiState<List<FamilyMember>> {
        val result = getAllData(token)
        return when (result) {
            is UiState.Success -> UiState.Success(result.data.family)
            is UiState.Error -> UiState.Error(result.message)
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
    }

    // ── Private helpers ──

    private var cachedMockContainer: EmployeeMockContainer? = null

    private fun loadMockContainer(): EmployeeMockContainer {
        cachedMockContainer?.let { return it }
        val open = context.assets.open("mock/employee_mock.json")
        val reader = BufferedReader(InputStreamReader(open, "UTF-8"))
        val content = reader.use { it.readText() }
        val data = json.decodeFromString<EmployeeMockContainer>(content)
        cachedMockContainer = data
        return data
    }

    private fun loadNoticesMock(): List<Notice> {
        val open = context.assets.open("mock/notices_mock.json")
        val reader = BufferedReader(InputStreamReader(open, "UTF-8"))
        val content = reader.use { it.readText() }
        val list = json.decodeFromString<List<com.powergrid.exemployee.data.remote.model.NoticeResponse>>(content)
        return list.map { d ->
            Notice(
                id = d.id,
                title = d.title,
                content = d.content,
                date = d.date,
                urgent = d.urgent,
                pdfPath = d.pdfPath
            )
        }
    }
}

private inline fun <T> safeCall(block: () -> UiState<T>): UiState<T> = try {
    block()
} catch (e: Exception) {
    UiState.Error(e.message ?: "Unknown error")
}
