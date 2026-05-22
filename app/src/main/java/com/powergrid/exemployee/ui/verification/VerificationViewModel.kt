package com.powergrid.exemployee.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.VerificationDoc
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repo: EmployeeRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<UiState<List<VerificationDoc>>>(UiState.Idle)
    val items: StateFlow<UiState<List<VerificationDoc>>> = _items.asStateFlow()

    fun load(token: String) = viewModelScope.launch {
        _items.value = UiState.Loading
        _items.value = repo.getVerificationItems(token)
    }
}
