package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseRecruiterCandidatesRepository
import com.example.authentif.data.RecruiterCandidatesRepository
import com.example.authentif.data.models.RecruiterCandidateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecruiterCandidatesUiState(
    val loading: Boolean = false,
    val items: List<RecruiterCandidateItem> = emptyList(),
    val error: String? = null
)

class RecruiterCandidatesViewModel(
    private val repo: RecruiterCandidatesRepository = FirebaseRecruiterCandidatesRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecruiterCandidatesUiState())
    val ui: StateFlow<RecruiterCandidatesUiState> = _ui

    fun loadApplicants() {
        val recruiterId = repo.getCurrentUserId()
        if (recruiterId.isNullOrBlank()) {
            _ui.value = _ui.value.copy(error = "Recruiter non connecté")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)

            val result = repo.getApplicants(recruiterId)
            _ui.value = if (result.isSuccess) {
                _ui.value.copy(
                    loading = false,
                    items = result.getOrNull().orEmpty(),
                    error = null
                )
            } else {
                _ui.value.copy(
                    loading = false,
                    items = emptyList(),
                    error = result.exceptionOrNull()?.message ?: "Erreur"
                )
            }
        }
    }

    fun updateStatus(appId: String, newStatus: String) {
        viewModelScope.launch {
            val result = repo.updateApplicationStatus(appId, newStatus)
            if (result.isSuccess) {
                loadApplicants()
            } else {
                _ui.value = _ui.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Erreur de mise à jour"
                )
            }
        }
    }

    fun allCount(): Int = ui.value.items.size
    fun pendingCount(): Int = ui.value.items.count { it.status == "pending" }
    fun acceptedCount(): Int = ui.value.items.count { it.status == "accepted" }
    fun rejectedCount(): Int = ui.value.items.count { it.status == "rejected" }
}