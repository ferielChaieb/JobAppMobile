package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authentif.R
import com.example.authentif.data.models.JobR
import com.example.authentif.databinding.FragmentRecruiterJobsBinding
import com.example.authentif.ui.adapters.RecruiterJobsAdapter
import com.example.authentif.viewmodel.RecruiterJobsViewModel
import kotlinx.coroutines.launch

class RecruiterJobsFragment : Fragment(R.layout.fragment_recruiter_jobs) {

    private var _binding: FragmentRecruiterJobsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecruiterJobsAdapter
    private val viewModel: RecruiterJobsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecruiterJobsBinding.bind(view)

        adapter = RecruiterJobsAdapter(
            onDetail = { job -> showJobDetail(job) },
            onEdit = {
                Toast.makeText(requireContext(), "Edit bientôt…", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJobs.adapter = adapter

        binding.chipActive.isChecked = true

        binding.chipActive.setOnClickListener { viewModel.loadJobs("active") }
        binding.chipClosed.setOnClickListener { viewModel.loadJobs("closed") }
        binding.chipDraft.setOnClickListener { viewModel.loadJobs("draft") }

        binding.btnPostJob.setOnClickListener {
            startActivity(Intent(requireContext(), PostJobActivity::class.java))
        }

        observeViewModel()
        viewModel.loadJobs("active")
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadJobs(viewModel.ui.value.currentStatus)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ui.collect { ui ->
                adapter.submit(ui.jobs)

                ui.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showJobDetail(job: JobR) {
        val msg = """
            Titre: ${job.title}
            Type: ${job.type}
            Lieu: ${job.location}

            Description:
            ${job.description}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Detail Job")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}