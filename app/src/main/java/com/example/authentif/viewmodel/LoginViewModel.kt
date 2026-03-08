package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.AuthRepository
import com.example.authentif.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState.Error("Champs requis")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading

            val result = repo.loginAndGetRole(email.trim(), password.trim())
            _state.value = if (result.isSuccess) {
                LoginState.Success(result.getOrNull() ?: "candidate")
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Erreur inconnue")
            }
        }
    }
}