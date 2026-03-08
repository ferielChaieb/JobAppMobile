package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.CandidateJobsRepository
import com.example.authentif.data.FirebaseCandidateJobsRepository
import com.example.authentif.data.models.JobPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class CandidateJobsUiState(
    val loading: Boolean = false,
    val allJobs: List<JobPublic> = emptyList(),
    val shownJobs: List<JobPublic> = emptyList(),
    val searchQuery: String = "",
    val selectedType: String = "All",
    val error: String? = null
)

class CandidateJobsViewModel(
    private val repo: CandidateJobsRepository = FirebaseCandidateJobsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(CandidateJobsUiState())
    val ui: StateFlow<CandidateJobsUiState> = _ui

    fun loadJobs() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)

            val result = repo.getActiveJobs()

            if (result.isSuccess) {
                val jobs = result.getOrNull().orEmpty()
                _ui.value = _ui.value.copy(
                    loading = false,
                    allJobs = jobs
                )
                applyFilters(_ui.value.searchQuery, _ui.value.selectedType)
            } else {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Error loading jobs"
                )
            }
        }
    }

    fun updateSearch(query: String) {
        applyFilters(query, _ui.value.selectedType)
    }

    fun updateType(type: String) {
        applyFilters(_ui.value.searchQuery, type)
    }

    private fun applyFilters(query: String, selectedType: String) {
        val q = query.trim().lowercase(Locale.getDefault())

        val filtered = _ui.value.allJobs.filter { job ->
            val title = job.title.lowercase(Locale.getDefault())
            val company = job.companyName.lowercase(Locale.getDefault())
            val location = job.location.lowercase(Locale.getDefault())
            val type = job.type.lowercase(Locale.getDefault())

            val matchesSearch =
                q.isEmpty() || title.contains(q) || company.contains(q) || location.contains(q) || type.contains(q)

            val matchesType =
                selectedType == "All" || job.type.equals(selectedType, ignoreCase = true)

            matchesSearch && matchesType
        }

        _ui.value = _ui.value.copy(
            shownJobs = filtered,
            searchQuery = query,
            selectedType = selectedType
        )
    }
}