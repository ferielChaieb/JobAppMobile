package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.CandidateProfileRepository
import com.example.authentif.data.FirebaseCandidateProfileRepository
import com.example.authentif.data.models.CandidateProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CandidateProfileState {
    object Idle : CandidateProfileState()
    object Loading : CandidateProfileState()
    data class Success(val profile: CandidateProfile) : CandidateProfileState()
    data class Error(val message: String) : CandidateProfileState()
}

sealed class CandidateProfileSaveState {
    object Idle : CandidateProfileSaveState()
    object Loading : CandidateProfileSaveState()
    object Success : CandidateProfileSaveState()
    data class Error(val message: String) : CandidateProfileSaveState()
}

class CandidateProfileViewModel(
    private val repo: CandidateProfileRepository = FirebaseCandidateProfileRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<CandidateProfileState>(CandidateProfileState.Idle)
    val state: StateFlow<CandidateProfileState> = _state

    private val _saveState = MutableStateFlow<CandidateProfileSaveState>(CandidateProfileSaveState.Idle)
    val saveState: StateFlow<CandidateProfileSaveState> = _saveState

    fun getCurrentUserId(): String? = repo.getCurrentUserId()
    fun getCurrentUserEmail(): String? = repo.getCurrentUserEmail()

    fun logout() = repo.logout()

    fun loadProfile() {
        val uid = repo.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _state.value = CandidateProfileState.Error("Pas connecté")
            return
        }

        viewModelScope.launch {
            _state.value = CandidateProfileState.Loading
            val result = repo.loadProfile(uid)
            _state.value = if (result.isSuccess) {
                CandidateProfileState.Success(result.getOrNull()!!)
            } else {
                CandidateProfileState.Error(result.exceptionOrNull()?.message ?: "Load error")
            }
        }
    }

    fun saveProfile(profile: CandidateProfile) {
        val uid = repo.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _saveState.value = CandidateProfileSaveState.Error("Pas connecté")
            return
        }

        viewModelScope.launch {
            _saveState.value = CandidateProfileSaveState.Loading
            val result = repo.saveProfile(uid, profile)
            _saveState.value = if (result.isSuccess) {
                CandidateProfileSaveState.Success
            } else {
                CandidateProfileSaveState.Error(result.exceptionOrNull()?.message ?: "Save error")
            }
        }
    }
}