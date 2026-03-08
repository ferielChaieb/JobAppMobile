package com.example.authentif.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.authentif.R
import com.example.authentif.viewmodel.ApplyState
import com.example.authentif.viewmodel.JobDetailsState
import com.example.authentif.viewmodel.JobDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class JobDetailsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: JobDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val jobId = intent.getStringExtra("jobId").orEmpty()
        if (jobId.isBlank()) { finish(); return }

        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener { finish() }

        observeUi()

        viewModel.load(jobId)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApply)
            .setOnClickListener {
                val candidateId = auth.currentUser?.uid
                if (candidateId.isNullOrBlank()) {
                    Toast.makeText(this, "Pas connecté", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.apply(jobId, candidateId)
            }
    }

    private fun observeUi() {
        val tvTitle = findViewById<android.widget.TextView>(R.id.tvTitle)
        val tvCompany = findViewById<android.widget.TextView>(R.id.tvCompany)
        val chipType = findViewById<com.google.android.material.chip.Chip>(R.id.tvType)
        val chipLocation = findViewById<com.google.android.material.chip.Chip>(R.id.tvLocation)
        val tvSalary = findViewById<android.widget.TextView>(R.id.tvSalary)
        val tvDesc = findViewById<android.widget.TextView>(R.id.tvDescription)

        tvTitle.text = "Loading..."

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is JobDetailsState.Loading -> {
                        tvTitle.text = "Loading..."
                    }
                    is JobDetailsState.Loaded -> {
                        val details = state.details
                        val job = details.job

                        val salaryText =
                            if (job.minSalary != "—" || job.maxSalary != "—")
                                "${job.minSalary} - ${job.maxSalary}"
                            else "—"

                        tvTitle.text = job.title.ifBlank { "—" }
                        tvCompany.text = details.companyName
                        chipType.text = job.type.ifBlank { "—" }
                        chipLocation.text = job.location.ifBlank { "—" }
                        tvSalary.text = salaryText
                        tvDesc.text = job.description.ifBlank { "—" }
                    }
                    is JobDetailsState.Error -> {
                        Toast.makeText(this@JobDetailsActivity, "Erreur: ${state.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.apply.collect { st ->
                when (st) {
                    is ApplyState.Loading -> {
                        // (optionnel) disable button ou show loader
                    }
                    is ApplyState.Success -> {
                        Toast.makeText(this@JobDetailsActivity, "Postulé ✅", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is ApplyState.Error -> {
                        Toast.makeText(this@JobDetailsActivity, st.message, Toast.LENGTH_SHORT).show()
                        if (st.message.contains("Déjà postulé")) finish()
                    }
                    else -> Unit
                }
            }
        }
    }
}