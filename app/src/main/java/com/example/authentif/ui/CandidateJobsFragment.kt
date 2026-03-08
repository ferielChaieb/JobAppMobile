package com.example.authentif.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authentif.R
import com.example.authentif.data.models.JobPublic
import com.example.authentif.ui.adapters.CandidateJobsAdapter
import com.example.authentif.ui.extensions.addTextChangedListenerSimple
import com.example.authentif.viewmodel.CandidateJobsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class CandidateJobsFragment : Fragment(R.layout.fragment_candidate_jobs) {

    private lateinit var etSearch: EditText
    private lateinit var tvCount: TextView
    private lateinit var rvJobs: RecyclerView

    private lateinit var chipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipFullTime: Chip
    private lateinit var chipPartTime: Chip
    private lateinit var chipInternship: Chip
    private lateinit var chipContract: Chip

    private lateinit var adapter: CandidateJobsAdapter

    private val viewModel: CandidateJobsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI references
        etSearch = view.findViewById(R.id.etSearch)
        tvCount = view.findViewById(R.id.tvCount)
        rvJobs = view.findViewById(R.id.rvJobs)

        chipGroup = view.findViewById(R.id.chipGroup)
        chipAll = view.findViewById(R.id.chipAll)
        chipFullTime = view.findViewById(R.id.chipFullTime)
        chipPartTime = view.findViewById(R.id.chipPartTime)
        chipInternship = view.findViewById(R.id.chipInternship)
        chipContract = view.findViewById(R.id.chipContract)

        // Adapter
        adapter = CandidateJobsAdapter(
            items = mutableListOf(),
            onFavClick = { job ->
                Toast.makeText(requireContext(), "Favorite coming soon", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { job ->
                val intent = Intent(requireContext(), JobDetailsActivity::class.java)
                intent.putExtra("jobId", job.id)
                startActivity(intent)
            }
        )

        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        // Chips filter
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            viewModel.updateType(getSelectedType())
        }

        // Search
        etSearch.addTextChangedListenerSimple {
            viewModel.updateSearch(etSearch.text?.toString().orEmpty())
        }

        observeViewModel()

        viewModel.loadJobs()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadJobs()
    }

    // Determine selected job type
    private fun getSelectedType(): String {
        val checkedId = chipGroup.checkedChipId

        if (checkedId == View.NO_ID || checkedId == chipAll.id)
            return "All"

        return when (checkedId) {
            chipFullTime.id -> "Full-time"
            chipPartTime.id -> "Part-time"
            chipInternship.id -> "Internship"
            chipContract.id -> "Contract"
            else -> "All"
        }
    }

    // Observe ViewModel state
    private fun observeViewModel() {

        lifecycleScope.launch {

            viewModel.ui.collect { ui ->

                adapter.updateData(ui.shownJobs)

                tvCount.text = "${ui.shownJobs.size} Jobs Found"

                ui.error?.let {
                    Toast.makeText(
                        requireContext(),
                        "Error loading jobs: $it",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}