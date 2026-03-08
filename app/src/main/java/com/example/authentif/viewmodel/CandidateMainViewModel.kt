package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import com.example.authentif.data.AuthRepository
import com.example.authentif.data.FirebaseAuthRepository

class CandidateMainViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return repo.isUserLoggedIn()
    }
}