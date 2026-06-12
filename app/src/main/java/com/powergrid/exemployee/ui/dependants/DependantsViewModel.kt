package com.powergrid.exemployee.ui.dependants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Dependant
import com.powergrid.exemployee.domain.model.Employee
import com.powergrid.exemployee.domain.model.VerificationDoc
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DependantsUiState(
    val employee: Employee? = null,
    val dependants: List<Dependant> = emptyList(),
    val verificationDocs: List<VerificationDoc> = emptyList(),
    val expandedCardId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** True only when every dependant's status is "Updated" (0) */
    val allVerified: Boolean
        get() = dependants.isNotEmpty() && dependants.all { it.status == "Updated" }
}

@HiltViewModel
class DependantsViewModel @Inject constructor(
    private val repo: EmployeeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DependantsUiState())
    val state: StateFlow<DependantsUiState> = _state.asStateFlow()

    fun load(token: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        val empResult = repo.getEmployeeInfo(token)
        val depResult = repo.getDependants(token)
        val docResult = repo.getVerificationItems(token)

        val employee = if (empResult is UiState.Success) empResult.data else null
        val dependants = if (depResult is UiState.Success) depResult.data else emptyList()
        val docs = if (docResult is UiState.Success) docResult.data else emptyList()

        if (empResult is UiState.Error && depResult is UiState.Error) {
            _state.update { it.copy(isLoading = false, errorMessage = empResult.message) }
        } else {
            _state.value = DependantsUiState(
                employee = employee,
                dependants = dependants,
                verificationDocs = docs,
                isLoading = false
            )
        }
    }

    /** Toggle expand: same id → collapse, different id → swap */
    fun toggleExpand(id: String) {
        _state.update { current ->
            current.copy(
                expandedCardId = if (current.expandedCardId == id) null else id
            )
        }
    }

    private val _uploadState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uploadState: StateFlow<UiState<Unit>> = _uploadState.asStateFlow()

    fun uploadDocuments(photoUriStr: String, docUriStr: String) = viewModelScope.launch {
        _uploadState.value = UiState.Loading
        kotlinx.coroutines.delay(1500) // Simulate network call
        _uploadState.value = UiState.Success(Unit)
    }

    fun resetUploadState() {
        _uploadState.value = UiState.Idle
    }
}
