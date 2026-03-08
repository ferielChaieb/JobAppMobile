package com.example.authentif.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.authentif.databinding.ActivityPostJobBinding
import com.example.authentif.viewmodel.PostJobState
import com.example.authentif.viewmodel.PostJobViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PostJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostJobBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: PostJobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val jobTypes = listOf("Full-time", "Part-time", "Contract", "Internship")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, jobTypes)
        (binding.actType as MaterialAutoCompleteTextView).setAdapter(adapter)

        binding.btnPostJob.setOnClickListener {
            viewModel.createJob(
                recruiterId = auth.currentUser?.uid,
                title = binding.etTitle.text?.toString().orEmpty(),
                type = binding.actType.text?.toString().orEmpty(),
                location = binding.etLocation.text?.toString().orEmpty(),
                minSalary = binding.etMinSalary.text?.toString().orEmpty(),
                maxSalary = binding.etMaxSalary.text?.toString().orEmpty(),
                experience = binding.etExperience.text?.toString().orEmpty(),
                description = binding.etDescription.text?.toString().orEmpty(),
                skills = binding.etSkills.text?.toString().orEmpty()
            )
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PostJobState.Idle -> setLoading(false)

                    is PostJobState.Loading -> setLoading(true)

                    is PostJobState.Success -> {
                        setLoading(false)
                        toast("Job posted ✅")
                        finish()
                    }

                    is PostJobState.Error -> {
                        setLoading(false)
                        toast(state.message)
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnPostJob.isEnabled = !isLoading
        binding.btnPostJob.text = if (isLoading) "Posting..." else "Post Job"
        binding.btnPostJob.alpha = if (isLoading) 0.85f else 1f
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}