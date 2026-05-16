package com.powergrid.exemployee.presentation.liveliness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LivelinessViewModel @Inject constructor() : ViewModel() {
    private val _submitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val submitState: StateFlow<UiState<String>> = _submitState

    fun submit(token: String) = viewModelScope.launch {
        _submitState.value = UiState.Loading
        delay(1500) // TODO: Replace with actual API call sending photo + token
        _submitState.value = UiState.Success("Liveliness recorded. Ref: LVL-${System.currentTimeMillis()}")
    }

    fun reset() { _submitState.value = UiState.Idle }
}
