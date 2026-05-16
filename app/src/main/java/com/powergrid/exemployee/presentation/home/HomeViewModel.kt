package com.powergrid.exemployee.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Employee
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: EmployeeRepository) : ViewModel() {
    private val _employee = MutableStateFlow<UiState<Employee>>(UiState.Idle)
    val employee: StateFlow<UiState<Employee>> = _employee

    fun loadEmployee(token: String) {
        if (_employee.value is UiState.Success) return
        viewModelScope.launch { _employee.value = UiState.Loading; _employee.value = repo.getEmployeeInfo(token) }
    }
}
