package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseRecruiterJobsRepository
import com.example.authentif.data.RecruiterJobsRepository
import com.example.authentif.data.models.JobR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecruiterJobsUiState(
    val loading: Boolean = false,
    val currentStatus: String = "active",
    val jobs: List<JobR> = emptyList(),
    val error: String? = null
)

class RecruiterJobsViewModel(
    private val repo: RecruiterJobsRepository = FirebaseRecruiterJobsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecruiterJobsUiState())
    val ui: StateFlow<RecruiterJobsUiState> = _ui

    fun loadJobs(status: String) {
        val uid = repo.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _ui.value = _ui.value.copy(
                loading = false,
                currentStatus = status,
                jobs = emptyList(),
                error = "Not logged in"
            )
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                loading = true,
                currentStatus = status,
                jobs = emptyList(),
                error = null
            )

            val result = repo.getRecruiterJobsByStatus(uid, status)

            _ui.value = if (result.isSuccess) {
                _ui.value.copy(
                    loading = false,
                    currentStatus = status,
                    jobs = result.getOrNull().orEmpty(),
                    error = null
                )
            } else {
                _ui.value.copy(
                    loading = false,
                    currentStatus = status,
                    jobs = emptyList(),
                    error = result.exceptionOrNull()?.message ?: "Error loading jobs"
                )
            }
        }
    }
}