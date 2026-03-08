package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebasePostJobRepository
import com.example.authentif.data.PostJobRepository
import com.example.authentif.data.models.JobPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostJobState {
    object Idle : PostJobState()
    object Loading : PostJobState()
    object Success : PostJobState()
    data class Error(val message: String) : PostJobState()
}

class PostJobViewModel(
    private val repo: PostJobRepository = FirebasePostJobRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<PostJobState>(PostJobState.Idle)
    val state: StateFlow<PostJobState> = _state

    fun createJob(
        recruiterId: String?,
        title: String,
        type: String,
        location: String,
        minSalary: String,
        maxSalary: String,
        experience: String,
        description: String,
        skills: String
    ) {
        if (recruiterId.isNullOrBlank()) {
            _state.value = PostJobState.Error("Not logged in")
            return
        }

        if (title.isBlank()) {
            _state.value = PostJobState.Error("Job title is required")
            return
        }

        if (description.isBlank()) {
            _state.value = PostJobState.Error("Job description is required")
            return
        }

        viewModelScope.launch {
            _state.value = PostJobState.Loading

            val companyRes = repo.getRecruiterCompanyName(recruiterId)
            if (companyRes.isFailure) {
                _state.value = PostJobState.Error(
                    "Recruiter profile missing: ${companyRes.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val job = JobPublic(
                recruiterId = recruiterId,
                title = title.trim(),
                type = type.trim().ifBlank { "—" },
                location = location.trim().ifBlank { "—" },
                description = description.trim(),
                minSalary = minSalary.trim().ifBlank { "—" },
                maxSalary = maxSalary.trim().ifBlank { "—" },
                createdAt = System.currentTimeMillis(),
                status = "active"
            )

            val result = repo.createJob(
                recruiterId = recruiterId,
                companyName = companyRes.getOrNull() ?: "Recruiter",
                job = job,
                experience = experience.trim(),
                skills = skills.trim()
            )

            _state.value = if (result.isSuccess) {
                PostJobState.Success
            } else {
                PostJobState.Error(result.exceptionOrNull()?.message ?: "Failed to post job")
            }
        }
    }
}