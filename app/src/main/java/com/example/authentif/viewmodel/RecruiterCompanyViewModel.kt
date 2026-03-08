package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseRecruiterCompanyRepository
import com.example.authentif.data.RecruiterCompanyRepository
import com.example.authentif.data.models.RecruiterCompanyProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecruiterCompanyState {
    object Idle : RecruiterCompanyState()
    object Loading : RecruiterCompanyState()
    data class Success(val profile: RecruiterCompanyProfile) : RecruiterCompanyState()
    data class Error(val message: String) : RecruiterCompanyState()
}

sealed class RecruiterCompanySaveState {
    object Idle : RecruiterCompanySaveState()
    object Loading : RecruiterCompanySaveState()
    object Success : RecruiterCompanySaveState()
    data class Error(val message: String) : RecruiterCompanySaveState()
}

class RecruiterCompanyViewModel(
    private val repo: RecruiterCompanyRepository = FirebaseRecruiterCompanyRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<RecruiterCompanyState>(RecruiterCompanyState.Idle)
    val state: StateFlow<RecruiterCompanyState> = _state

    private val _saveState = MutableStateFlow<RecruiterCompanySaveState>(RecruiterCompanySaveState.Idle)
    val saveState: StateFlow<RecruiterCompanySaveState> = _saveState

    fun getCurrentUserId(): String? = repo.getCurrentUserId()

    fun logout() = repo.logout()

    fun loadRecruiterProfile() {
        val uid = repo.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _state.value = RecruiterCompanyState.Error("Not logged in")
            return
        }

        viewModelScope.launch {
            _state.value = RecruiterCompanyState.Loading

            val result = repo.loadRecruiterProfile(uid)
            _state.value = if (result.isSuccess) {
                RecruiterCompanyState.Success(result.getOrNull() ?: RecruiterCompanyProfile())
            } else {
                RecruiterCompanyState.Error(result.exceptionOrNull()?.message ?: "Error loading profile")
            }
        }
    }

    fun saveRecruiterProfile(profile: RecruiterCompanyProfile) {
        val uid = repo.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _saveState.value = RecruiterCompanySaveState.Error("Not logged in")
            return
        }

        if (profile.industry.isBlank() || profile.location.isBlank()) {
            _saveState.value = RecruiterCompanySaveState.Error("Industry and Location are required")
            return
        }

        viewModelScope.launch {
            _saveState.value = RecruiterCompanySaveState.Loading

            val result = repo.saveRecruiterProfile(uid, profile)
            _saveState.value = if (result.isSuccess) {
                RecruiterCompanySaveState.Success
            } else {
                RecruiterCompanySaveState.Error(result.exceptionOrNull()?.message ?: "Save failed")
            }
        }
    }
}