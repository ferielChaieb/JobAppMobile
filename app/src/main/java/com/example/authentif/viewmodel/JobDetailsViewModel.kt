package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseJobDetailsRepository
import com.example.authentif.data.JobDetailsRepository
import com.example.authentif.data.models.JobDetailsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class JobDetailsState {
    object Loading : JobDetailsState()
    data class Loaded(val details: JobDetailsUi) : JobDetailsState()
    data class Error(val message: String) : JobDetailsState()
}

sealed class ApplyState {
    object Idle : ApplyState()
    object Loading : ApplyState()
    object Success : ApplyState()
    data class Error(val message: String) : ApplyState()
}

class JobDetailsViewModel(
    private val repo: JobDetailsRepository = FirebaseJobDetailsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<JobDetailsState>(JobDetailsState.Loading)
    val state: StateFlow<JobDetailsState> = _state

    private val _apply = MutableStateFlow<ApplyState>(ApplyState.Idle)
    val apply: StateFlow<ApplyState> = _apply

    private var cachedJob: JobDetailsUi? = null

    fun load(jobId: String) {
        viewModelScope.launch {
            _state.value = JobDetailsState.Loading
            val res = repo.getJobDetails(jobId)
            if (res.isSuccess) {
                cachedJob = res.getOrNull()
                _state.value = JobDetailsState.Loaded(res.getOrNull()!!)
            } else {
                _state.value = JobDetailsState.Error(res.exceptionOrNull()?.message ?: "Erreur")
            }
        }
    }

    fun apply(jobId: String, candidateId: String) {
        val details = cachedJob ?: run {
            _apply.value = ApplyState.Error("Job non chargé")
            return
        }

        val recruiterId = details.job.recruiterId
        val companyName = details.companyName
        val jobTitle = details.job.title

        if (recruiterId.isBlank()) {
            _apply.value = ApplyState.Error("RecruiterId manquant")
            return
        }

        viewModelScope.launch {
            _apply.value = ApplyState.Loading
            val res = repo.applyToJob(
                jobId = jobId,
                recruiterId = recruiterId,
                companyName = companyName,
                jobTitle = jobTitle,
                candidateId = candidateId
            )
            _apply.value = if (res.isSuccess) {
                ApplyState.Success
            } else {
                ApplyState.Error(res.exceptionOrNull()?.message ?: "Erreur")
            }
        }
    }
}