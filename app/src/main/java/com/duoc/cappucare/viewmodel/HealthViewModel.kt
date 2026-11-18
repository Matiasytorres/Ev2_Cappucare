package com.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.cappucare.data.model.GlobalHealthStats
import com.duoc.cappucare.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HealthUiState {
    object Loading : HealthUiState()
    data class Success(val stats: GlobalHealthStats) : HealthUiState()
    data class Error(val message: String) : HealthUiState()
}

class HealthViewModel(
    private val repository: HealthRepository = HealthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        fetchStats()
    }

    fun fetchStats() {
        viewModelScope.launch {
            _uiState.value = HealthUiState.Loading
            repository.getGlobalStats()
                .onSuccess { _uiState.value = HealthUiState.Success(it) }
                .onFailure { _uiState.value = HealthUiState.Error(it.message ?: "Error de conexión") }
        }
    }
}