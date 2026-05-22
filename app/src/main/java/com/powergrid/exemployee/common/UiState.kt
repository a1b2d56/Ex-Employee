package com.powergrid.exemployee.common

sealed class UiState<out T> {
    object Idle    : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, @Suppress("unused") val code: Int? = null) : UiState<Nothing>()
}
