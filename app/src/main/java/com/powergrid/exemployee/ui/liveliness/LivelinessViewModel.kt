package com.powergrid.exemployee.ui.liveliness

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powergrid.exemployee.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LivelinessViewModel @Inject constructor() : ViewModel() {
    private val _submitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val submitState: StateFlow<UiState<String>> = _submitState.asStateFlow()

    private var capturedPhoto: Bitmap? = null

    fun setCapturedPhoto(bitmap: Bitmap?) { capturedPhoto = bitmap }

    fun submit() = viewModelScope.launch {
        if (capturedPhoto == null) {
            _submitState.value = UiState.Error("Please capture a photo first")
            return@launch
        }
        _submitState.value = UiState.Loading
        // Mocking API call
        delay(1500)
        _submitState.value = UiState.Success("Liveliness recorded. Ref: LVL-${System.currentTimeMillis()}")
    }

    fun reset() { _submitState.value = UiState.Idle }
}
