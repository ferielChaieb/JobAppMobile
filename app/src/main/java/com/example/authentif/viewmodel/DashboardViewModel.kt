package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.DashboardRepository
import com.example.authentif.data.DashboardStats
import com.example.authentif.data.FirebaseDashboardRepository
import com.example.authentif.data.models.JobPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = false,
    val appliedCount: String = "0",
    val acceptedCount: String = "0",
    val jobs: List<JobPublic> = emptyList(),
    val jobsError: String? = null
)

class DashboardViewModel(
    private val repo: DashboardRepository = FirebaseDashboardRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(DashboardUiState())
    val ui: StateFlow<DashboardUiState> = _ui

    fun refresh(uid: String?) {
        if (uid.isNullOrBlank()) {
            _ui.value = DashboardUiState()
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, jobsError = null)

            val statsRes = repo.getStats(uid)
            val jobsRes = repo.getLatestJobsActive(3)

            val stats = statsRes.getOrNull() ?: DashboardStats()
            _ui.value = _ui.value.copy(
                loading = false,
                appliedCount = stats.appliedCount.toString(),
                acceptedCount = stats.acceptedCount.toString(),
                jobs = jobsRes.getOrNull() ?: emptyList(),
                jobsError = jobsRes.exceptionOrNull()?.message
            )
        }
    }
}