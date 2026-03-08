package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseRecruiterDashboardRepository
import com.example.authentif.data.RecruiterDashboardData
import com.example.authentif.data.RecruiterDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecruiterDashboardUiState(
    val loading: Boolean = false,
    val recruiterName: String = "Recruiter",
    val activeJobsCount: String = "0",
    val applicantsCount: String = "0",
    val hiredCount: String = "0",
    val recentApplicationsEmpty: Boolean = true,
    val recentApplicationsMessage: String = "",
    val error: String? = null
)

class RecruiterDashboardViewModel(
    private val repo: RecruiterDashboardRepository = FirebaseRecruiterDashboardRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecruiterDashboardUiState())
    val ui: StateFlow<RecruiterDashboardUiState> = _ui

    private val _recent = MutableStateFlow<List<com.example.authentif.data.models.RecentApplicationItem>>(emptyList())
    val recent: StateFlow<List<com.example.authentif.data.models.RecentApplicationItem>> = _recent

    fun isUserLoggedIn(): Boolean = repo.isUserLoggedIn()

    fun logout() = repo.logout()

    fun loadHeaderName() {
        val recruiterId = repo.getCurrentUserId()
        if (recruiterId.isNullOrBlank()) return

        viewModelScope.launch {
            val result = repo.getRecruiterName(recruiterId)
            _ui.value = _ui.value.copy(
                recruiterName = result.getOrNull() ?: "Recruiter"
            )
        }
    }

    fun loadStatsAndRecent() {
        val recruiterId = repo.getCurrentUserId()
        if (recruiterId.isNullOrBlank()) {
            _ui.value = _ui.value.copy(error = "Utilisateur non connecté")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)

            val result = repo.getDashboardData(recruiterId)
            if (result.isSuccess) {
                val data = result.getOrNull() ?: RecruiterDashboardData()
                _recent.value = data.recentApplications

                val empty = data.recentApplications.isEmpty()

                _ui.value = _ui.value.copy(
                    loading = false,
                    activeJobsCount = data.activeJobsCount.toString(),
                    applicantsCount = data.applicantsCount.toString(),
                    hiredCount = data.hiredCount.toString(),
                    recentApplicationsEmpty = empty,
                    recentApplicationsMessage = if (empty) "No applications yet." else "",
                    error = null
                )
            } else {
                _recent.value = emptyList()
                _ui.value = _ui.value.copy(
                    loading = false,
                    activeJobsCount = "0",
                    applicantsCount = "0",
                    hiredCount = "0",
                    recentApplicationsEmpty = true,
                    recentApplicationsMessage = "Error: ${result.exceptionOrNull()?.message}",
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}