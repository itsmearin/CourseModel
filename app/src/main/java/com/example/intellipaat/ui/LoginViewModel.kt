package com.example.intellipaat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            _uiState.value = LoginUiState.Error("Invalid Email address format")
            return
        }

        if(password.length < 6) {
            _uiState.value = LoginUiState.Error("Password must be atleast 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            delay(1500)
            _uiState.value = LoginUiState.Success
        }
    }
}