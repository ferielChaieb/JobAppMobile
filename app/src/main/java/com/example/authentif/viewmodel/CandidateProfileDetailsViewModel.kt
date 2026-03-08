package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.CandidateProfileDetailsRepository
import com.example.authentif.data.FirebaseCandidateProfileDetailsRepository
import com.example.authentif.data.models.CandidateProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CandidateProfileDetailsState {
    object Idle : CandidateProfileDetailsState()
    object Loading : CandidateProfileDetailsState()
    data class Success(val profile: CandidateProfile) : CandidateProfileDetailsState()
    data class Error(val message: String) : CandidateProfileDetailsState()
}

class CandidateProfileDetailsViewModel(
    private val repo: CandidateProfileDetailsRepository = FirebaseCandidateProfileDetailsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<CandidateProfileDetailsState>(CandidateProfileDetailsState.Idle)
    val state: StateFlow<CandidateProfileDetailsState> = _state

    fun loadCandidate(candidateId: String) {
        if (candidateId.isBlank()) {
            _state.value = CandidateProfileDetailsState.Error("candidateId manquant")
            return
        }

        viewModelScope.launch {
            _state.value = CandidateProfileDetailsState.Loading

            val result = repo.getCandidateProfile(candidateId)
            _state.value = if (result.isSuccess) {
                CandidateProfileDetailsState.Success(result.getOrNull()!!)
            } else {
                CandidateProfileDetailsState.Error(
                    result.exceptionOrNull()?.message ?: "Erreur"
                )
            }
        }
    }
}