package com.example.authentif.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.viewmodel.CandidateProfileDetailsState
import com.example.authentif.viewmodel.CandidateProfileDetailsViewModel
import kotlinx.coroutines.launch

class CandidateProfileDetailsActivity : AppCompatActivity() {

    private val viewModel: CandidateProfileDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_profile_details)

        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val candidateId = intent.getStringExtra("candidateId").orEmpty()
        if (candidateId.isBlank()) {
            Toast.makeText(this, "candidateId manquant", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        observeViewModel()
        viewModel.loadCandidate(candidateId)
    }

    private fun observeViewModel() {
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPosition = findViewById<TextView>(R.id.tvPosition)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvExperience = findViewById<TextView>(R.id.tvExperience)
        val tvSkills = findViewById<TextView>(R.id.tvSkills)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CandidateProfileDetailsState.Loading -> {
                        tvName.text = "Loading..."
                    }

                    is CandidateProfileDetailsState.Success -> {
                        val profile = state.profile

                        tvName.text = profile.name.ifBlank { "Candidate" }
                        tvEmail.text = profile.email.ifBlank { "—" }
                        tvPosition.text = profile.proInfo.position.ifBlank { "—" }
                        tvLocation.text = profile.proInfo.location.ifBlank { "—" }
                        tvExperience.text = profile.proInfo.experienceYears.ifBlank { "—" }
                        tvSkills.text = if (profile.skills.isEmpty()) "—" else profile.skills.joinToString(", ")
                    }

                    is CandidateProfileDetailsState.Error -> {
                        Toast.makeText(
                            this@CandidateProfileDetailsActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    else -> Unit
                }
            }
        }
    }
}