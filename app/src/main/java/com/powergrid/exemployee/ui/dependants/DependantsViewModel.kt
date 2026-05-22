package com.powergrid.exemployee.ui.dependants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Dependant
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DependantsViewModel @Inject constructor(
    private val repo: EmployeeRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<UiState<List<Dependant>>>(UiState.Idle)
    val items: StateFlow<UiState<List<Dependant>>> = _items.asStateFlow()

    fun load(token: String) = viewModelScope.launch {
        _items.value = UiState.Loading
        _items.value = repo.getDependants(token)
    }
}
