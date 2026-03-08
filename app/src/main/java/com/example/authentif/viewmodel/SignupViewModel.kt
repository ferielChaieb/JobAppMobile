package com.example.authentif.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.AuthRepository
import com.example.authentif.data.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data class Success(val role: String) : SignupState()
    data class Error(val message: String) : SignupState()
}

class SignupViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<SignupState>(SignupState.Idle)
    val state: StateFlow<SignupState> = _state

    fun signup(name: String, email: String, password: String, role: String) {
        val n = name.trim()
        val e = email.trim()
        val p = password.trim()

        // validations (comme ton code actuel)
        if (n.isEmpty()) {
            _state.value = SignupState.Error("Nom requis")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            _state.value = SignupState.Error("Email invalide")
            return
        }
        if (p.length < 6) {
            _state.value = SignupState.Error("Mot de passe min 6 caractères")
            return
        }

        viewModelScope.launch {
            _state.value = SignupState.Loading
            val res = repo.signupAndCreateProfile(n, e, p, role)
            _state.value = if (res.isSuccess) {
                SignupState.Success(res.getOrNull() ?: role)
            } else {
                SignupState.Error(res.exceptionOrNull()?.localizedMessage ?: "Erreur de création du compte")
            }
        }
    }
}