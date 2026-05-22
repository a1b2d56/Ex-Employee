package com.powergrid.exemployee.ui.noticeboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.domain.model.Notice
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeboardViewModel @Inject constructor(
    private val repo: EmployeeRepository,
) : ViewModel() {

    private val _notices = MutableStateFlow<UiState<List<Notice>>>(UiState.Idle)
    val notices: StateFlow<UiState<List<Notice>>> = _notices.asStateFlow()

    fun load(token: String) = viewModelScope.launch {
        _notices.value = UiState.Loading
        _notices.value = repo.getNotices(token)
    }
}
